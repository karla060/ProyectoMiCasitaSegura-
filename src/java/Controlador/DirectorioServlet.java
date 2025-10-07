/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controlador;

import Modelo.Usuarios;
import ModeloDAO.UsuariosDAO;
import Modelo.Lote;
import ModeloDAO.LoteDAO;
import Modelo.Casa;
import ModeloDAO.CasaDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/DirectorioResidencial")
public class DirectorioServlet extends HttpServlet {
    private final UsuariosDAO usuarioDAO = new UsuariosDAO();
    private final LoteDAO    loteDAO    = new LoteDAO();
    private final CasaDAO    casaDAO    = new CasaDAO();

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp)
            throws ServletException, IOException {
        // Carga filtros y muestra formulario vacío
        cargarFiltros(req);
        req.getRequestDispatcher("/vistas/FA10_directorio.jsp")
           .forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        // FA1 – Si pulsó “Limpiar”, redirige a GET para resetear
        if ("limpiar".equals(req.getParameter("accion"))) {
            resp.sendRedirect(req.getContextPath() + "/DirectorioResidencial");
            return;
        }

        // Carga siempre lotes y casas para el form
        cargarFiltros(req);

        //  Leer parámetros de búsqueda
        String nombres   = limpiar(req.getParameter("nombres"));
        String apellidos = limpiar(req.getParameter("apellidos"));
        Integer idLote   = parsearEntero(req.getParameter("idLote"));
        Integer idCasa   = parsearEntero(req.getParameter("idCasa"));

        // FA3 – lote y casa deben ir juntos o ninguno
        if ((idLote == null) ^ (idCasa == null)) {
            req.setAttribute("error",
              "Por favor, seleccione lote y número de casa o ninguno de los dos.");
            req.getRequestDispatcher("/vistas/FA10_directorio.jsp")
               .forward(req, resp);
            return;
        }

        // Nueva validación: al menos un criterio debe estar informado
        if (nombres == null 
         && apellidos == null 
         && idLote   == null 
         && idCasa   == null) {
            req.setAttribute("error",
              "Ingrese al menos un criterio de búsqueda.");
            req.getRequestDispatcher("/vistas/FA10_directorio.jsp")
               .forward(req, resp);
            return;
        }

        // 3.3.5 – Invocar DAO con los filtros
        List<Usuarios> resultados = usuarioDAO.buscarResidentes(
            nombres, apellidos, idLote, idCasa
        );

        // 3.3.6 – Mostrar resultados o mensaje de “no encontrado”
        if (resultados.isEmpty()) {
            req.setAttribute("info",
              "No se encontró ningún usuario con los datos ingresados.");
        } else {
            req.setAttribute("lista", resultados);
        }

        // Forward a la misma JSP para renderizar errores, info y/o tabla
        req.getRequestDispatcher("/vistas/FA10_directorio.jsp")
           .forward(req, resp);
    }

    /**  
     * RN1 – Carga combos de filtros (lotes y casas)  
     */
    private void cargarFiltros(HttpServletRequest req) {
        try {
            List<Lote> lotes = loteDAO.listar();
            List<Casa> casas = casaDAO.listar();
            req.setAttribute("lotes", lotes);
            req.setAttribute("casas", casas);
        } catch (SQLException e) {
            log("Error cargando filtros de DirectorioResidencial", e);
            req.setAttribute("error", "No se pudieron cargar los filtros.");
        }
    }

    /** Convierte cadena vacía o nula en null */
    private String limpiar(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    /** Parsea entero o devuelve null si no es numérico o está vacío */
    private Integer parsearEntero(String s) {
        try {
            return (s == null || s.isEmpty()) 
                   ? null 
                   : Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}