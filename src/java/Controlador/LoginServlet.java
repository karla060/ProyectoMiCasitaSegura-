/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controlador;

import Modelo.Usuarios;
import ModeloDAO.UsuariosDAO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/Login")
public class LoginServlet extends HttpServlet {

    private UsuariosDAO usuariosDAO = new UsuariosDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String correo = request.getParameter("correo");
        String contrasena = request.getParameter("contrasena");

        Usuarios usuario = validarLogin(correo, contrasena);

        if (usuario != null && usuario.isActivo()) {
            HttpSession sesion = request.getSession();
            sesion.setAttribute("usuario", usuario);
            response.sendRedirect(request.getContextPath() + "/index.jsp");
        } else {
            request.setAttribute("error", "Usuario o contraseña incorrectos, o cuenta inactiva.");
            request.getRequestDispatcher("vistas/login.jsp").forward(request, response);
        }
    }

    private Usuarios validarLogin(String correo, String contrasena) {
        for (Usuarios u : usuariosDAO.listar()) {
            if (u.getCorreo().equalsIgnoreCase(correo) &&
                u.getContrasena().equals(contrasena)) {
                return u;
            }
        }
        return null;
    }
}
   
