<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, Modelo.Usuarios" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Crear Nueva Conversación</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet"/>
</head>
<body class="bg-light">

<div class="container mt-5">
    <% if (request.getAttribute("error") != null) { %>
    <div class="alert alert-danger">
        <%= request.getAttribute("error") %>
    </div>
<% } %>

  <h3 class="text-center mb-4">Crear Nueva Conversación</h3>

  <form action="crearConversacion" method="post" class="card p-4 shadow">

    <div class="mb-3">
      <label for="agente" class="form-label">Selecciona un agente activo:</label>
      <select name="idAgente" id="agente" class="form-select" required>
        <option value="">-- Selecciona un agente --</option>
        <%
          List<Usuarios> agentes = (List<Usuarios>) request.getAttribute("agentes");
          if (agentes != null && !agentes.isEmpty()) {
              for (Usuarios u : agentes) {
        %>
          <option value="<%= u.getId() %>"><%= u.getNombres() %></option>
        <%
              }
          } else {
        %>
          <option disabled>No hay agentes activos disponibles</option>
        <%
          }
        %>
      </select>
    </div>

    <div class="text-center mt-4">
      <button type="submit" class="btn btn-primary me-3">Guardar</button>
      <a href="consultaGeneral" class="btn btn-secondary">Cancelar</a>
    </div>

  </form>

  <%
    // Mostrar mensaje si se pasó desde el servlet
    String mensaje = (String) request.getAttribute("mensaje");
    if (mensaje != null) {
  %>
    <div class="alert alert-info mt-3 text-center"><%= mensaje %></div>
  <%
    }
  %>

</div>

</body>
</html>
