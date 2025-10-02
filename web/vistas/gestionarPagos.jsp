<%-- 
    Document   : gestionarPagos
    Created on : 30/09/2025, 05:26:33 PM
    Author     : mpelv
--%>
<%@page import="java.text.SimpleDateFormat"%>
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

    String success = request.getParameter("success");
%>

<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<title>Gestionar Pagos</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons/font/bootstrap-icons.css" rel="stylesheet">
</head>
<body>
<div class="container py-5">
    <h1>Historial de Pagos - <%= usuario.getNombres() %></h1>

    <!-- Mensaje de éxito -->
    <% if("1".equals(success)) { %>
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            Pago realizado con éxito
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
        </div>
    <% } %>

    <!-- Botones al mismo nivel -->
    <div class="d-flex justify-content-between mb-3">
        <a href="<%= request.getContextPath() %>/PagoServlet" class="btn btn-warning">
            Pagar Servicio
        </a>
        <a href="<%= request.getContextPath() %>/index.jsp" class="btn btn-secondary">
            <i class="bi bi-house-fill"></i> Menú Principal
        </a>
    </div>

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
        <%
            int cont = 1;
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            if(pagos != null){
                for(Pago p : pagos){
        %>
            <tr>
                <td><%= cont++ %></td>
                <td><%= p.getTipoPago() %></td>
                <td><%= p.getCantidad() %></td>
                <td><%= (p.getFechaPago() != null) ? sdf.format(p.getFechaPago()) : "" %></td>
                <td><%= p.getObservaciones() %></td>
            </tr>
        <% 
                }
            } 
        %>
        </tbody>
    </table>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
