/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Controlador;

import ModeloDAO.ReporteDAO;
import ModeloDAO.UsuariosDAO;
import Modelo.ReporteMantenimiento;
import service.EmailService;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.mail.MessagingException;

@WebServlet("/ReporteMantenimientoServlet")
public class ReporteMantenimientoServlet extends HttpServlet {

    private EmailService emailService;

    @Override
    public void init() throws ServletException {
        // Inicializa EmailService con tu configuración SMTP
        emailService = new EmailService(
                "smtp.gmail.com", "587",
                "patzanpirirjefferson4@gmail.com", "qsym rtfd subg bgee",
                true
        );
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Muestra el formulario
        request.getRequestDispatcher("vistas/reporteMantenimiento.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            String tipo = request.getParameter("tipoInconveniente");
            String descripcion = request.getParameter("descripcion");
            String fechaHoraStr = request.getParameter("fechaHora");

            // Parsear fecha
            Date fechaHora;
            if (fechaHoraStr == null || fechaHoraStr.isEmpty()) {
                fechaHora = new Date();
            } else {
                try {
                    fechaHora = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(fechaHoraStr);
                } catch (Exception e) {
                    fechaHora = new Date();
                }
            }

            // Obtener sesión
            HttpSession session = request.getSession();
            Integer idUsuario = (Integer) session.getAttribute("usuarioId");
            String nombreResidente = (String) session.getAttribute("usuarioNombre");

            if (idUsuario == null || nombreResidente == null) {
                throw new ServletException("Sesión de usuario inválida.");
            }

            // Guardar reporte
            ReporteMantenimiento reporte = new ReporteMantenimiento();
            reporte.setTipoInconveniente(tipo);
            reporte.setDescripcion(descripcion);
            reporte.setFechaHora(fechaHora);
            reporte.setIdUsuario(idUsuario);
            
            
            
            // DAO maneja la conexión interna
            new ReporteDAO().guardar(reporte);

            // Obtener correos de admins
            List<String> correosAdmins = new UsuariosDAO().obtenerCorreosAdmins();

            // Crear el formato deseado (por ejemplo: dd/MM/yyyy HH:mm)
             SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            // Formatear la fecha del reporte
            String fechaFormateada = formatoFecha.format(fechaHora);
            
            
            // Enviar correo a todos los admins si hay alguno
            if (correosAdmins != null && !correosAdmins.isEmpty()) {
                String asunto = "Reporte de mantenimiento";
                String cuerpo = "El residente " + nombreResidente + " ha ingresado un reporte de error del sistema, el detalle de reporte es:\n\n" +
                        "Tipo de inconveniente: " + tipo + "\n" +
                        "Descripción: " + descripcion + "\n" +
                        "Fecha y hora de incidente: " + fechaFormateada + "\n" +
                        "Por favor, tomar las acciones correspondientes.";

                emailService.enviarCorreoAdmins(correosAdmins, asunto, cuerpo);
            }

            request.setAttribute("mensaje", "Reporte enviado correctamente.");
            request.getRequestDispatcher("vistas/reporteMantenimiento.jsp").forward(request, response);

        } catch (MessagingException me) {
            throw new ServletException("Error enviando correo: " + me.getMessage(), me);
        } catch (Exception e) {
            throw new ServletException("Error procesando reporte: " + e.getMessage(), e);
        }
    }
}



/*
package Controlador;

import ModeloDAO.ReporteDAO;
import ModeloDAO.UsuariosDAO;
import Modelo.ReporteMantenimiento;
import service.EmailService;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import javax.mail.MessagingException;
import javax.servlet.annotation.WebServlet;

@WebServlet("/ReporteMantenimientoServlet")
public class ReporteMantenimientoServlet extends HttpServlet {
    private Connection con;
    

    @Override
    public void init() throws ServletException {
        con = (Connection) getServletContext().getAttribute("conexion");
    }

    
    @Override
protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    // Simplemente muestra el JSP con el formulario
    request.getRequestDispatcher("vistas/reporteMantenimiento.jsp").forward(request, response);
}

  
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String tipo = request.getParameter("tipoInconveniente");
            String descripcion = request.getParameter("descripcion");
          //  String fechaHoraStr = request.getParameter("fechaHora");
           // Date fechaHora = new Date(); // podrías parsear si viene del form

           String fechaHoraStr = request.getParameter("fechaHora");
Date fechaHora;

if (fechaHoraStr == null || fechaHoraStr.isEmpty()) {
    fechaHora = new Date(); // fecha actual si no se envía
} else {
    try {
        fechaHora = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(fechaHoraStr);
    } catch (Exception e) {
        fechaHora = new Date(); // fallback a fecha actual
    }
}
           
           
           
            int idUsuario = (int) request.getSession().getAttribute("usuarioId");
            String nombreResidente = (String) request.getSession().getAttribute("usuarioNombre");

            // Guardar en BD
            ReporteMantenimiento reporte = new ReporteMantenimiento();
            reporte.setTipoInconveniente(tipo);
            reporte.setDescripcion(descripcion);
            reporte.setFechaHora(fechaHora);
            reporte.setIdUsuario(idUsuario);

            new ReporteDAO(con).guardar(reporte);

            // Obtener correos de admins
            UsuariosDAO usuarioDAO = new UsuariosDAO(con);
            List<String> correosAdmins = usuarioDAO.obtenerCorreosAdmins();

            // Enviar correo a cada admin
            
            for (String correo : correosAdmins) {
    String asunto = "Reporte de mantenimiento";
    String cuerpo = "El residente " + nombreResidente + " ha ingresado un reporte de error del sistema.\n\n" +
                    "Tipo de inconveniente: " + tipo + "\n" +
                    "Descripción: " + descripcion + "\n" +
                    "Fecha y hora: " + fechaHora + "\n\n" +
                    "Por favor, tomar las acciones correspondientes.";

    try {
        EmailService.enviarCorreoAdmins(correo, asunto, cuerpo);
        System.out.println("Correo enviado a " + correo);
    } catch (MessagingException e) {
        e.printStackTrace();
    }
}

           /* for (String correo : correosAdmins) {
                String asunto = "Reporte de mantenimiento";
                String cuerpo = "El residente " + nombreResidente + " ha ingresado un reporte de error del sistema.\n\n" +
                                "Tipo de inconveniente: " + tipo + "\n" +
                                "Descripción: " + descripcion + "\n" +
                                "Fecha y hora: " + fechaHora + "\n\n" +
                                "Por favor, tomar las acciones correspondientes.";
                EmailService.enviarCorreoAdmins(correo, asunto, cuerpo);
            }
            

            request.setAttribute("mensaje", "Reporte enviado");
            request.getRequestDispatcher("vistas/reporteMantenimiento.jsp").forward(request, response);

        } catch (Exception e) {
            throw new ServletException("Error procesando reporte: " + e.getMessage(), e);
        }
    }
}
*/