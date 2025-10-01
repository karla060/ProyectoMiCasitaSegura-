<%-- 
    Document   : gestionarPagos
    Created on : 30/09/2025, 05:26:33 PM
    Author     : mpelv
--%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="Modelo.Usuarios, Modelo.Pago, ModeloDAO.PagoDAO, Config.Conexion" %>
<%@ page import="java.sql.Connection, java.util.List" %>
<%@ page import="javax.servlet.http.HttpSession" %>

<%
    HttpSession sesion = request.getSession(false);
    if (sesion == null || sesion.getAttribute("usuario") == null) {
        response.sendRedirect(request.getContextPath() + "/vistas/login.jsp");
        return;
    }
    Usuarios usuario = (Usuarios) sesion.getAttribute("usuario");
    if (usuario.getIdRol() != 3) {
        response.sendRedirect(request.getContextPath() + "/index.jsp");
        return;
    }

    List<Pago> pagos = null;
    try (Connection con = new Conexion().getConnection()) {
        PagoDAO dao = new PagoDAO(con);
        pagos = dao.listarPagosPorUsuario(usuario.getId());
    } catch(Exception e) { e.printStackTrace(); }
%>

<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<title>Gestionar Pagos</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container py-5">
    <h1>Historial de Pagos - <%= usuario.getNombres() %></h1>

    <!-- Botón para flujo alterno -->
    <a href="<%= request.getContextPath() %>/PagoServlet" class="btn btn-warning mb-3">Pagar Servicio</a>

    <!-- Tabla de pagos -->
    <table class="table table-striped">
        <thead>
            <tr>
                <th>No.</th>
                <th>Tipo Pago</th>
                <th>Cantidad (Q.)</th>
                <th>Fecha de pago</th>
                <th>Observaciones</th>
            </tr>
        </thead>
        <tbody>
            <% int cont=1;
               if(pagos!=null){
                   for(Pago p: pagos){ %>
                       <tr>
                           <td><%= cont++ %></td>
                           <td><%= p.getTipoPago() %></td>
                           <td><%= p.getCantidad() %></td>
                           <td><%= p.getFechaPago() %></td>
                           <td><%= p.getObservaciones() %></td>
                       </tr>
            <%     }
               } %>
        </tbody>
    </table>
</div>
</body>
</html>
