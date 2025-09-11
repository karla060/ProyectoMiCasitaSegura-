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


import Modelo.Usuarios;
import Modelo.Visitante;
import ModeloDAO.AuditoriaSistemaDAO;
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
import util.SesionHelper;


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

        // 🔹 Obtener datos antes de eliminar
        Visitante visitanteCancelado = dao.obtenerPorId(idCancelar);

        // 🔹 Eliminar visitante
        ok = dao.eliminar(idCancelar);

        // 🔹 Registrar auditoría si la eliminación fue exitosa
        if (ok && visitanteCancelado != null) {
            Usuarios admin = SesionHelper.getUsuarioLogueado(request);
            String usuarioAccion = (admin != null)
                    ? admin.getNombres() + " " + admin.getApellidos()
                    : "Sistema";

            String detalle = "Se canceló la visita: " + visitanteCancelado.getNombre() +
                             " | DPI=" + visitanteCancelado.getDpi() +
                             " | Tipo de visita=" + visitanteCancelado.getTipoVisita() +
                             (visitanteCancelado.getTipoVisita().equalsIgnoreCase("Por intentos")
                                 ? " | Intentos=" + visitanteCancelado.getIntentos()
                                 : " | Fecha de visita=" + new SimpleDateFormat("yyyy-MM-dd")
                                     .format(visitanteCancelado.getFechaVisita())
                             ) +
                             (visitanteCancelado.getCorreo() != null
                                 ? " | Correo=" + visitanteCancelado.getCorreo() : "") +
                             (visitanteCancelado.getResidente() != null
                                 ? " | Residente=" + visitanteCancelado.getResidente() : "");

            new AuditoriaSistemaDAO().registrar(usuarioAccion, "Cancelación de visita", detalle);
        }

    } catch (NumberFormatException e) {
        e.printStackTrace();
    }

    // Lleva un mensaje de estado (opcional)
    String msg = ok ? "Registro cancelado." : "No se pudo cancelar (id inválido o no existe).";
    response.sendRedirect(request.getContextPath() + "/VisitanteServlet?accion=listar&msg=" +
                          java.net.URLEncoder.encode(msg, "UTF-8"));
    break;
}

          /*  case "cancelar": {
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
}*/
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


   private void registrarVisitante(HttpServletRequest request,
                                HttpServletResponse response)
        throws ServletException, IOException {
    // Asegura la codificación antes de leer parámetros
    request.setCharacterEncoding("UTF-8");

    // 1) Lectura de parámetros
    String nombre     = trim(request.getParameter("nombre"));
    String dpi        = trim(request.getParameter("dpi"));
    String tipoVisita = trim(request.getParameter("tipoVisita"));
    String correo     = trim(request.getParameter("correo"));

    // Normaliza DPI: elimina espacios
    if (dpi != null) {
        dpi = dpi.replaceAll("\\s+", "");
    }

    // 2) Validaciones de negocio
    List<String> errores = new ArrayList<>();
    if (nombre == null || nombre.isEmpty()) {
        errores.add("Nombre del visitante es obligatorio.");
    }
    if (tipoVisita == null || tipoVisita.isEmpty()) {
        errores.add("Tipo de visita es obligatorio.");
    }

    Integer intentos  = null;
    Date    fechaVisita = null;
    if ("Por intentos".equalsIgnoreCase(tipoVisita)) {
        try {
            intentos = Integer.parseInt(request.getParameter("intentos"));
            if (intentos <= 0) {
                errores.add("Intentos debe ser al menos 1.");
            }
        } catch (NumberFormatException e) {
            errores.add("Intentos inválidos.");
        }
    } else if ("Visita".equalsIgnoreCase(tipoVisita)) {
        String fechaStr = request.getParameter("fechaVisita");
        try {
            if (fechaStr == null || fechaStr.trim().isEmpty()) {
                throw new ParseException("vacío", 0);
            }
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
        request.getRequestDispatcher("/vistas/FA01_registro_de_visitantes.jsp")
               .forward(request, response);
        return;
    }

    // 3) Construcción del modelo Visitante
    Visitante visitante = new Visitante();
    visitante.setNombre(nombre);
    visitante.setDpi(dpi);
    visitante.setTipoVisita(tipoVisita);
    visitante.setCorreo(correo);
    visitante.setIntentos(intentos != null ? intentos : 0);
    visitante.setFechaVisita(fechaVisita);
    
    // Recuperar correo del residente autenticado
    HttpSession session = request.getSession(false);
    String correoResidente = null;
    if (session != null) {
    Usuarios usuarioSes = (Usuarios) session.getAttribute("usuario");
    if (usuarioSes != null) {
        correoResidente = usuarioSes.getCorreo();
    }
}

// Asignar el residente en el objeto Visitante
visitante.setResidente(correoResidente);


    // 4) Persistencia y captura de ID generado
    try {
        int idGenerado = dao.registrarYRetornarId(visitante);
        visitante.setId(idGenerado);
        if (visitante.getId() <= 0) {
            throw new SQLException("No se obtuvo ID generado");
        }
    } catch (Exception e) {
        request.setAttribute("error", "No se pudo registrar: " + e.getMessage());
        request.getRequestDispatcher("/vistas/FA01_registro_de_visitantes.jsp")
               .forward(request, response);
        return;
    }
    
    
    
    // 🔹 Obtener admin en sesión
Usuarios admin = SesionHelper.getUsuarioLogueado(request);
String usuarioAccion = (admin != null)
        ? admin.getNombres() + " " + admin.getApellidos()
        : "Sistema";

// 🔹 Registrar en auditoría la creación del visitante
new AuditoriaSistemaDAO().registrar(
    usuarioAccion,
    "Creación de visitante",
    "Se registró el visitante: " + visitante.getNombre() +
    " | DPI=" + visitante.getDpi() +
    " | Tipo de visita=" + visitante.getTipoVisita() +
    (visitante.getTipoVisita().equalsIgnoreCase("Por intentos")
        ? " | Intentos=" + visitante.getIntentos()
        : " | Fecha de visita=" + new SimpleDateFormat("yyyy-MM-dd").format(visitante.getFechaVisita())
    ) +
    (visitante.getCorreo() != null ? " | Correo=" + visitante.getCorreo() : "") +
    (visitante.getResidente() != null ? " | Residente=" + visitante.getResidente() : "")
);

    

    // 6) Generar QR y enviar correos
    try {
        byte[] qrBytes = QRUtils.generarBytes(visitante, 300, 300);
       EmailService email = new EmailService( "smtp.gmail.com", "587", 
               "patzanpirirjefferson4@gmail.com", 
               "qsym rtfd subg bgee", true );
        

        // 6.1) Envía QR al visitante (si tiene correo)
        if (visitante.getCorreo() != null && !visitante.getCorreo().isEmpty()) {
            email.enviarQRVisitante(visitante, qrBytes);
        }

        // 6.2) Envía confirmación al residente (sin adjunto QR)
        if (correoResidente != null && !correoResidente.isEmpty()) {
            email.enviarConfirmacionResidente(visitante, correoResidente);
        }

        request.setAttribute("visitanteCreado",
            "Registro exitoso. El código QR fue enviado por correo.");

    } catch (Exception e) {
        request.setAttribute("error",
            "Registro creado, pero falló el envío de correos: " + e.getMessage());
        request.getRequestDispatcher("/vistas/FA01_registro_de_visitantes.jsp")
               .forward(request, response);
        return;
    }

    // 7) Forward final al JSP con mensaje de éxito
    request.getRequestDispatcher("/vistas/FA01_registro_de_visitantes.jsp")
           .forward(request, response);
}

   
// Método helper para truncar hora de Date
private static Date soloFecha(Date d) {
    Calendar c = Calendar.getInstance();
    c.setTime(d);
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);
    return c.getTime();
}

// Método helper para trim seguro
private static String trim(String s) {
    return (s == null) ? null : s.trim();
}
}