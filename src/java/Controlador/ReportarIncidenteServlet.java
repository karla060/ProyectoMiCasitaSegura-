/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controlador;

import Config.Conexion;
import ModeloDAO.IncidenteDAO;
import Modelo.Incidente;
import Modelo.Usuarios;
import ModeloDAO.UsuariosDAO;
import service.EmailService;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@WebServlet("/ReportarIncidente")
public class ReportarIncidenteServlet extends HttpServlet {

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
        // Cargar tipos de incidentes
        try {
            IncidenteDAO dao = new IncidenteDAO();
            List<String> tipos = dao.listarTipos();
            request.setAttribute("tipos", tipos);
        } catch (Exception e) {
            e.printStackTrace();
        }

        request.getRequestDispatcher("vistas/reportarIncidente.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession sesion = request.getSession(false);
        if (sesion == null || sesion.getAttribute("usuarioId") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        int residenteId = (int) sesion.getAttribute("usuarioId");
        String tipo = request.getParameter("tipo");
        String fechaHoraStr = request.getParameter("fechaHora");
        String descripcion = request.getParameter("descripcion");

        if (tipo == null || tipo.isEmpty() ||
            fechaHoraStr == null || fechaHoraStr.isEmpty() ||
            descripcion == null || descripcion.isEmpty()) {
            request.setAttribute("error", "Todos los campos son obligatorios.");
            doGet(request, response);
            return;
        }

        try {
       // Parsear la fecha que viene del input (sin zona)
    LocalDateTime ldt = LocalDateTime.parse(fechaHoraStr);

    // Aplicar zona horaria del sistema
    ZonedDateTime zdt = ldt.atZone(ZoneId.systemDefault());

    // Restar 6 horas (ajuste manual UTC-6)
    ZonedDateTime ajustado = zdt.minusHours(6);

    // Convertir a Date para tu DAO
    Date fechaHora = Date.from(ajustado.toInstant());

            Incidente incidente = new Incidente();
            incidente.setResidenteId(residenteId);
            incidente.setTipo(tipo);
            incidente.setFechaHora(fechaHora);
            incidente.setDescripcion(descripcion);

            IncidenteDAO dao = new IncidenteDAO();
            int idIncidente = dao.guardar(incidente);

            // Enviar notificación a los guardias activos
            Usuarios usuario = (Usuarios) sesion.getAttribute("usuario");
            //EmailService emailService = (EmailService) getServletContext().getAttribute("emailService");
            if(emailService != null){
                // Obtener correos de guardias desde tu DAO de usuarios
                List<String> correosGuardias = new UsuariosDAO().obtenerCorreosAgentes();
                String asunto = "Reporte de incidente";
                // Formatear la fecha para mostrarla bien en el correo
        DateTimeFormatter formatterCorreo = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
        String fechaFormateada = ajustado.format(formatterCorreo);
        // Sumamos 6 horas solo para mostrar en correo
        String fechaFormateada2 = ldt.plusHours(0).format(formatterCorreo);
        // Obtener el usuario logueado desde sesión

// Si tiene casa asignada, buscar el nombre de la casa en el catálogo
if (usuario != null && usuario.getIdCasa() != null) {
    String sqlCasa = "SELECT nombre FROM catalogos WHERE id = ?";
    try (Connection con = new Conexion().getConnection();
         PreparedStatement ps = con.prepareStatement(sqlCasa)) {
        ps.setInt(1, usuario.getIdCasa());
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                usuario.setNumeroCasa(rs.getString("nombre"));
            }
        }
    } catch (SQLException e) {
        System.err.println("Error obteniendo número de casa: " + e.getMessage());
    }
}

                String cuerpo = "Se le informa que el residente " + usuario.getNombres() +
                        ", que vive en casa " + usuario.getNumeroCasa() + ", ha reportado un incidente, a continuación el detalle:\n\n" +
                        tipo + "\n" +
                        fechaFormateada2 + "\n" +
                        descripcion + "\n\n" +
                        "Por favor, tomar las acciones correspondientes.";
                emailService.enviarCorreoAgentes(correosGuardias, asunto, cuerpo);
            }

            // Mostrar mensaje de éxito y redirigir a Comunicación Interna
            request.getSession().setAttribute("exito", "Se ha creado el incidente con éxito.");
            response.sendRedirect(request.getContextPath() + "/vistas/comunicacionInterna.jsp");


        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error al guardar el incidente.");
            doGet(request, response);
        }
    }
}

