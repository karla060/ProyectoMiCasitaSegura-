/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controlador;

import Modelo.Usuarios;
import ModeloDAO.UsuariosDAO;
import service.EmailService;
import util.QRUtils;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/CrearUsuario")
public class FA1_CrearUsuarioControlador extends HttpServlet {

    private UsuariosDAO usuarioDAO;
    private EmailService emailService;

    
    @Override
    public void init() throws ServletException {
        // Inicializamos DAO y servicio de correo con mismas credenciales
        usuarioDAO = new UsuariosDAO();
        emailService = new EmailService(
            "smtp.gmail.com", "587",
            "patzanpirirjefferson4@gmail.com", 
            "qsym rtfd subg bgee", true
        );
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Cargar datos para combos: Roles, Lotes, Casas
        try {
            req.setAttribute("roles", new ModeloDAO.RolesDAO().obtenerTodos());
            req.setAttribute("lotes", new ModeloDAO.LoteDAO().listar());
            req.setAttribute("casas", new ModeloDAO.CasaDAO().listar());
        } catch (Exception e) {
            throw new ServletException("Error cargando datos", e);
        }
        // Mostrar formulario JSP
        req.getRequestDispatcher("vistas/FA1_CrearUsuario.jsp")
           .forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1) Leer parámetros
        String dpi       = req.getParameter("dpi");
        String nombres   = req.getParameter("nombres");
        String apellidos = req.getParameter("apellidos");
        String correo    = req.getParameter("correo");
        String pass      = req.getParameter("contrasena");
        int idRol        = Integer.parseInt(req.getParameter("idRol"));
        String loteParam = req.getParameter("idLote");
        String casaParam = req.getParameter("idCasa");
        boolean activo   = req.getParameter("activo") != null;

        // 2) Validaciones mínimas
        if (dpi.isEmpty() || nombres.isEmpty() || apellidos.isEmpty()) {
            req.setAttribute("error", "DPI, Nombres y Apellidos son obligatorios.");
            doGet(req, resp);
            return;
        }

        // 3) Mapear a entidad
        Usuarios u = new Usuarios();
        u.setDpi(dpi);
        u.setNombres(nombres);
        u.setApellidos(apellidos);
        u.setCorreo(correo);
        u.setContrasena(pass);
        u.setIdRol(idRol);
        u.setIdLote((loteParam != null && !loteParam.isEmpty())
                      ? Integer.valueOf(loteParam) : null);
        u.setIdCasa((casaParam != null && !casaParam.isEmpty())
                      ? Integer.valueOf(casaParam) : null);
        u.setActivo(activo);

        // 4) Insertar y, si es residente, enviar QR
        boolean ok = usuarioDAO.insertar(u);
        if (!ok) {
            req.setAttribute("error", "No se pudo crear usuario. DPI o correo duplicado.");
            doGet(req, resp);
            return;
        }

        // 5) Generar QR y enviar correo si rol = RESIDENTE
        if (u.getIdRol() == UsuariosDAO.ID_ROL_RESIDENTE) {
            try {
                byte[] qr = QRUtils.generarBytes(u, 300, 300);
                String asunto = "Notificación de acceso creada";
                String cuerpo = String.format(
                "¡Hola %1$s!%n" +
                "Se ha generado exitosamente tu código QR de acceso al residencial.%n%n" +
                "A continuación, encontrarás los detalles de tu registro:%n" +
                "Nombre del Residente: %1$s%n" +
                "Validez del código QR: Permanente%n%n" +
                "Instrucciones Importantes:%n" +
                "- Guarda este correo o el archivo adjunto.%n" +
                "- Preséntalo al llegar al residencial para que el personal de seguridad lo escanee y valide tu acceso.",
                u.getNombres()
                );

                emailService.enviarQR(
                    u.getCorreo(),
                    asunto,
                    cuerpo,
                    qr,
                    "qr_acceso_" + u.getId() + ".png"
                );
                req.setAttribute("mensaje", "Usuario creado y correo enviado.");
            } catch (Exception ex) {
                req.setAttribute("mensaje",
                    "Usuario creado, pero fallo al enviar QR: " + ex.getMessage());
            }
        } else {
            req.setAttribute("mensaje", "Usuario creado correctamente.");
        }

        // 6) Redirigir a lista o volver al formulario
        resp.sendRedirect("ListarUsuarios");
    }
}

