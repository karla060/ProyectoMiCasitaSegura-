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

<html>
<head>
  <meta charset="UTF-8">
  <title>Crear Usuario</title>
  <style>
    form div { margin-bottom: 12px; }
    label { display: inline-block; width: 120px; vertical-align: top; }
    input, select { width: 240px; }
    #btnGuardar:disabled {
      background: #ccc;
      cursor: pointer;
      color:white;
      padding: 6px 12px;
      border:none;
    }
    
      #btnLimpiar {
      background: #ccc;
      cursor: pointer;
      color:black;
      padding: 6px 12px;
      border:none;
    }
  </style>
</head>

<body>
  <h2>Nuevo Usuario</h2>

  <c:if test="${not empty error}">
    <div style="color:red; margin-bottom:12px;">${error}</div>
  </c:if>
  <c:if test="${not empty mensaje}">
    <div style="color:green; margin-bottom:12px;">${mensaje}</div>
  </c:if>

  <form id="formCrearUsuario" action="CrearUsuario" method="post">
    <div>
      <label for="dpi">DPI:</label>
      <input id="dpi" name="dpi" type="text"/>
    </div>
    <div>
      <label for="nombres">Nombres:</label>
      <input id="nombres" name="nombres" type="text"/>
    </div>
    <div>
      <label for="apellidos">Apellidos:</label>
      <input id="apellidos" name="apellidos" type="text"/>
    </div>
    <div>
      <label for="correo">Correo:</label>
      <input id="correo" name="correo" type="email"/>
    </div>
    <div>
      <label for="contrasena">Contraseña:</label>
      <input id="contrasena" name="contrasena" type="password"/>
    </div>
    <div>
      <label for="rol">Rol:</label>
      <select id="rol" name="idRol">
        <option value="">-- Selecciona --</option>
        <% 
          List<Roles> roles = (List<Roles>)request.getAttribute("roles");
          for (Roles r : roles) { 
        %>
          <option value="<%= r.getId_rol() %>">
            <%= r.getNombre_rol() %>
          </option>
        <% } %>
      </select>
    </div>
    <div>
      <label for="lote">Lote:</label>
      <select id="lote" name="idLote" disabled>
        <option value="">-- Selecciona --</option>
        <% 
          List<Lote> lotes = (List<Lote>)request.getAttribute("lotes");
          for (Lote l : lotes) { 
        %>
          <option value="<%= l.getIdLote() %>">
            <%= l.getNombreLote() %>
          </option>
        <% } %>
      </select>
    </div>
    <div>
      <label for="casa">Casa:</label>
      <select id="casa" name="idCasa" disabled>
        <option value="">-- Selecciona --</option>
        <% 
          List<Casa> casas = (List<Casa>)request.getAttribute("casas");
          for (Casa c : casas) { 
        %>
          <option value="<%= c.getIdCasa() %>">
            <%= c.getNumeroCasa() %>
          </option>
        <% } %>
      </select>
    </div>
    <div>
      <label for="activo">Activo:</label>
      <input id="activo" name="activo" type="checkbox" checked/>
    </div>
    <div>
      <button id="btnGuardar" type="submit" disabled>Guardar</button>
      <button type="button" onclick="window.location='ListarUsuarios'"
       style="background-color: #ccc; color: black; border: none; padding: 6px 12px; cursor: pointer;">
        Cancelar
      </button>
      <button id="btnLimpiar" type="reset">Limpiar</button>
    </div>
  </form>

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

      // Patrón de email (igual que en JavaFX)
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
        const allValid = basicFieldsValid() &&
                         emailValid() &&
                         passValid() &&
                         loteCasaValid();
        btn.disabled = !allValid;
      }

      // Al cambiar rol, habilitar/deshabilitar lote y casa
      role.addEventListener('change', () => {
        const block = isSecurityAgent();
        lote.disabled = block;
        casa.disabled = block;
        updateButton();
      });

      // Asociar validación a todos los inputs y selects
      [dpi, nombres, apellidos, correo, contrasena, role, lote, casa]
        .forEach(el => {
          el.addEventListener('input',  updateButton);
          el.addEventListener('change', updateButton);
        });

      // Validación inicial al cargar la página
      updateButton();
      
      // Capturamos el form y, al resetearlo, volvemos a ajustar estados
const form = document.getElementById('formCrearUsuario');
form.addEventListener('reset', () => {
  // Espera un tick para que HTML restablezca valores por defecto
  setTimeout(() => {
    // Reaplica bloqueo de lote/casa según rol (ya refresca updateButton)
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

