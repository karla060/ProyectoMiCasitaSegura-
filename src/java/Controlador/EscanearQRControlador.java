/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//hola
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

    String tipo = v.getTipoVisita() != null ? v.getTipoVisita().trim().toLowerCase() : "";

    // --- Validar reglas de visita SOLO si está intentando entrar ---
    if (v.getDentro() == 0) { // Entrada
        if ("por intentos".equals(tipo)) {
            if (v.getIntentos() == null || v.getIntentos() <= 0) {
                res.success = false;
                res.message = "Acceso denegado - sin intentos disponibles";
                System.out.println("[Visitante] Sin intentos: ID " + v.getId());
                return;
            }
        } else if ("visita".equals(tipo) && v.getFechaVisita() != null) {
    // Normalizar a yyyy-MM-dd para comparar solo fechas
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        String hoyStr = sdf.format(new java.util.Date());
        String visitaStr = sdf.format(v.getFechaVisita());
    
        java.util.Date hoyDate = sdf.parse(hoyStr);
        java.util.Date visitaDate = sdf.parse(visitaStr);

        if (hoyDate.after(visitaDate)) {
        res.success = false;
        res.message = "Acceso denegado - fecha expirada";
        System.out.println("[Visitante] Fecha expirada: ID " + v.getId() + " (hoy: " + hoyDate + ")");
        return;
        }
    }

        
        /*else if ("visita".equals(tipo) && v.getFechaVisita() != null) {
            java.util.Date hoy = new java.util.Date();
            if (hoy.after(v.getFechaVisita())) {
                res.success = false;
                res.message = "Acceso denegado - fecha expirada";
                System.out.println("[Visitante] Fecha expirada: ID " + v.getId() + " (hoy: " + hoy + ")");
                return;
            }
        }*/

        // --- Restar intento solo al entrar ---
        if ("por intentos".equals(tipo)) {
            dao.restarIntentoSiCorresponde(v.getId());
            v.setIntentos(v.getIntentos() - 1);
            System.out.println("[Visitante] Intento restado en entrada: ID " + v.getId() + " - Quedan " + v.getIntentos());
        }

        // --- Abrir entrada ---
        arduino.abrirEntrada();
        dao.actualizarEstado(v.getId(), 1);
        dao.registrarAuditoria(v.getId(), "entrada");

        res.success = true;
        res.nombre  = v.getNombre();
        res.message = "Acceso autorizado - Entrada (Visitante)";
        System.out.println("[Visitante] Entrada autorizada: ID " + v.getId());

    } else { // Salida
        // --- Abrir salida SIN validar intentos ---
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
                if ("por intentos".equals(tipo)) {
            // Restar intento solo en la entrada
            dao.restarIntentoSiCorresponde(v.getId());
            v.setIntentos(v.getIntentos() - 1);
            System.out.println("[Visitante] Intento restado en entrada: ID " + v.getId() + " - Quedan " + v.getIntentos());
        }
        
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
    }*/

}
















