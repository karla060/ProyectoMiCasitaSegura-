<%-- 
    Document   : gestionarPagos
    Created on : 30/09/2025, 05:26:33 PM
    Author     : mpelv
--%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="Modelo.Usuarios" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

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
   <c:if test="${param.success eq '1'}">
    <div class="alert alert-success alert-dismissible fade show" role="alert">
        Pago realizado con éxito
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
    </div>
</c:if>

    <!-- Botones -->
    <div class="d-flex justify-content-between mb-3">
        <a href="${pageContext.request.contextPath}/PagoServlet" class="btn btn-warning">Pagar Servicio</a>
        <a href="${pageContext.request.contextPath}/index.jsp" class="btn btn-secondary"><i class="bi bi-house-fill"></i> Menú Principal</a>
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
            <c:forEach var="p" items="${pagos}" varStatus="status">
                <tr>
                    <td>${status.index + 1}</td>
                    <td>${tiposMap[p.idCatalogo]}</td>
                    <td>${p.cantidad}</td>
                    <td>
                        <fmt:formatDate value="${p.fechaPago}" pattern="dd/MM/yyyy" />
                    </td>
                    <td>${p.observaciones}</td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
