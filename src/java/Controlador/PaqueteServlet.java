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


import Modelo.Paquete;
import Modelo.Usuarios;
import ModeloDAO.PaqueteDAO;
import ModeloDAO.UsuariosDAO;
import ModeloDAO.AuditoriaSistemaDAO;
import service.EmailService;
import util.SesionHelper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/PaqueteServlet")
public class PaqueteServlet extends HttpServlet {

    private PaqueteDAO paqueteDAO;
    private UsuariosDAO usuarioDAO;
    private EmailService emailService;
    private AuditoriaSistemaDAO auditoriaDAO;

    @Override
    public void init() throws ServletException {
        paqueteDAO = new PaqueteDAO();
        usuarioDAO = new UsuariosDAO();
        auditoriaDAO = new AuditoriaSistemaDAO();

        // Configuración del correo (igual que en CrearUsuario)
        emailService = new EmailService(
                "smtp.gmail.com", "587",
                "patzanpirirjefferson4@gmail.com",
                "qsym rtfd subg bgee", true
        );
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String accion = req.getParameter("accion");
        if (accion == null) accion = "listar";

        switch (accion) {
            case "listar":
                listar(req, resp);
                break;
            case "entregar":
                marcarEntregado(req, resp);
                break;
            default:
                listar(req, resp);
                break;
        }
    }

    private void listar(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        List<Paquete> listaPaquetes = paqueteDAO.listarPendientes();
        List<Usuarios> listaResidentes = usuarioDAO.listarResidentesActivos();

        req.setAttribute("paquetes", listaPaquetes);
        req.setAttribute("residentes", listaResidentes);
        req.getRequestDispatcher("vistas/paqueteria.jsp").forward(req, resp);
    }

    private void marcarEntregado(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession();

        try {
            int idPaquete = Integer.parseInt(req.getParameter("id"));
            boolean exito = paqueteDAO.marcarEntregado(idPaquete);

            if (exito) {
                // 🔹 Registrar en auditoría
                Usuarios admin = SesionHelper.getUsuarioLogueado(req);
                String usuarioAccion = (admin != null)
                        ? admin.getNombres() + " " + admin.getApellidos()
                        : "Sistema";

                auditoriaDAO.registrar(
                        usuarioAccion,
                        "Entrega de paquete",
                        "Se marcó como entregado el paquete ID=" + idPaquete
                );

                // 🔹 Obtener datos del residente y enviar correo
                Paquete paquete = paqueteDAO.obtenerPorId(idPaquete);
                if (paquete != null) {
                  Usuarios residente = usuarioDAO.buscarPorId(paquete.getIdResidente());
                    if (residente != null && residente.getCorreo() != null && !residente.getCorreo().isEmpty()) {
                        String asunto = " Paquete entregado";
                        String cuerpo = String.format(
                                "Estimado(a) %s %s:%n%n" +
                                "Le informamos que su paquete con número de guía %s ha sido entregado exitosamente.%n%n" +
                                "Saludos cordiales,%nAdministración del residencial.",
                                residente.getNombres(),
                                residente.getApellidos(),
                                paquete.getNumeroGuia()
                        );

                        emailService.enviarCorreo(residente.getCorreo(), asunto, cuerpo);
                        System.out.println(" Correo enviado a " + residente.getCorreo());
                    }
                }

                session.setAttribute("msg", " Paquete marcado como entregado y correo enviado.");
            } else {
                session.setAttribute("msg", " No se pudo marcar el paquete como entregado.");
            }
        } catch (Exception e) {
            session.setAttribute("msg", " Error al procesar entrega: " + e.getMessage());
            e.printStackTrace();
        }

        resp.sendRedirect("PaqueteServlet?accion=listar");
    }

    @Override
protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

    req.setCharacterEncoding("UTF-8");
    HttpSession session = req.getSession();

    String numeroGuia = req.getParameter("numeroGuia");
    String idResidenteStr = req.getParameter("idResidente");

    if (numeroGuia == null || numeroGuia.isEmpty() ||
        idResidenteStr == null || idResidenteStr.isEmpty()) {

        session.setAttribute("msg", "Número de guía y residente son obligatorios.");
        resp.sendRedirect("PaqueteServlet?accion=listar");
        return;
    }

    int idResidente = Integer.parseInt(idResidenteStr);

    // Crear objeto
    Paquete p = new Paquete();
    p.setNumeroGuia(numeroGuia);
    p.setIdResidente(idResidente);
    p.setEntregado(false);

    boolean ok = paqueteDAO.registrar(p);

    if (ok) {
        // Registrar auditoría
        Usuarios admin = SesionHelper.getUsuarioLogueado(req);
        String usuarioAccion = (admin != null)
                ? admin.getNombres() + " " + admin.getApellidos()
                : "Sistema";

        auditoriaDAO.registrar(
                usuarioAccion,
                "Registro de paquete",
                "Número de guía: " + numeroGuia + " | Residente ID=" + idResidente
        );

        // ✅ No enviar correo aquí
        session.setAttribute("msg", "Paquete registrado correctamente.");
    } else {
        session.setAttribute("msg", "Error al registrar el paquete.");
    }

    resp.sendRedirect("PaqueteServlet?accion=listar");
}

}
