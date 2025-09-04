<%-- 
    Document   : ListarUsuarios
    Created on : 31/08/2025, 07:07:29 PM
    Author     : gp
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html lang="es">
<head>
    <title>Listado de Usuarios</title>
    <% String ctx = request.getContextPath(); %>
    <link rel="stylesheet" href="<%=ctx%>/css/bootstrap.min.css"/>
</head>
<body class="p-4">
<div class="container">

    <div class="d-flex justify-content-between align-items-center mb-3">
        <h3>Usuarios Registrados</h3>
        <div>
            <!-- Botón para crear nuevo usuario -->
            <form action="CrearUsuario" method="get" style="display:inline;">
                <button type="submit" class="btn btn-success">Nuevo Usuario</button>
            </form>

            <!-- Botón: Regresar al menú principal -->
            <a href="index.jsp" class="btn btn-primary ms-2">Menú Principal</a>
        </div>
    </div>

    <table class="table table-bordered table-striped align-middle">
        <thead class="table-light">
        <tr>
            <th>DPI</th>
            <th>Nombres</th>
            <th>Apellidos</th>
            <th>Correo</th>
            <th>Casa</th>
            <th>Rol</th>
            <th style="width: 180px;">Acciones</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="u" items="${usuarios}">
            <tr>
                <td>${u.dpi}</td>
                <td>${u.nombres}</td>
                <td>${u.apellidos}</td>
                <td>${u.correo}</td>
                <td>
                    <c:choose>
                        <c:when test="${not empty u.idCasa}">${u.idCasa}</c:when>
                        <c:otherwise>-</c:otherwise>
                    </c:choose>
                </td>
                <td>${rolesMap[u.idRol]}</td>
                <td>
                    <form action="EliminarUsuario" method="post" style="display:inline;"
                          onsubmit="return confirm('¿Eliminar usuario ${u.nombres}?');">
                        <input type="hidden" name="id" value="${u.id}" />
                        <button type="submit" class="btn btn-danger btn-sm">Eliminar</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
</body>
</html>

