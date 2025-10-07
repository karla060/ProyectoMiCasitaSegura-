package Controlador;

import Modelo.Conversacion;
import Modelo.Usuarios;
import ModeloDAO.ConversacionDAO;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import util.SesionHelper;

@WebServlet("/consultaGeneral")
public class ConsultaGeneralServlet extends HttpServlet {
    private ConversacionDAO convDAO = new ConversacionDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Usuarios usuario = SesionHelper.getUsuarioLogueado(request);
        if(usuario == null){
            response.sendRedirect("login.jsp");
            return;
        }

        List<Conversacion> convs = convDAO.listarPorUsuario(usuario.getId(), usuario.getIdRol());

        int convId = 0;
        String convIdParam = request.getParameter("convId");
        if(convIdParam != null) {
            try { convId = Integer.parseInt(convIdParam); } catch(NumberFormatException e){}
        } else if(!convs.isEmpty()) {
            convId = convs.get(0).getId();
        }

        request.setAttribute("conversaciones", convs);
        request.setAttribute("usuarioActual", usuario);
        request.setAttribute("convSeleccionadaId", convId);
        request.getRequestDispatcher("vistas/consultaGeneral.jsp").forward(request,response);
    }
}
