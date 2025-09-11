/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controlador;

import Config.Conexion;
import Modelo.Reserva;
import ModeloDAO.ReservaDAO;
import service.EmailService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Time;
import java.util.List;

@WebServlet("/ReservaServlet")
public class ReservaServlet extends HttpServlet {

    private Conexion conexion;

    @Override
    public void init() throws ServletException {
        conexion = new Conexion();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String accion = request.getParameter("accion");

        if (accion == null || accion.equals("listar")) {
            listarReservas(request, response);
        } else if (accion.equals("cancelar")) {
            cancelarReserva(request, response);
        } else if (accion.equals("nueva")) {
            request.getRequestDispatcher("vistas/crear_reserva.jsp").forward(request, response);
        } else {
            listarReservas(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String accion = request.getParameter("accion");

        if ("registrar".equals(accion)) {
            registrarReserva(request, response);
        } else {
            listarReservas(request, response);
        }
    }

    // 📌 Listar reservas
    private void listarReservas(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try (Connection con = conexion.getConnection()) {
            ReservaDAO dao = new ReservaDAO(con);
            List<Reserva> lista = dao.listar();
            request.setAttribute("reservas", lista);
            request.getRequestDispatcher("vistas/gestionar_reservas.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException("Error al listar reservas", e);
        }
    }

    // 📌 Registrar reserva
    private void registrarReserva(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession sesion = request.getSession(false);

        if (sesion == null || sesion.getAttribute("usuarioNombre") == null) {
            response.sendRedirect("vistas/login.jsp");
            return;
        }

        String salon = request.getParameter("salon");
        String fechaStr = request.getParameter("fecha");
        String horaInicioStr = request.getParameter("horaInicio");
        String horaFinStr = request.getParameter("horaFin");

        String residenteNombre = (String) sesion.getAttribute("usuarioNombre");
        String residenteCorreo = (String) sesion.getAttribute("usuarioCorreo");

        try (Connection con = conexion.getConnection()) {
            ReservaDAO dao = new ReservaDAO(con);

            Date fecha = Date.valueOf(fechaStr);
            Time horaInicio = Time.valueOf(horaInicioStr + ":00");
            Time horaFin = Time.valueOf(horaFinStr + ":00");

            // Validar disponibilidad
            if (!dao.estaDisponible(salon, fecha, horaInicio, horaFin)) {
                request.setAttribute("error", "El salón no está disponible en ese horario.");
                request.getRequestDispatcher("vistas/crear_reserva.jsp").forward(request, response);
                return;
            }

            Reserva r = new Reserva();
            r.setSalon(salon);
            r.setResidenteNombre(residenteNombre);
            r.setResidenteCorreo(residenteCorreo);
            r.setFecha(fecha);
            r.setHoraInicio(horaInicio);
            r.setHoraFin(horaFin);
            r.setEstado("activa");

            dao.registrar(r);

            // 📧 Notificación por correo
            if (residenteCorreo != null && !residenteCorreo.trim().isEmpty()) {
                String cuerpo = "Estimado residente, su reserva para el área común "
                        + salon + " ha sido confirmada exitosamente para el día "
                        + fechaStr + " en el horario de "
                        + horaInicioStr + " a " + horaFinStr + ".\n\n"
                        + "Le recordamos revisar las políticas de uso del espacio, "
                        + "respetar los tiempos asignados y notificar con al menos 24 horas de anticipación "
                        + "en caso de cancelación o modificación.\n\n"
                        + "¡Gracias por contribuir a un uso ordenado de nuestros recursos comunitarios!";

                try {
                    EmailService email = new EmailService(
                            "smtp.gmail.com", "587",
                            "patzanpirirjefferson4@gmail.com", "qsym rtfd subg bgee", true
                    );
                    email.enviarCorreo(residenteCorreo, "Notificación de reserva", cuerpo);
                    System.out.println("[EMAIL] Notificación enviada a " + residenteCorreo);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("[EMAIL] Error enviando correo: " + e.getMessage());
                }
            }

            request.setAttribute("msg", "Reserva creada con éxito");
            listarReservas(request, response);

        } catch (Exception e) {
            throw new ServletException("Error al registrar reserva", e);
        }
    }

    // 📌 Cancelar reserva
    private void cancelarReserva(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");

        try (Connection con = conexion.getConnection()) {
            ReservaDAO dao = new ReservaDAO(con);
            dao.cancelar(Integer.parseInt(idStr));
            request.setAttribute("msg", "Reserva cancelada con éxito");
            listarReservas(request, response);
        } catch (Exception e) {
            throw new ServletException("Error al cancelar reserva", e);
        }
    }
}

