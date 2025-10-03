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
import java.util.List;
import java.sql.SQLException;

import javax.servlet.annotation.WebServlet;


@WebServlet("/PagoServlet")
public class PagoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

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

        String tipoPago = request.getParameter("tipoPago"); 
        String mesAPagarStr = "";
        Calendar fechaLimite = Calendar.getInstance();
        double cantidad = 0;

        try (Connection con = new Conexion().getConnection()) {
            PagoDAO pagoDAO = new PagoDAO(con);
            AuditoriaSistemaDAO auditoriaDAO = new AuditoriaSistemaDAO();

            Calendar mesAPagar = Calendar.getInstance();

            if (tipoPago != null && !tipoPago.isEmpty()) {
                switch (tipoPago) {
                   case "Mantenimiento":
    // Buscar último pago
    Pago ultimoPago = pagoDAO.obtenerUltimoPagoPorTipo(usuario.getId(), tipoPago);
    if (ultimoPago != null && ultimoPago.getMesPagado() != null) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(ultimoPago.getMesPagado());
        cal.add(Calendar.MONTH, 1);
        mesAPagar.setTime(cal.getTime());
    } else {
        Date fechaCreacion = auditoriaDAO.obtenerFechaCreacionUsuarioPorId(usuario.getId());
        mesAPagar.setTime(fechaCreacion != null ? fechaCreacion : new Date());
    }
    cantidad = 550;

    // Calcular mora si está atrasado
    Calendar hoy = Calendar.getInstance();
    if(hoy.after(mesAPagar)) {
        long diff = hoy.getTimeInMillis() - mesAPagar.getTimeInMillis();
        long diasRetraso = (diff / (1000*60*60*24));
        // incluir hoy como día de atraso
        diasRetraso++;
        double mora = diasRetraso * 25;
        request.setAttribute("mora", mora);
    } else {
        request.setAttribute("mora", 0.0);
    }
    break;


     case "Multa":
    // Buscar meses atrasados
    List<Date> mesesAtrasados = pagoDAO.obtenerMesesAtrasados(usuario.getId());
    Date mesMultaPendiente = null;

    // Revisar si la multa de ese mes ya fue pagada
  for (Date mes : mesesAtrasados) {
    if (!pagoDAO.multaPagada(usuario.getId(), new java.sql.Date(mes.getTime()))) {
        mesMultaPendiente = mes;
        break;
    }
}


    if (mesMultaPendiente == null) {
        request.setAttribute("mensaje", "No tienes multas pendientes por pagar.");
        request.getRequestDispatcher("/vistas/pagoServicio.jsp").forward(request, response);
        return;
    }

    cantidad = 250;
    mesAPagar.setTime(mesMultaPendiente); // la multa corresponde al mes atrasado
    // Calcular mora según días de retraso
   
    break;



   case "Reinstalación de servicios":
    try {
        // Verificar si requiere reinstalación (>=3 meses de mantenimiento sin pagar)
        if (!pagoDAO.necesitaReinstalacion(usuario.getId())) {
            request.setAttribute("mensaje", 
                "No tienes reinstalaciones de servicio pendientes (se requieren al menos 3 meses sin pagar mantenimiento).");
            request.getRequestDispatcher("/vistas/pagoServicio.jsp").forward(request, response);
            return;
        }
    } catch (SQLException e) {
        e.printStackTrace();
        request.setAttribute("mensaje", "Error al verificar reinstalación de servicios.");
        request.getRequestDispatcher("/vistas/pagoServicio.jsp").forward(request, response);
        return;
    }

    cantidad = 750;
    mesAPagar.setTime(new Date());
    break;

                }
            } else {
                mesAPagar.setTime(new Date());
            }

            mesAPagarStr = new SimpleDateFormat("MMMM yyyy").format(mesAPagar.getTime());

            // Fecha límite de pago para calcular mora (día 5 del mes)
            fechaLimite.setTime(mesAPagar.getTime());
            fechaLimite.set(Calendar.DAY_OF_MONTH, 5);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Atributos para JSP
        request.setAttribute("tipoPago", tipoPago);
        request.setAttribute("mesAPagarStr", mesAPagarStr);
        request.setAttribute("fechaLimite", fechaLimite.getTime());
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
        String mesAPagarStr = request.getParameter("mesAPagar");

        double cantidad = 0;
        switch (tipoPago) {
            case "Mantenimiento": cantidad = 550; break;
            case "Multa": cantidad = 250; break;
            case "Reinstalación de servicios": cantidad = 750; break;
        }

        double mora = 0;
        double total = cantidad;

        try (Connection con = new Conexion().getConnection()) {
            PagoDAO dao = new PagoDAO(con);
            
             java.util.Date mesAPagar = null;
           if ("Multa".equals(tipoPago)) {
            // Buscar meses atrasados
            List<Date> mesesAtrasados = dao.obtenerMesesAtrasados(idUsuario);
            Date mesMultaPendiente = null;

            for (Date mes : mesesAtrasados) {
                if (!dao.multaPagada(idUsuario, new java.sql.Date(mes.getTime()))) {
                    mesMultaPendiente = mes;
                    break;
                }
            }

            if (mesMultaPendiente == null) {
                //  Bloquear pago si ya se pagaron todas las multas
                request.setAttribute("mensajeError", "No tiene multas pendientes por pagar.");
                request.getRequestDispatcher("/vistas/pagoServicio.jsp").forward(request, response);
                return;
            }

            cantidad = 250;
            mesAPagar = mesMultaPendiente; // La multa corresponde al mes atrasado
        }

            if ("Reinstalación de servicios".equals(tipoPago)) {
                if (!dao.necesitaReinstalacion(idUsuario)) {
                    request.setAttribute("mensajeError", "No tiene reinstalaciones pendientes de servicio.");
                    request.getRequestDispatcher("/vistas/pagoServicio.jsp").forward(request, response);
                    return;
                }
            }

            // Convertir fechas
            java.util.Date fechaVencDate = new SimpleDateFormat("yyyy-MM-dd").parse(fechaVenc);
            mesAPagar = null;
            
            
            if ("Mantenimiento".equals(tipoPago) || "Multa".equals(tipoPago)) {
                if (mesAPagarStr != null && !mesAPagarStr.isEmpty()) {
                    mesAPagar = new SimpleDateFormat("yyyy-MM-dd").parse(mesAPagarStr);
                }
            }

            // Calcular mora si es mantenimiento
            if ("Mantenimiento".equals(tipoPago)) {
                Calendar limite = Calendar.getInstance();
                limite.setTime(mesAPagar);
                limite.set(Calendar.DAY_OF_MONTH, 5); // día límite
                Date hoy = new Date();
                if (hoy.after(limite.getTime())) {
                    long diff = hoy.getTime() - limite.getTimeInMillis();
                    long diasAtraso = (diff / (1000*60*60*24)); // incluir hoy
                    mora = diasAtraso * 25; // Q25 por día
                    total += mora;
                }
            }

            // Crear objeto Pago
            Pago pago = new Pago();
            pago.setIdUsuario(idUsuario);
            pago.setTipoPago(tipoPago);
            pago.setCantidad(cantidad);
            pago.setObservaciones(observaciones);
            pago.setNumeroTarjeta(numeroTarjeta);
            pago.setFechaVencimiento(fechaVencDate);
            pago.setCvv(cvv);
            pago.setNombreTitular(nombreTitular);
            pago.setMora(mora);
            pago.setTotal(total);
            pago.setMesPagado(mesAPagar);

            // Guardar pago
            dao.registrarPago(pago);

            response.sendRedirect(request.getContextPath() + "/PagoServlet?success=1&tipoPago=" + tipoPago);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/PagoServlet?error=1&tipoPago=" + tipoPago);
        }
    }
}