<%-- 
    Document   : FA1_CrearUsuario
    Created on : 31/08/2025, 05:34:43 PM
    Author     : gp
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="Modelo.Roles" %>
<%@ page import="Modelo.Lote" %>
<%@ page import="Modelo.Casa" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Crear Usuario</title>
  <% String ctx = request.getContextPath(); %>
  <link rel="stylesheet" href="<%=ctx%>/css/bootstrap.min.css"/>
</head>

<body class="p-4">
<div class="container">
  <div class="d-flex justify-content-between align-items-center mb-3">
    <h3>Nuevo Usuario</h3>
    
  </div>

  <!-- Mensajes -->
  <c:if test="${not empty error}">
    <div class="alert alert-danger">${error}</div>
  </c:if>
  <c:if test="${not empty mensaje}">
    <div class="alert alert-success">${mensaje}</div>
  </c:if>
    
    
  <form id="formCrearUsuario" action="CrearUsuario" method="post" class="row g-3">

    <div class="col-md-6">
      <label for="dpi" class="form-label">DPI</label>
      <input id="dpi" name="dpi" type="text" class="form-control"/>
    </div>

    <div class="col-md-6">
      <label for="nombres" class="form-label">Nombres</label>
      <input id="nombres" name="nombres" type="text" class="form-control"/>
    </div>

    <div class="col-md-6">
      <label for="apellidos" class="form-label">Apellidos</label>
      <input id="apellidos" name="apellidos" type="text" class="form-control"/>
    </div>

    <div class="col-md-6">
      <label for="correo" class="form-label">Correo</label>
      <input id="correo" name="correo" type="email" class="form-control"/>
    </div>

    <div class="col-md-6">
      <label for="contrasena" class="form-label">Contraseña</label>
      <input id="contrasena" name="contrasena" type="password" class="form-control"/>
    </div>

    <div class="col-md-6">
      <label for="rol" class="form-label">Rol</label>
      <select id="rol" name="idRol" class="form-select">
        <option value="">-- Selecciona --</option>
        <% 
          List<Roles> roles = (List<Roles>)request.getAttribute("roles");
          for (Roles r : roles) { 
        %>
          <option value="<%= r.getId_rol() %>"><%= r.getNombre_rol() %></option>
        <% } %>
      </select>
    </div>

    <div class="col-md-6">
      <label for="lote" class="form-label">Lote</label>
      <select id="lote" name="idLote" class="form-select" disabled>
        <option value="">-- Selecciona --</option>
        <% 
          List<Lote> lotes = (List<Lote>)request.getAttribute("lotes");
          for (Lote l : lotes) { 
        %>
          <option value="<%= l.getIdLote() %>"><%= l.getNombreLote() %></option>
        <% } %>
      </select>
    </div>

    <div class="col-md-6">
      <label for="casa" class="form-label">Casa</label>
      <select id="casa" name="idCasa" class="form-select" disabled>
        <option value="">-- Selecciona --</option>
        <% 
          List<Casa> casas = (List<Casa>)request.getAttribute("casas");
          for (Casa c : casas) { 
        %>
          <option value="<%= c.getIdCasa() %>"><%= c.getNumeroCasa() %></option>
        <% } %>
      </select>
    </div>

    <div class="col-md-6 form-check">
      <input id="activo" name="activo" type="checkbox" class="form-check-input" checked/>
      <label for="activo" class="form-check-label">Activo</label>
    </div>

    <div class="col-12 d-flex gap-2">
      <button id="btnGuardar" type="submit" class="btn btn-success" disabled>Guardar</button>
      <button type="button" onclick="window.location='ListarUsuarios'" class="btn btn-secondary">Cancelar</button>
      <button id="btnLimpiar" type="reset" class="btn btn-warning">Limpiar</button>
    </div>
  </form>
</div>

<script>
  document.addEventListener('DOMContentLoaded', () => {
    const btn       = document.getElementById('btnGuardar');
    const dpi       = document.getElementById('dpi');
    const nombres   = document.getElementById('nombres');
    const apellidos = document.getElementById('apellidos');
    const correo    = document.getElementById('correo');
    const contrasena= document.getElementById('contrasena');
    const role      = document.getElementById('rol');
    const lote      = document.getElementById('lote');
    const casa      = document.getElementById('casa');

    const emailPattern = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;

    function basicFieldsValid() {
      return dpi.value.trim()       !== '' &&
             nombres.value.trim()   !== '' &&
             apellidos.value.trim() !== '' &&
             correo.value.trim()    !== '' &&
             contrasena.value       !== '' &&
             role.value            !== '';
    }

    function emailValid() {
      const v = correo.value.trim();
      return v === '' || emailPattern.test(v);
    }

    function passValid() {
      const v = contrasena.value;
      return v === '' || v.length >= 6;
    }

    function isSecurityAgent() {
      const txt = role.options[role.selectedIndex].text.toLowerCase();
      return txt.includes('seguridad');
    }

    function loteCasaValid() {
      if (isSecurityAgent()) {
        return true;
      }
      return lote.value !== '' && casa.value !== '';
    }

    function updateButton() {
      const allValid = basicFieldsValid() && emailValid() && passValid() && loteCasaValid();
      btn.disabled = !allValid;
    }

    role.addEventListener('change', () => {
      const block = isSecurityAgent();
      lote.disabled = block;
      casa.disabled = block;
      updateButton();
    });

    [dpi, nombres, apellidos, correo, contrasena, role, lote, casa].forEach(el => {
      el.addEventListener('input',  updateButton);
      el.addEventListener('change', updateButton);
    });

    updateButton();

    const form = document.getElementById('formCrearUsuario');
    form.addEventListener('reset', () => {
      setTimeout(() => {
        const block = isSecurityAgent();
        lote.disabled = block;
        casa.disabled = block;
        updateButton();
      }, 0);
    });
  });
</script>
</body>
</html>


