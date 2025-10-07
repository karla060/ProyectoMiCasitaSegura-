<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="Modelo.Usuarios" %>
<%
    HttpSession sesion = request.getSession(false);
    if (sesion == null || sesion.getAttribute("usuario") == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    Usuarios usuario = (Usuarios) sesion.getAttribute("usuario");
    List<String> tipos = (List<String>) request.getAttribute("tipos");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Reportar Incidente</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css">
    <script>
        function habilitarBoton() {
            var tipo = document.getElementById("tipo").value;
            var fechaHora = document.getElementById("fechaHora").value;
            var descripcion = document.getElementById("descripcion").value;
            document.getElementById("guardarBtn").disabled = !(tipo && fechaHora && descripcion);
        }
    </script>
</head>
<body class="p-3">
<h2>Reportar Incidente</h2>

<% if(request.getAttribute("error") != null) { %>
    <div class="alert alert-danger"><%= request.getAttribute("error") %></div>
<% } %>

<% if(request.getAttribute("exito") != null) { %>
    <div class="alert alert-success"><%= request.getAttribute("exito") %></div>
<% } %>

<form method="post" action="ReportarIncidente">
    <div class="mb-3">
        <label for="tipo" class="form-label">Tipo de incidente</label>
        <select name="tipo" id="tipo" class="form-select" onchange="habilitarBoton()">
            <option value="">Seleccione...</option>
            <% for(String t : tipos) { %>
                <option value="<%= t %>"><%= t %></option>
            <% } %>
        </select>
    </div>

    <div class="mb-3">
        <label for="fechaHora" class="form-label">Fecha y hora del incidente</label>
        <input type="datetime-local" id="fechaHora" name="fechaHora" class="form-control" onchange="habilitarBoton()" required>
    </div>

    <div class="mb-3">
        <label for="descripcion" class="form-label">Descripción</label>
        <textarea id="descripcion" name="descripcion" class="form-control" maxlength="200" rows="4" oninput="habilitarBoton()" required></textarea>
    </div>

    <button type="submit" id="guardarBtn" class="btn btn-success" disabled>Guardar</button>
    <a href="<%= request.getContextPath() %>/vistas/comunicacionInterna.jsp" class="btn btn-secondary">Regresar</a>
</form>
</body>
</html>
