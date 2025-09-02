/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controlador;

import Modelo.Usuarios;
import ModeloDAO.RolesDAO;
import ModeloDAO.UsuariosDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/ListarUsuarios")
public class ListarUsuariosControlador extends HttpServlet {

    private UsuariosDAO usuarioDAO;
    private RolesDAO rolesDAO;

    @Override
    public void init() throws ServletException {
        usuarioDAO = new UsuariosDAO();
        rolesDAO   = new RolesDAO();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            // 1) Consulta todos los usuarios
            List<Usuarios> usuarios = usuarioDAO.listar();
            
            // 2) Consulta todos los roles y arma un map id→nombre
            Map<Integer,String> rolesMap = new HashMap<>();
            rolesDAO.obtenerTodos().forEach(r ->
                rolesMap.put(r.getId_rol(), r.getNombre_rol())
            );

            // 3) Pasa como atributos de request
            req.setAttribute("usuarios", usuarios);
            req.setAttribute("rolesMap", rolesMap);

            // 4) Forward a JSP
            req.getRequestDispatcher("vistas/ListarUsuarios.jsp")
               .forward(req, resp);

        } catch (SQLException ex) {
            throw new ServletException("Error accediendo a datos", ex);
        }
    }
}

