<%-- 
    Document   : FA10_directorio
    Created on : 10/09/2025, 05:49:01 PM
    Author     : mpelv
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8"/>
  <title>Directorio Residencial</title>
  <link
    href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css"
    rel="stylesheet"/>
</head>
<body class="bg-light">
  <div class="container py-4">
    <h2 class="mb-4">Directorio Residencial</h2>

    <form method="post"
          action="${pageContext.request.contextPath}/DirectorioResidencial"
          class="row g-3 align-items-end">

      <!-- Nombres -->
      <div class="col-md-3">
        <label class="form-label">Nombres</label>
        <input type="text"
               name="nombres"
               class="form-control"
               value="${param.nombres}" />
      </div>

      <!-- Apellidos -->
      <div class="col-md-3">
        <label class="form-label">Apellidos</label>
        <input type="text"
               name="apellidos"
               class="form-control"
               value="${param.apellidos}" />
      </div>

      <!-- Lote -->
      <div class="col-md-2">
        <label class="form-label">Lote</label>
        <select name="idLote" class="form-select">
          <option value="">--</option>
          <c:forEach var="l" items="${lotes}">
            <option value="${l.idLote}"
              <c:if test="${param.idLote == l.idLote}">selected</c:if>>
              ${l.nombreLote}
            </option>
          </c:forEach>
        </select>
      </div>

      <!-- Casa -->
      <div class="col-md-2">
        <label class="form-label">Numero de casa </label>
        <select name="idCasa" class="form-select">
          <option value="">--</option>
          <c:forEach var="c" items="${casas}">
            <option value="${c.idCasa}"
              <c:if test="${param.idCasa == c.idCasa}">selected</c:if>>
              ${c.numeroCasa}
            </option>
          </c:forEach>
        </select>
      </div>

      <!-- Botones -->
      <div class="col-md-2 d-grid gap-2">
          
           <!-- Botón Cerrar -->
       <button type="button"
          onclick="window.location.href='${pageContext.request.contextPath}/index.jsp'"
          class="btn btn-danger">
          Cerrar
             </button>
          
          
        <button type="submit"
                name="accion"
                value="buscar"
                class="btn btn-primary">
          Buscar
        </button>
        <button type="submit"
                name="accion"
                value="limpiar"
                class="btn btn-secondary mt-2">
          Limpiar
        </button>
                     
      </div>
    </form>

    <!-- Mensajes de error/info -->
    <c:if test="${not empty error}">
      <div class="alert alert-danger mt-3">${error}</div>
    </c:if>
    <c:if test="${not empty info}">
      <div class="alert alert-info mt-3">${info}</div>
    </c:if>

    <!-- Resultados -->
    <c:if test="${not empty lista}">
      <table class="table table-striped table-bordered mt-4">
        <thead class="table-light">
          <tr>
            <th>Nombre Completo</th>
            <th>Lote - Numero de casa</th>
            <th>Correo Electrónico</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="u" items="${lista}">
            <tr>
              <td>${u.nombres} ${u.apellidos}</td>
              <td>${u.nombreLote} - ${u.numeroCasa}</td>
              <td>${u.correo}</td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </c:if>

  </div>
</body>
</html>