<%-- 
    Document   : reporteMantenimiento
    Created on : 1/10/2025, 06:10:53 PM
    Author     : gp
--%>

<%@page import="java.util.List"%>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Reporte de Mantenimiento</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css">
    <script>
        function habilitarEnviar() {
            var tipo = document.getElementById("tipoInconveniente").value;
            var descripcion = document.getElementById("descripcion").value;
            var fecha = document.getElementById("fechaHora").value;
            document.getElementById("btnEnviar").disabled = !(tipo && descripcion && fecha);
        }

        function limpiarCampos() {
            document.getElementById("tipoInconveniente").value = "";
            document.getElementById("descripcion").value = "";
            document.getElementById("fechaHora").value = "";
            document.getElementById("btnEnviar").disabled = true;
        }
    </script>
</head>
<body class="p-4">
<h2>Reportes de Mantenimiento</h2>

<form method="post" action="ReporteMantenimientoServlet">
    <div class="mb-3">
        <label>Tipo de inconveniente</label>
      <select id="tipoInconveniente" name="tipoInconveniente" class="form-select" onchange="habilitarEnviar()">
    <option value="">--Seleccione--</option>
    <%
        List<String> tipos = (List<String>) request.getAttribute("tiposInconvenientes");
        if (tipos != null) {
            for (String t : tipos) {
    %>
        <option value="<%= t %>"><%= t %></option>
    <%
            }
        }
    %>
</select>


    </div>

    <div class="mb-3">
        <label>Descripción</label>
        <textarea id="descripcion" name="descripcion" class="form-control" oninput="habilitarEnviar()"></textarea>
    </div>

    <div class="mb-3">
        <label>Fecha y hora del incidente</label>
        <input type="datetime-local" id="fechaHora" name="fechaHora" class="form-control" onchange="habilitarEnviar()">
    </div>

    <button type="submit" id="btnEnviar" class="btn btn-success" disabled>Enviar Reporte</button>
    <button type="button" class="btn btn-secondary" onclick="limpiarCampos()">Limpiar</button>
    <button type="button" class="btn btn-danger" onclick="window.location.href='index.jsp'">Cancelar</button>
</form>

<% if (request.getAttribute("mensaje") != null) { %>
    <div class="alert alert-success mt-3"><%= request.getAttribute("mensaje") %></div>
<% } %>
</body>
</html>

