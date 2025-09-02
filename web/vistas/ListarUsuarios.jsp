<%-- 
    Document   : ListarUsuarios
    Created on : 31/08/2025, 07:07:29 PM
    Author     : gp
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <title>Listado de Usuarios</title>
    <style>
      table { border-collapse: collapse; width: 100%; }
      th, td { padding: 8px; border: 1px solid #ccc; text-align: left; }
      form { display: inline; }
    </style>
</head>
<body>
  <h2>Usuarios Registrados</h2>

  <!-- Botón para crear nuevo usuario -->
  <form action="CrearUsuario" method="get">
    <button type="submit">Nuevo Usuario</button>
  </form>

  <br/><br/>
  
  <table>
    <tr>
     
      <th>DPI</th>
      <th>Nombres</th>
      <th>Apellidos</th>
      <th>Correo</th>
      <th>Casa</th>
      <th>Rol</th>     
      <th>Acciones</th>
    </tr>
    <c:forEach var="u" items="${usuarios}">
      <tr>
        
        <td>${u.dpi}</td>
        <td>${u.nombres}</td>
        <td>${u.apellidos}</td>
        <td>${u.correo}</td>
        <td><c:choose>
              <c:when test="${not empty u.idCasa}">${u.idCasa}</c:when>
              <c:otherwise>-</c:otherwise>
            </c:choose>
        </td>
        <td>${rolesMap[u.idRol]}</td>
        <td>
          <form action="EliminarUsuario" method="post"
                onsubmit="return confirm('¿Eliminar usuario ${u.nombres}?');">
            <input type="hidden" name="id" value="${u.id}" />
            <button type="submit">Eliminar</button>
          </form>
        </td>
      </tr>
    </c:forEach>
  </table>
</body>
</html>
