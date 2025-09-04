/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Controlador;

import Modelo.Usuarios;
import Modelo.Visitante;
import ModeloDAO.UsuariosDAO;
import ModeloDAO.VisitanteDAO;
import service.ArduinoService;
import com.google.gson.Gson;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.util.stream.Collectors;

@WebServlet("/EscanearQR")
public class EscanearQRControlador extends HttpServlet {

    private UsuariosDAO usuarioDAO;
    private ArduinoService arduino;
    private final Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        usuarioDAO = new UsuariosDAO();

        // Lee el puerto desde init-param
        String puertoSerie = getServletConfig().getInitParameter("puertoSerie");
        if (puertoSerie == null || puertoSerie.isEmpty()) {
            puertoSerie = "COM11";  // valor por defecto
        }

        try {
            arduino = new ArduinoService(puertoSerie);
            log("ArduinoService: puerto abierto en " + puertoSerie);
        } catch (Exception ex) {
            arduino = null;
            log("No se pudo abrir el puerto " + puertoSerie, ex);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("vistas/EscanearQR.jsp")
           .forward(req, resp);
    }

    
    @Override
protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

    resp.setContentType("application/json");
    ResponseData responseData = new ResponseData();

    if (arduino == null) {
        responseData.success = false;
        responseData.message = "Hardware no disponible. No se pudo inicializar conexión con Arduino.";
        resp.getWriter().write(gson.toJson(responseData));
        return;
    }

    // Leer JSON con el campo "qr"
    String body = new BufferedReader(req.getReader())
                      .lines().collect(Collectors.joining());
    RequestData requestData = gson.fromJson(body, RequestData.class);

     
    try {
        int id = Integer.parseInt(requestData.qr.trim());

        // 🔹 Aquí va tu bloque
        // Primero busca usuario
        Usuarios u = usuarioDAO.buscarPorId(id);
        if (u != null) {
            manejarAccesoUsuario(u, responseData);
        } else {
            // Si no es usuario, buscar visitante
            VisitanteDAO visitanteDAO = new VisitanteDAO();
            Visitante v = visitanteDAO.obtenerPorId(id);

            if (v != null) {
                manejarAccesoVisitante(v, visitanteDAO, responseData);
            } else {
                responseData.success = false;
                responseData.message = "Código QR no registrado.";
            }
        }

    } catch (NumberFormatException ex) {
        responseData.success = false;
        responseData.message = "QR inválido.";
    } catch (Exception ex) {
        responseData.success = false;
        responseData.message = "Error interno: " + ex.getMessage();
    }
   
    resp.getWriter().write(gson.toJson(responseData));
 
}
 
    @Override
    public void destroy() {
        if (arduino != null) {
            arduino.cerrar();
        }
    }

    // -------------------------
    // Clases internas para JSON
    // -------------------------
    private static class RequestData {
        String qr;
    }

    private static class ResponseData {
        boolean success;
        String nombre;
        String message;
    }
    
    // --- Usuarios ---
private void manejarAccesoUsuario(Usuarios u, ResponseData res) throws Exception {
    if (u.getDentro() == 0) {
        arduino.abrirEntrada();
        usuarioDAO.actualizarEstado(u.getId(), 1);
        usuarioDAO.registrarAuditoria(u.getId(), "entrada");
        res.success = true;
        res.nombre  = u.getNombres();
        res.message = "Acceso autorizado - Entrada (Usuario)";
    } else {
        arduino.abrirSalida();
        usuarioDAO.actualizarEstado(u.getId(), 0);
        usuarioDAO.registrarAuditoria(u.getId(), "salida");
        res.success = true;
        res.nombre  = u.getNombres();
        res.message = "Acceso autorizado - Salida (Usuario)";
    }
}


private void manejarAccesoVisitante(Visitante v, VisitanteDAO dao, ResponseData res) throws Exception {
    if (v == null) {
        res.success = false;
        res.message = "Visitante no encontrado";
        System.out.println("[Visitante] Visitante no encontrado.");
        return;
    }

    // --- Validar reglas de visita ---
    String tipo = v.getTipoVisita() != null ? v.getTipoVisita().trim().toLowerCase() : "";

    if ("por intentos".equals(tipo)) {
        if (v.getIntentos() == null || v.getIntentos() <= 0) {
            res.success = false;
            res.message = "Acceso denegado - sin intentos disponibles";
            System.out.println("[Visitante] Sin intentos: ID " + v.getId());
            return;
        }

        // Restar intento en BD (sin bloquear acceso si falla)
        dao.restarIntentoSiCorresponde(v.getId());
        // Reflejarlo en el objeto local
        v.setIntentos(v.getIntentos() - 1);
        System.out.println("[Visitante] Intento restado: ID " + v.getId() + " - Quedan " + v.getIntentos());
    } 
    else if ("visita".equals(tipo) && v.getFechaVisita() != null) {
        java.util.Date hoy = new java.util.Date();
        if (hoy.after(v.getFechaVisita())) {
            res.success = false;
            res.message = "Acceso denegado - fecha expirada";
            System.out.println("[Visitante] Fecha expirada: ID " + v.getId() + " (hoy: " + hoy + ")");
            return;
        }
    }

    // --- Control entrada/salida ---
    if (v.getDentro() == 0) {
        // Entrada
        arduino.abrirEntrada();
        dao.actualizarEstado(v.getId(), 1);
        dao.registrarAuditoria(v.getId(), "entrada");

        res.success = true;
        res.nombre  = v.getNombre();
        res.message = "Acceso autorizado - Entrada (Visitante)";
        System.out.println("[Visitante] Entrada autorizada: ID " + v.getId());
    } else {
        // Salida
        arduino.abrirSalida();
        dao.actualizarEstado(v.getId(), 0);
        dao.registrarAuditoria(v.getId(), "salida");

        res.success = true;
        res.nombre  = v.getNombre();
        res.message = "Acceso autorizado - Salida (Visitante)";
        System.out.println("[Visitante] Salida autorizada: ID " + v.getId());
    }
}









