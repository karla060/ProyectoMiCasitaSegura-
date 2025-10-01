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


import Modelo.Pago;
import ModeloDAO.PagoDAO;
import Config.Conexion;
import Modelo.Usuarios;
import ModeloDAO.AuditoriaSistemaDAO;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.annotation.WebServlet;

@WebServlet("/PagoServlet")
public class PagoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/vistas/login.jsp");
            return;
        }

        Usuarios usuario = (Usuarios) session.getAttribute("usuario");
        if (usuario.getIdRol() != 3) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        String mesAPagarStr = "";
        Calendar fechaLimite = Calendar.getInstance(); // Fecha límite para calcular mora (RN5)
        double cantidad = 0; // Cantidad base para el tipo de pago (Mantenimiento por defecto)

        try (Connection con = new Conexion().getConnection()) {
            PagoDAO pagoDAO = new PagoDAO(con);
            AuditoriaSistemaDAO auditoriaDAO = new AuditoriaSistemaDAO();

            // Obtener último pago del usuario
            Pago ultimoPago = null;
            for (Pago p : pagoDAO.listarPagosPorUsuario(usuario.getId())) {
                if (ultimoPago == null || p.getFechaPago().after(ultimoPago.getFechaPago())) {
                    ultimoPago = p;
                }
            }

            // Determinar mes a pagar
            Calendar mesAPagar = Calendar.getInstance();
            if (ultimoPago != null) {
                mesAPagar.setTime(ultimoPago.getFechaPago());
                mesAPagar.add(Calendar.MONTH, 1);
            } else {
                Date fechaCreacion = auditoriaDAO.obtenerFechaCreacionUsuarioPorId(usuario.getId());
                if (fechaCreacion != null) {
                    mesAPagar.setTime(fechaCreacion);
                } else {
                    mesAPagar.setTime(new Date()); // fallback
                }
            }
            mesAPagarStr = new SimpleDateFormat("MMMM yyyy").format(mesAPagar.getTime());

            // Calcular fecha límite (5 del mes correspondiente)
            fechaLimite.setTime(mesAPagar.getTime());
            fechaLimite.set(Calendar.DAY_OF_MONTH, 5);

            // Cantidad base del pago de mantenimiento
            cantidad = 550;

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Enviar atributos al JSP
        request.setAttribute("mesAPagarStr", mesAPagarStr);
        request.setAttribute("fechaLimite", fechaLimite.getTime()); // enviar fecha límite
        request.setAttribute("cantidad", cantidad);

        request.getRequestDispatcher("/vistas/pagoServicio.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/vistas/login.jsp");
            return;
        }

        Usuarios usuario = (Usuarios) session.getAttribute("usuario");
        int idUsuario = usuario.getId();

        String tipoPago = request.getParameter("tipoPago");
        String observaciones = request.getParameter("observaciones");
        String numeroTarjeta = request.getParameter("numeroTarjeta");
        String fechaVenc = request.getParameter("fechaVencimiento");
        String cvv = request.getParameter("cvv");
        String nombreTitular = request.getParameter("nombreTitular");

        // Asignamos monto según tipo de pago
        double cantidad = 0;
        switch (tipoPago) {
            case "Mantenimiento": cantidad = 550; break;
            case "Multa": cantidad = 250; break;
            case "Reinstalación de servicios": cantidad = 750; break;
        }

        try {
            java.util.Date fecha = new SimpleDateFormat("yyyy-MM-dd").parse(fechaVenc);

            double mora = 0;
            double total = cantidad + mora; // Para Mantenimiento, la mora se calculará en el servlet si aplica

            Pago pago = new Pago();
            pago.setIdUsuario(idUsuario);
            pago.setTipoPago(tipoPago);
            pago.setCantidad(cantidad);
            pago.setObservaciones(observaciones);
            pago.setNumeroTarjeta(numeroTarjeta);
            pago.setFechaVencimiento(fecha);
            pago.setCvv(cvv);
            pago.setNombreTitular(nombreTitular);
            pago.setMora(mora);
            pago.setTotal(total);

            try (Connection con = new Conexion().getConnection()) {
                PagoDAO dao = new PagoDAO(con);
                dao.registrarPago(pago);
            }

            response.sendRedirect(request.getContextPath() + "/PagoServlet?success=1");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/PagoServlet?error=1");
        }
    }
}
