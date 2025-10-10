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
import Modelo.Pago;
import ModeloDAO.PagoDAO;
import ModeloDAO.AuditoriaSistemaDAO;
import ModeloDAO.TipoDePagoDAO;
import Modelo.TipoDePago;
import Config.Conexion;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
            TipoDePagoDAO tipoDAO = new TipoDePagoDAO();

            // ✅ Cargar tipos de pago (catalogo = 3)
            List<TipoDePago> tiposPago = tipoDAO.listar();
            request.setAttribute("tiposPago", tiposPago);

            Calendar mesAPagar = Calendar.getInstance();
            int idCatalogo = 0;

            if (tipoPago != null && !tipoPago.isEmpty()) {
                switch (tipoPago) {
                    case "Mantenimiento":
                        idCatalogo = 77;
                        cantidad = 550;
                        break;
                    case "Multa":
                        idCatalogo = 78;
                        cantidad = 250;
                        break;
                    case "Reinstalación de servicios":
                        idCatalogo = 79;
                        cantidad = 750;
                        break;
                }

                switch (tipoPago) {
                    case "Mantenimiento":
    // Buscar último pago
    Pago ultimoPago = pagoDAO.obtenerUltimoPagoPorCatalogo(usuario.getId(), idCatalogo);
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

            mesAPagarStr = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES")).format(mesAPagar.getTime());
            fechaLimite.setTime(mesAPagar.getTime());
            fechaLimite.set(Calendar.DAY_OF_MONTH, 5);

        } catch (Exception e) {
            e.printStackTrace();
        }

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
    String idCatalogoStr = request.getParameter("idCatalogo");

    int idCatalogo = idCatalogoStr != null && !idCatalogoStr.isEmpty() ? Integer.parseInt(idCatalogoStr) : 0;

    double cantidad = 0;
    switch (idCatalogo) {
        case 77: cantidad = 550; break; // Mantenimiento
        case 78: cantidad = 250; break; // Multa
        case 79: cantidad = 750; break; // Reinstalación de servicios
        default: cantidad = 0; break;
    }

    java.util.Date mesAPagar = null;
    double mora = 0;
    double total = cantidad;

    try (Connection con = new Conexion().getConnection()) {
        PagoDAO dao = new PagoDAO(con);

        if ("Mantenimiento".equals(tipoPago)) {
            Pago ultimoPago = dao.obtenerUltimoPagoPorCatalogo(idUsuario, idCatalogo);
            if (ultimoPago != null && ultimoPago.getMesPagado() != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(ultimoPago.getMesPagado());
                cal.add(Calendar.MONTH, 1);
                mesAPagar = cal.getTime();
            } else {
                AuditoriaSistemaDAO auditoriaDAO = new AuditoriaSistemaDAO();
                Date fechaCreacion = auditoriaDAO.obtenerFechaCreacionUsuarioPorId(idUsuario);
                mesAPagar = (fechaCreacion != null) ? fechaCreacion : new Date();
            }

            // Cálculo de mora compatible con Java 8
            Calendar limite = Calendar.getInstance();
            limite.setTime(mesAPagar);
            limite.set(Calendar.DAY_OF_MONTH, 5);
            Date hoy = new Date();
            if (hoy.after(limite.getTime())) {
                long diff = hoy.getTime() - limite.getTimeInMillis();
                long diasAtraso = (diff / (1000 * 60 * 60 * 24));
                mora = diasAtraso * 25;
                total += mora; // suma correcta de cantidad + mora
            }

        } else if ("Multa".equals(tipoPago)) {
            List<Date> mesesAtrasados = dao.obtenerMesesAtrasados(idUsuario);
            for (Date mes : mesesAtrasados) {
                if (!dao.multaPagada(idUsuario, mes)) {
                    mesAPagar = mes;
                    break;
                }
            }
            if (mesAPagar == null) {
                request.setAttribute("mensajeError", "No tiene multas pendientes por pagar.");
                request.getRequestDispatcher("/vistas/pagoServicio.jsp").forward(request, response);
                return;
            }
            total = cantidad; // Multa sin mora

        } else if ("Reinstalación de servicios".equals(tipoPago)) {
            if (!dao.necesitaReinstalacion(idUsuario)) {
                request.setAttribute("mensajeError", "No tiene reinstalaciones pendientes de servicio.");
                request.getRequestDispatcher("/vistas/pagoServicio.jsp").forward(request, response);
                return;
            }
            idCatalogo = 79;
            cantidad = 750;
            mora = 0;
            total = cantidad;
            mesAPagar = new Date();
        }

        // Validar fecha de vencimiento
        java.util.Date fechaVencDate = null;
        if (fechaVenc != null && !fechaVenc.isEmpty()) {
            fechaVencDate = new SimpleDateFormat("yyyy-MM-dd").parse(fechaVenc);
        }

        // Guardar el pago
        Pago pago = new Pago();
        pago.setIdUsuario(idUsuario);
        pago.setIdCatalogo(idCatalogo);
        pago.setCantidad(cantidad);
        pago.setObservaciones(observaciones);
        pago.setNumeroTarjeta(numeroTarjeta);
        pago.setFechaVencimiento(fechaVencDate);
        pago.setCvv(cvv);
        pago.setNombreTitular(nombreTitular);
        pago.setMora(mora);
        pago.setTotal(total);
        pago.setMesPagado(mesAPagar);

        dao.registrarPago(pago);

        response.sendRedirect(request.getContextPath() + "/GestionarPagos?success=1&idCatalogo=" + idCatalogo);

    } catch (Exception e) {
        e.printStackTrace();
        response.sendRedirect(request.getContextPath() + "/PagoServlet?error=1&tipoPago=" + idCatalogo);
    }
}


}
