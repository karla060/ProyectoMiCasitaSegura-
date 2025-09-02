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





/*
package Controlador;

import Modelo.Usuarios;
import ModeloDAO.UsuariosDAO;
import service.ArduinoService;

import com.google.gson.Gson;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;

@WebServlet("/EscanearQR")
public class EscanearQRControlador extends HttpServlet {

    private UsuariosDAO usuarioDAO;
    private ArduinoService arduino;
    private Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        // Inicializamos DAO y ArduinoService con el puerto serie que uses
        usuarioDAO = new UsuariosDAO();
        arduino    = new ArduinoService("COM11");
    }

    // 1) GET → muestra el JSP que captura la cámara y escanea el código QR
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Ruta absoluta a tu JSP en webapp (ajústala si la guardas en /vistas/)
        req.getRequestDispatcher("vistas/EscanearQR.jsp")
           .forward(req, resp);
    }

    // 2) POST → recibe { "qr": "123" } como JSON y devuelve JSON con success/message
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Lee todo el cuerpo
        String json = new BufferedReader(req.getReader())
                          .lines()
                          .collect(Collectors.joining());

        // Mapea a RequestData
        RequestData requestData = gson.fromJson(json, RequestData.class);
        String qrData = requestData.qr != null ? requestData.qr.trim() : "";

        ResponseData responseData = new ResponseData();

        try {
            int id = Integer.parseInt(qrData);
            Usuarios u = usuarioDAO.buscarPorId(id);

            if (u != null) {
                // Abre la talanquera
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

        // Devuelve JSON
        resp.setContentType("application/json");
        resp.getWriter().write(gson.toJson(responseData));
    }

    // Clase interna para deserializar el request JSON
    private static class RequestData {
        String qr;
    }

    // Clase interna para serializar la respuesta JSON
    private static class ResponseData {
        boolean success;
        String nombre;
        String message;
    }
}
*/



/*package Controlador;

import Modelo.Usuarios;
import ModeloDAO.UsuariosDAO;
import service.ArduinoService;    // tu servicio que abre el puerto serie

import com.google.gson.Gson;      // para respuestas JSON
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;

@WebServlet("/EscanearQR")
public class EscanearQRControlador extends HttpServlet {

    private UsuariosDAO usuarioDAO;
    private ArduinoService arduino;

    @Override
    public void init() throws ServletException {
        usuarioDAO = new UsuariosDAO();
        // Asegúrate de que el puerto COM sea el correcto en tu entorno
        arduino = new ArduinoService("COM11");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // 1. Leer JSON enviado por fetch()
        String json = new BufferedReader(req.getReader())
                          .lines().collect(Collectors.joining());
        String qrData = new Gson().fromJson(json, Request.class).qr;

        Response out = new Response();
        try {
            int id = Integer.parseInt(qrData.trim());
            Usuarios u = usuarioDAO.buscarPorId(id);

            if (u != null) {
                // Abre la talanquera
                arduino.abrirTalanquera();
                out.success = true;
                out.nombre  = u.getNombres();
            } else {
                out.success = false;
                out.message = "Usuario no registrado.";
            }
        } catch (NumberFormatException ex) {
            out.success = false;
            out.message = "QR inválido.";
        } catch (Exception ex) {
            out.success = false;
            out.message = "Error interno: " + ex.getMessage();
        }

        // 2. Responder JSON
        resp.setContentType("application/json");
        resp.getWriter().print(new Gson().toJson(out));
    }

    // Clases auxiliares para (de)serializar JSON
    private static class Request { String qr; }
    private static class Response {
        boolean success;
        String nombre;
        String message;
    }
}*/