/*
    private void manejarAccesoVisitante(Visitante v, VisitanteDAO dao, ResponseData res) throws Exception {
    if (v == null) {
        res.success = false;
        res.message = "Visitante no encontrado";
        return;
    }

    // 1) Validar reglas de visita
    if ("Por intentos".equalsIgnoreCase(v.getTipoVisita())) {
        if (v.getIntentos() == null || v.getIntentos() <= 0) {
            res.success = false;
            res.message = "Acceso denegado - sin intentos disponibles";
            System.out.println("[Visitante] Sin intentos: ID " + v.getId());
            return;
        }

        // Restar intento de manera segura
        boolean restado = dao.restarIntentoSiCorresponde(v.getId());
        if (!restado) {
            res.success = false;
            res.message = "Acceso denegado - no se pudo restar intento";
            System.out.println("[Visitante] ERROR al restar intento: ID " + v.getId() + ", intentos restantes: " + v.getIntentos());
            System.out.println("[Visitante] ERROR al restar intento: ID " + v.getId());
            return;
        }

        // Reflejarlo en el objeto local
        v.setIntentos(v.getIntentos() - 1);
        System.out.println("[Visitante] Intento restado en BD: ID " + v.getId() + " - Quedan " + v.getIntentos());
    }
    else if ("Visita".equalsIgnoreCase(v.getTipoVisita()) && v.getFechaVisita() != null) {
        java.util.Date hoy = new java.util.Date();
        if (hoy.after(v.getFechaVisita())) {
            res.success = false;
            res.message = "Acceso denegado - fecha expirada";
            System.out.println("[Visitante] Fecha expirada: ID " + v.getId() + " (hoy: " + hoy + ")");
            return;
        }
    }

    // 2) Control entrada/salida
    if (v.getDentro() == 0) {
        // Entrada
        arduino.abrirEntrada();
        dao.actualizarEstado(v.getId(), 1);
        dao.registrarAuditoria(v.getId(), "entrada");

        res.success = true;
        res.nombre  = v.getNombre();
        res.message = "Acceso autorizado - Entrada (Visitante)";
        System.out.println("[Visitante] Entrada autorizada: ID " + v.getId());
    } else {
        // Salida
        arduino.abrirSalida();
        dao.actualizarEstado(v.getId(), 0);
        dao.registrarAuditoria(v.getId(), "salida");

        res.success = true;
        res.nombre  = v.getNombre();
        res.message = "Acceso autorizado - Salida (Visitante)";
        System.out.println("[Visitante] Salida autorizada: ID " + v.getId());
    }
}
*/



/*
    private void manejarAccesoVisitante(Visitante v, VisitanteDAO dao, ResponseData res) throws Exception {
    String tipo = v.getTipoVisita() == null ? "" : v.getTipoVisita().trim();

    // 1) Validar reglas de visita
    if ("Por intentos".equalsIgnoreCase(tipo)) {

        // 1.1) Sin intentos → denegar
        if (v.getIntentos() <= 0) {
            res.success = false;
            res.message = "Acceso denegado - sin intentos disponibles";
            System.out.println("[Visitante] Sin intentos: ID " + v.getId());
            return;
        }

        // 1.2) Restar intento en BD
        boolean restado = dao.restarIntentoSiCorresponde(v.getId());
        System.out.println("[Visitante] Intento restado en BD: " + restado);
        if (!restado) {
            res.success = false;
            res.message = "Acceso denegado - no se pudo restar intento";
            System.out.println("[Visitante] ERROR al restar intento: ID " + v.getId());
            return;
        }

        // 1.3) Reflejar en el objeto
        v.setIntentos(v.getIntentos() - 1);

    } else if ("Visita".equalsIgnoreCase(tipo) && v.getFechaVisita() != null) {
        // Validar fecha
        java.util.Date hoy = new java.util.Date();
        if (hoy.after(v.getFechaVisita())) {
            res.success = false;
            res.message = "Acceso denegado - fecha expirada";
            System.out.println("[Visitante] Fecha expirada: ID " + v.getId() + " (hoy: " + hoy + ")");
            return;
        }
    }

    // 2) Control dentro/fuera
    if (v.getDentro() == 0) {
        arduino.abrirEntrada();
        dao.actualizarEstado(v.getId(), 1);
        dao.registrarAuditoria(v.getId(), "entrada");
        res.success = true;
        res.nombre  = v.getNombre();
        res.message = "Acceso autorizado - Entrada (Visitante)";
        System.out.println("[Visitante] Entrada autorizada: ID " + v.getId());
    } else {
        arduino.abrirSalida();
        dao.actualizarEstado(v.getId(), 0);
        dao.registrarAuditoria(v.getId(), "salida");
        res.success = true;
        res.nombre  = v.getNombre();
        res.message = "Acceso autorizado - Salida (Visitante)";
        System.out.println("[Visitante] Salida autorizada: ID " + v.getId());
    }
}
*/

}




