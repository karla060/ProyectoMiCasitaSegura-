/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controlador;

import ModeloDAO.UsuariosDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/EliminarUsuario")
public class EliminarUsuariosControlador extends HttpServlet {

    private UsuariosDAO usuarioDAO;

    @Override
    public void init() {
        usuarioDAO = new UsuariosDAO();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Leer ID enviado por el formulario
        String idParam = req.getParameter("id");
        if (idParam != null) {
            try {
                int id = Integer.parseInt(idParam);
                usuarioDAO.eliminar(id);
            } catch (NumberFormatException ignored) { }
        }
        // Redirigir de vuelta al listado
        resp.sendRedirect("ListarUsuarios");
    }
}
