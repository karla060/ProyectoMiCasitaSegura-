/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


package Controlador;

import Modelo.Usuarios;
import ModeloDAO.UsuariosDAO;
import ModeloDAO.AuditoriaSistemaDAO;
import util.SesionHelper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;

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

        String idParam = req.getParameter("id");

        if (idParam != null && !idParam.isEmpty()) {
            try {
                int id = Integer.parseInt(idParam);

                // 🔹 Obtener usuario antes de eliminar
                Usuarios u = usuarioDAO.buscarPorId(id);

                if (u != null) {
                    // 🔹 Eliminar usuario
                    usuarioDAO.eliminar(id);

                    // 🔹 Registrar auditoría
                    Usuarios admin = SesionHelper.getUsuarioLogueado(req);
                    String usuarioAccion = (admin != null)
                            ? admin.getNombres() + " " + admin.getApellidos()
                            : "Sistema";

                    String detalle = "Se eliminó el usuario: " + u.getNombres() + " " + u.getApellidos() +
                            " | DPI=" + u.getDpi() +
                            " | Rol=" + u.getIdRol() +
                            (u.getIdLote() != null ? " | Lote=" + u.getIdLote() : "") +
                            (u.getIdCasa() != null ? " | Casa=" + u.getIdCasa() : "") +
                            " | Activo=" + u.isActivo();

                    new AuditoriaSistemaDAO().registrar(
                            usuarioAccion,
                            "Eliminación de usuario",
                            detalle
                    );
                }

            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        // 🔹 Redirigir al listado con mensaje
        String msg = "Operación completada.";
        resp.sendRedirect("ListarUsuarios?msg=" + URLEncoder.encode(msg, "UTF-8"));
    }
}




/*
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
}*/
