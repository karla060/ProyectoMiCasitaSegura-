/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controlador;

import Modelo.Usuarios;
import ModeloDAO.AuditoriaSistemaDAO;
import ModeloDAO.CasaDAO;
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
import util.SesionHelper;

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

            CasaDAO casaDAO = new CasaDAO();
            Map<Integer, String> casasMap = new HashMap<>();
            casaDAO.listar().forEach(c ->
             casasMap.put(c.getIdCasa(), String.valueOf(c.getNumeroCasa()))
            );

            req.setAttribute("casasMap", casasMap);

            
            
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
    
    @Override
protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
    
    String idStr = req.getParameter("id");
    boolean ok = false;

    try {
        int id = Integer.parseInt(idStr);

        // 🔹 Obtener usuario antes de eliminar
        Usuarios u = usuarioDAO.buscarPorId(id);

        if (u != null) {
            // 🔹 Eliminar usuario
            usuarioDAO.eliminar(id);

            // 🔹 Registrar auditoría si se eliminó correctamente
            if (ok) {
                Usuarios admin = SesionHelper.getUsuarioLogueado(req);
                String usuarioAccion = (admin != null)
                        ? admin.getNombres() + " " + admin.getApellidos()
                        : "Sistema";

                new AuditoriaSistemaDAO().registrar(
                        usuarioAccion,
                        "Eliminación de usuario",
                        "Se eliminó el usuario: " + u.getNombres() + " " + u.getApellidos() +
                        " | DPI=" + u.getDpi() +
                        " | Rol=" + u.getIdRol() +
                        (u.getIdLote() != null ? " | Lote=" + u.getIdLote() : "") +
                        (u.getIdCasa() != null ? " | Casa=" + u.getIdCasa() : "") +
                        " | Activo=" + u.isActivo()
                );
            }
        }

    } catch (NumberFormatException e) {
        e.printStackTrace();
    }

    String msg = ok ? "Usuario eliminado correctamente." : "No se pudo eliminar el usuario.";
    resp.sendRedirect("ListarUsuarios?msg=" + java.net.URLEncoder.encode(msg, "UTF-8"));
}

    
}

