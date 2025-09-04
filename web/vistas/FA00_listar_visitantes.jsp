<%-- 
    Document   : FA00_listar_visitantes
    Created on : 31/08/2025, 06:50:51 PM
    Author     : mpelv
--%>

<%@page import="Modelo.Visitante"%>
<%@ page import="java.util.List" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <title>Listado de Visitantes</title>
    <% String ctx = request.getContextPath(); %>
    <link rel="stylesheet" href="<%=ctx%>/css/bootstrap.min.css"/>
</head>
<body class="p-4">
<div class="container">

    <div class="d-flex justify-content-between align-items-center mb-3">
        <h3>Listado de Visitantes</h3>
        <a href="<%=ctx%>/VisitanteServlet?accion=nuevo" class="btn btn-success">
            Registro de visitantes
        </a>
                        <!-- NUEVO BOTÓN: Menú principal -->
            <a href="index.jsp" class="btn btn-primary ms-2">
                Menú Principal
            </a>
    </div>

    <table class="table table-bordered table-striped align-middle">
        <thead class="table-light">
        <tr>
            <th>Nombre del Visitante</th>
            <th>DPI</th>
            <th>Tipo de Visita</th>
            <th>Fecha</th>
            <th>Intentos</th>
            <th>Correo</th>
            <!-- Residente removido del encabezado -->
            <th style="width: 240px;">Acciones</th>
        </tr>
        </thead>
        <tbody>
        <%
            List<Visitante> lista = (List<Visitante>) request.getAttribute("lista");
            if (lista != null && !lista.isEmpty()) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                for (Visitante v : lista) {
        %>
        <tr>
            <td><%= v.getNombre() %></td>
            <td><%= v.getDpi() != null ? v.getDpi() : "" %></td>
            <td><%= v.getTipoVisita() %></td>
            <td><%= v.getFechaVisita() != null ? sdf.format(v.getFechaVisita()) : "" %></td>
            <td><%= v.getIntentos() > 0 ? v.getIntentos() : "" %></td>
            <td><%= v.getCorreo() != null ? v.getCorreo() : "" %></td>
            <!-- Residente NO se muestra -->
            <td>
               <a href="<%=ctx%>/VisitanteServlet?accion=cancelar&id=<%= v.getId() %>"
         class="btn btn-danger btn-sm"
          onclick="return confirm('¿Desea cancelar la visita?');">
             Cancelar visita
</a>

               <a href="<%=ctx%>/VisitanteServlet?accion=descargarQR&id=<%= v.getId() %>"
             class="btn btn-info btn-sm ms-1"  target="_blank">
                   Descargar QR
</a>

            </td>
        </tr>
        <%      }
            } else { %>
        <tr>
            <td colspan="7" class="text-center text-muted">No hay registros</td>
        </tr>
        <% } %>
        </tbody>
    </table>
</div>
</body>