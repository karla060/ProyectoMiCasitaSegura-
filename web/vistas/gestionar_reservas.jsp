<%-- 
    Document   : gestionar_reservas
    Created on : 10/09/2025, 08:50:52 PM
    Author     : gp
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List"%>
<%@ page import="Modelo.Reserva"%>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Lista de Reservas</title>
    <!-- Bootstrap 5 CDN -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

<div class="container mt-5">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h2 class="text-primary">Reservas Activas</h2>
        <a href="ReservaServlet?accion=nueva" class="btn btn-success">
            <i class="bi bi-plus-circle"></i> Crear Reserva
        </a>
                <a href="index.jsp" class="btn btn-secondary">
            <i class="bi bi-house-door"></i> Menú Principal
        </a>
    </div>

    <%-- Mensajes de error o éxito --%>
    <%
        if (request.getAttribute("error") != null) {
    %>
        <div class="alert alert-danger"><%= request.getAttribute("error") %></div>
    <%
        } else if (request.getAttribute("msg") != null) {
    %>
        <div class="alert alert-success"><%= request.getAttribute("msg") %></div>
    <%
        }
    %>

    <div class="card shadow-lg">
        <div class="card-body">
            <div class="table-responsive">
                <table class="table table-striped table-hover align-middle">
                    <thead class="table-primary">
                        <tr>
                            <th>Salón reservado</th>
                            <th>Fecha Reservada</th>
                            <th>Hora Inicio Reservada</th>
                            <th>Hora Fin Reservada</th>
                            <th>Acción</th>
                        </tr>
                    </thead>
                    <tbody>
                    <%
                        List<Reserva> lista = (List<Reserva>) request.getAttribute("reservas");
                        if (lista != null && !lista.isEmpty()) {
                            for (Reserva r : lista) {
                    %>
                        <tr>
                            <td><%= r.getSalon() %></td>
                            <td><%= new java.text.SimpleDateFormat("dd/MM/yyyy").format(r.getFecha()) %></td>
                            <td><%= r.getHoraInicio() %></td>
                            <td><%= r.getHoraFin() %></td>
                            <td>
                                <a href="ReservaServlet?accion=cancelar&id=<%= r.getId() %>"
                                   class="btn btn-danger btn-sm"
                                   onclick="return confirm('¿Desea cancelar la reserva?');">
                                    Cancelar Reserva
                                </a>
                            </td>
                        </tr>
                    <%
                            }
                        } else {
                    %>
                        <tr>
                            <td colspan="5" class="text-center text-muted">No hay reservas registradas.</td>
                        </tr>
                    <%
                        }
                    %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<!-- Bootstrap JS + Icons -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons/font/bootstrap-icons.css" rel="stylesheet">



</body>
</html>



