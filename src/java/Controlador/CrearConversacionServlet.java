package Controlador;



import Modelo.Usuarios;
import ModeloDAO.ConversacionDAO;
import ModeloDAO.UsuariosDAO;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/crearConversacion")
public class CrearConversacionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Mostrar lista de agentes activos
        UsuariosDAO usuarioDAO = new UsuariosDAO();
        List<Usuarios> agentesActivos = usuarioDAO.listarAgentesActivos();
        request.setAttribute("agentes", agentesActivos);
        request.getRequestDispatcher("vistas/crearConversacion.jsp").forward(request, response);
    }

@Override
protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

    // Validar sesión
    HttpSession sesion = request.getSession(false);
    if (sesion == null || sesion.getAttribute("usuarioId") == null) {
        response.sendRedirect("vistas/login.jsp");
        return;
    }

    String idAgenteStr = request.getParameter("idAgente");
    if (idAgenteStr == null || idAgenteStr.isEmpty()) {
        request.setAttribute("error", "Debe seleccionar un agente antes de guardar.");
        request.getRequestDispatcher("vistas/crearConversacion.jsp").forward(request, response);
        return;
    }

    int idResidente = (int) sesion.getAttribute("usuarioId");
    int idAgente = Integer.parseInt(idAgenteStr);

    ConversacionDAO dao = new ConversacionDAO();
    
    // ConversacionDAO dao = new ConversacionDAO();  // ya existente
if (dao.existeConversacion(idResidente, idAgente)) {
    // Si ya existe, mostramos error y volvemos al JSP
    request.setAttribute("error", "Ya existe una conversacion con el usuario seleccionado.");

    // Recargar la lista de agentes activos para el JSP
    UsuariosDAO usuarioDAO = new UsuariosDAO();
    List<Usuarios> agentesActivos = usuarioDAO.listarAgentesActivos();
    request.setAttribute("agentes", agentesActivos);

    // Volver al JSP de crear conversación
    request.getRequestDispatcher("vistas/crearConversacion.jsp").forward(request, response);
    return;
}

    // Si no existe, crear normalmente
        dao.crearConversacion(idResidente, idAgente);
        response.sendRedirect("consultaGeneral?convId=" + idAgente);

}

}
