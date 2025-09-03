/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Controlador;

import Modelo.Usuarios;
import ModeloDAO.UsuariosDAO;
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
            Usuarios u = usuarioDAO.buscarPorId(id);

            if (u != null) {
                if (u.getDentro() == 0) {
                    // Usuario está fuera -> abrir entrada
                    arduino.abrirEntrada();
                    usuarioDAO.actualizarEstado(u.getId(), 1);
                    usuarioDAO.registrarAuditoria(u.getId(), "entrada");

                    responseData.success = true;
                    responseData.nombre  = u.getNombres();
                    responseData.message = "Acceso autorizado - Entrada";
                } else {
                    // Usuario está dentro -> abrir salida
                    arduino.abrirSalida();
                    usuarioDAO.actualizarEstado(u.getId(), 0);
                    usuarioDAO.registrarAuditoria(u.getId(), "salida");

                    responseData.success = true;
                    responseData.nombre  = u.getNombres();
                    responseData.message = "Acceso autorizado - Salida";
                }
            } else {
                responseData.success = false;
                responseData.message = "Usuario no registrado.";
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
}






/*
package Controlador;

import Modelo.Usuarios;
import ModeloDAO.UsuariosDAO;
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
        String puertoSerie = getServletConfig()
                               .getInitParameter("puertoSerie");
        if (puertoSerie == null || puertoSerie.isEmpty()) {
            puertoSerie = "COM11";  // valor por defecto
        }

        try {
            arduino = new ArduinoService(puertoSerie);
            log("ArduinoService: puerto abierto en " + puertoSerie);
        } catch (Exception ex) {
            // No abortar init: solo registremos el error
            arduino = null;
            log("No se pudo abrir el puerto " + puertoSerie, ex);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("vistas/EscanearQR.jsp")
           .forward(req, resp);
    }
   
    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        ResponseData responseData = new ResponseData();

        // Si el servicio Arduino no está inicializado, devolvemos error
        if (arduino == null) {
            responseData.success = false;
            responseData.message = "Hardware no disponible. " +
                "No se pudo inicializar conexión con Arduino.";
            resp.getWriter().write(gson.toJson(responseData));
            return;
        }

        // 1. Leer JSON con el campo "qr"
        String body = new BufferedReader(req.getReader())
                          .lines().collect(Collectors.joining());
        RequestData requestData = gson.fromJson(body, RequestData.class);

        try {
            int id = Integer.parseInt(requestData.qr.trim());
            Usuarios u = usuarioDAO.buscarPorId(id);

            if (u != null) {
                arduino.abrirTalanquera();
                responseData.success = true;
                responseData.nombre  = u.getNombres();
            } else {
                responseData.success = false;
                responseData.message = "Usuario no registrado.";
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

    private static class RequestData {
        String qr;
    }

    private static class ResponseData {
        boolean success;
        String nombre;
        String message;
    }

    @Override
    public void destroy() {
        if (arduino != null) {
            arduino.cerrar();
        }
    }
}
*/



