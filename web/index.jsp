<%-- 
    Document   : index
    Created on : 20/08/2025, 09:06:29 PM
    Author     : gp
--%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="Modelo.Usuarios" %>
<%
    HttpSession sesion = request.getSession(false);
    if (sesion == null || sesion.getAttribute("usuario") == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }

    Usuarios usuario = (Usuarios) sesion.getAttribute("usuario");
    int idRol = usuario.getIdRol();
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <title>Menú Principal</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
  <style>
    body { background: #f8f9fa; }
    .card-menu { max-width: 500px; margin: auto; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
    .card-menu h1 { font-size: 1.8rem; font-weight: bold; }
    .menu-btn { width: 100%; margin-bottom: 12px; padding: 12px; font-size: 1.1rem; border-radius: 8px; }
  </style>
</head>
<body>

<div class="container py-5">
  <div class="card card-menu p-4">
    <div class="text-center mb-4">
      <h1 class="text-primary">Bienvenid@, <%= usuario.getNombres() %></h1>
      <p class="text-muted">Selecciona una opción del menú</p>
    </div>

    <%-- ADMINISTRADOR (idRol = 1) → acceso completo --%>
    <% if (idRol == 1) { %>
      <a class="btn btn-primary menu-btn"
         href="<%= request.getContextPath() %>/ListarUsuarios">Mantenimiento de Usuarios</a>
      <a class="btn btn-info text-white menu-btn"
         href="<%= request.getContextPath() %>/EscanearQR">Escanear Código QR</a>
    <% } %>

    <%-- AGENTE DE SEGURIDAD (idRol = 2) → solo escanear QR --%>
    <% if (idRol == 2) { %>
      <a class="btn btn-info text-white menu-btn"
         href="<%= request.getContextPath() %>/EscanearQR">Escanear Código QR</a>
    <% } %>

    <%-- RESIDENTE (idRol = 3) → registrar visitantes + escanear QR --%>
    <% if (idRol == 3) { %>
      <a class="btn btn-success menu-btn"
         href="<%= request.getContextPath() %>/VisitanteServlet?accion=listar">registrar visitante</a>
      <a class="btn btn-info text-white menu-btn"
         href="<%= request.getContextPath() %>/EscanearQR">Escanear Código QR</a>
      <a class="btn btn-info text-white menu-btn"
         href="<%= request.getContextPath() %>/ReservaServlet">Gestionar reservas</a>
    <% } %>
    
 <%-- AGENTE y RESIDENTE → Directorio Residencial --%>
    <% if (idRol == 2 || idRol == 3) { %>
      <a class="btn btn-secondary menu-btn"
         href="<%= request.getContextPath() %>/DirectorioResidencial">
        Directorio Residencial
      </a>
    <% } %>  

    <!-- Logout -->
    <form action="<%= request.getContextPath() %>/Logout" method="post">
      <button class="btn btn-danger menu-btn" type="submit">Cerrar Sesión</button>
    </form>
  </div>
</div>

</body>
</html>

