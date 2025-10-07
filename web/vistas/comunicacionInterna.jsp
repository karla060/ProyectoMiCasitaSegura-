<%-- 
    Document   : comunicacionInterna
    Created on : 05/10/2025
    Author     : gp
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Comunicación Interna</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons/font/bootstrap-icons.css" rel="stylesheet">
</head>
<body class="p-3">

    <c:if test="${not empty sessionScope.exito}">
    <div class="alert alert-success">${sessionScope.exito}</div>
    <c:remove var="exito" scope="session"/>
</c:if>

<h2>Comunicación Interna</h2>
<p>Seleccione la acción que desea realizar:</p>
<div class="mb-3">
    <a href="<%= request.getContextPath() %>/consultaGeneral" class="btn btn-info me-2">
        Consulta General
    </a>
    <a href="<%= request.getContextPath() %>/ReportarIncidente" class="btn btn-warning">
        Reportar Incidente
    </a>
</div>

        <div class="mb-3">
    <a href="<%= request.getContextPath() %>/index.jsp" class="btn btn-secondary mb-3">
        <i class="bi bi-house-fill"></i> Menú Principal
    </a>
</div>
        
</body>
</html>
