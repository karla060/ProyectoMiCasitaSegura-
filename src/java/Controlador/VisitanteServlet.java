/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controlador;

/**
 *
 * @author mpelv
 */


import Modelo.Visitante;
import ModeloDAO.VisitanteDAO;
import util.QRUtils;
import service.EmailService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.ServletOutputStream;

@WebServlet(name = "VisitanteServlet", urlPatterns = {"/VisitanteServlet"})
public class VisitanteServlet extends HttpServlet {

    private final VisitanteDAO dao = new VisitanteDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String accion = Optional.ofNullable(request.getParameter("accion")).orElse("listar");

        switch (accion) {
            case "nuevo": {
                // Formulario de registro
                request.getRequestDispatcher("/vistas/FA01_registro_de_visitantes.jsp").forward(request, response);
                break;
            }
            case "listar": {
                // Carga lista y muestra tabla
                List<Visitante> lista = dao.listar();
                request.setAttribute("lista", lista);
                request.getRequestDispatcher("/vistas/FA00_listar_visitantes.jsp").forward(request, response);
                break;
            }
            case "cancelar": {
    String idStr = request.getParameter("id");
    boolean ok = false;
    try {
        int idCancelar = Integer.parseInt(idStr);
        ok = dao.eliminar(idCancelar); // true si borró 1+
    } catch (Exception e) {
        e.printStackTrace();
    }
    // Lleva un mensaje de estado (opcional)
    String msg = ok ? "Registro cancelado." : "No se pudo cancelar (id inválido o no existe).";
    response.sendRedirect(request.getContextPath() + "/VisitanteServlet?accion=listar&msg=" + java.net.URLEncoder.encode(msg, "UTF-8"));
    break;
}
           /* case "descargarQR": {
                // Si implementaste FA05, aquí iría la descarga (ya te dejé el case antes)
                response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "descargarQR no implementado aquí");
                break;
            }
            default: {
                response.sendRedirect(request.getContextPath() + "/VisitanteServlet?accion=listar");
            }*/
            case "descargarQR": {
    try {
        int id = Integer.parseInt(request.getParameter("id"));
        Visitante v = dao.obtenerPorId(id);
        if (v == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Visitante no encontrado");
            break;
        }

        byte[] png = QRUtils.generarBytes(v, 500, 500); // tamaño ajustable

        String nombreSeguro = v.getNombre() != null ? v.getNombre().replaceAll("[^a-zA-Z0-9]", "_") : "visitante";
        String filename = "QR_" + v.getId() + "_" + nombreSeguro + ".png";

        response.setContentType("image/png");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentLength(png.length);

        try (ServletOutputStream out = response.getOutputStream()) {
            out.write(png);
            out.flush();
        }
    } catch (Exception e) {
        e.printStackTrace();
        request.setAttribute("error", "No se pudo descargar el QR: " + e.getMessage());
        List<Visitante> lista = dao.listar();
        request.setAttribute("lista", lista);
        request.getRequestDispatcher("/vistas/FA00_listar_visitantes.jsp").forward(request, response);
    }
    break;
}

     default:
            response.sendRedirect(request.getContextPath() + "/VisitanteServlet?accion=listar");           
            
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String accion = request.getParameter("accion");
        if ("registrar".equals(accion)) {
            registrarVisitante(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/VisitanteServlet?accion=listar");
        }
    }

    private void registrarVisitante(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        // 1) Parámetros
        String nombre = trim(request.getParameter("nombre"));
        String dpi = trim(request.getParameter("dpi"));
        String tipoVisita = trim(request.getParameter("tipoVisita")); // "Visita" | "Por intentos"
        String correo = trim(request.getParameter("correo"));

        // Normaliza DPI: si permites espacios en el form, guárdalo sin espacios
        if (dpi != null) dpi = dpi.replaceAll("\\s+", "");

        HttpSession session = request.getSession(false);
        String residente = (session != null && session.getAttribute("usuarioNombre") != null)
                ? String.valueOf(session.getAttribute("usuarioNombre"))
                : "Residente";
        String correoResidente = (session != null && session.getAttribute("usuarioCorreo") != null)
                ? String.valueOf(session.getAttribute("usuarioCorreo"))
                : null;

        // 2) Validaciones de negocio
        List<String> errores = new ArrayList<>();
        if (nombre == null || nombre.isEmpty()) errores.add("Nombre del visitante es obligatorio.");
        if (tipoVisita == null || tipoVisita.isEmpty()) errores.add("Tipo de visita es obligatorio.");

        Integer intentos = null;
        Date fechaVisita = null;

        if ("Por intentos".equalsIgnoreCase(tipoVisita)) {
            try {
                intentos = Integer.parseInt(request.getParameter("intentos"));
                if (intentos <= 0) errores.add("Intentos debe al menos 1.");
            } catch (Exception e) {
                errores.add("Intentos inválidos.");
            }
        } else if ("Visita".equalsIgnoreCase(tipoVisita)) {
            String fechaStr = request.getParameter("fechaVisita");
            try {
                if (fechaStr == null || fechaStr.trim().isEmpty()) throw new ParseException("vacío", 0);
                fechaVisita = new SimpleDateFormat("yyyy-MM-dd").parse(fechaStr);
                if (soloFecha(fechaVisita).before(soloFecha(new Date()))) {
                    errores.add("La fecha de visita no puede ser pasada.");
                }
            } catch (Exception e) {
                errores.add("Fecha de visita inválida.");
            }
        }

        if (!errores.isEmpty()) {
            request.setAttribute("error", String.join(" ", errores));
            request.getRequestDispatcher("/vistas/FA01_registro_de_visitantes.jsp").forward(request, response);
            return;
        }

        // 3) Construcción del modelo
        Visitante visitante = new Visitante();
        visitante.setNombre(nombre);
        visitante.setDpi(dpi);
        visitante.setTipoVisita(tipoVisita);
        visitante.setCorreo(correo);
        visitante.setResidente(residente);
        visitante.setIntentos(intentos != null ? intentos : 0);
        visitante.setFechaVisita(fechaVisita);

        // 4) Persistencia + ID generado (manejo robusto)
        int idGenerado;
        try {
            idGenerado = dao.registrarYRetornarId(visitante); // Debe usar RETURN_GENERATED_KEYS y fallback a LAST_INSERT_ID() si es necesario
        } catch (SQLException e) {
            request.setAttribute("error", "No se pudo registrar: " + e.getMessage());
            request.getRequestDispatcher("/vistas/FA01_registro_de_visitantes.jsp").forward(request, response);
            return;
        } catch (Exception e) {
            request.setAttribute("error", "Error inesperado al registrar: " + e.getMessage());
            request.getRequestDispatcher("/vistas/FA01_registro_de_visitantes.jsp").forward(request, response);
            return;
        }

        visitante.setId(idGenerado);
        if (visitante.getId() <= 0) {
            request.setAttribute("error", "Registro creado, pero no se pudo obtener el ID generado.");
            request.getRequestDispatcher("/vistas/FA01_registro_de_visitantes.jsp").forward(request, response);
            return;
        }

       // 5) Generar QR y ENVIAR correo con EmailService existente
try {
    byte[] qrBytes = QRUtils.generarBytes(visitante, 300, 300);

    // Usa el mismo patrón “como antes” (no modificamos EmailService)
    EmailService email = new EmailService(
        "smtp.gmail.com", "587",
        "patzanpirirjefferson4@gmail.com", "qsym rtfd subg bgee",
        true // STARTTLS
    );

    boolean envioHecho = false;
    if (visitante.getCorreo() != null && !visitante.getCorreo().trim().isEmpty()) {
        email.enviarQRVisitante(visitante, qrBytes);
        envioHecho = true;
    }

    // Solo un mensaje: éxito de registro y, si aplica, confirmación de envío
    if (envioHecho) {
        request.setAttribute("visitanteCreado", "Registro exitoso. El código QR fue enviado por correo.");
    } else {
        request.setAttribute("visitanteCreado", "Registro exitoso. (No se envió correo porque no se proporcionó destinatario).");
    }

} catch (Exception e) {
    // Registro ya creado; muestra SOLO el error de correo/QR y termina
    request.setAttribute("error", "Registro creado, pero hubo un problema con QR/correos: " + e.getMessage());
    request.getRequestDispatcher("/vistas/FA01_registro_de_visitantes.jsp").forward(request, response);
    return;
}

// 6) Éxito: queda un único mensaje en pantalla
request.getRequestDispatcher("/vistas/FA01_registro_de_visitantes.jsp").forward(request, response);

    }

    private static String trim(String s) { return s == null ? null : s.trim(); }

    private static Date soloFecha(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }
}