/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controlador;

import Config.Conexion;
import Modelo.Pago;
import Modelo.Usuarios;
import ModeloDAO.PagoDAO;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author mpelv
 */
@WebServlet("/GestionarPagos")
public class GestionarPagosServlet extends HttpServlet {
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

        try (Connection con = new Conexion().getConnection()) {
            PagoDAO dao = new PagoDAO(con);
            List<Pago> pagos = dao.listarPagosPorUsuario(usuario.getId());
            request.setAttribute("pagos", pagos);
        } catch (Exception e) {
            e.printStackTrace();
        }

        request.getRequestDispatcher("/vistas/gestionarPagos.jsp").forward(request, response);
    }
}
