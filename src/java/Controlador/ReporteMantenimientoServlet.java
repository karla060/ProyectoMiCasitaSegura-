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
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
            // Cargar tipos de inconvenientes desde el catálogo
        ReporteDAO dao = new ReporteDAO();
        List<String> tiposInconvenientes = new ArrayList<>();

        try {
            tiposInconvenientes = dao.listarTiposInconvenientes();
        } catch (SQLException ex) {
            Logger.getLogger(ReporteMantenimientoServlet.class.getName()).log(Level.SEVERE, null, ex);
        }

        request.setAttribute("tiposInconvenientes", tiposInconvenientes);


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

