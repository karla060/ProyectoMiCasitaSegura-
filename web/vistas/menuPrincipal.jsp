<%-- 
    Document   : menuPrincipal
    Created on : 31/08/2025, 07:12:10 PM
    Author     : gp
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<html>
<head>
  <title>Menú Principal</title>
  <style>
    ul { list-style: none; padding: 0; }
    li { margin: 0.5em 0; }
    .btn {
      display: inline-block;
      padding: 0.6em 1.2em;
      background: #0066cc;
      color: #fff;
      text-decoration: none;
      border-radius: 4px;
      font-family: sans-serif;
    }
    .btn:hover { background: #004a99; }
    form { display: inline; }
  </style>
</head>
<body>
  <h1>Panel de Control</h1>

  <!-- Listar Usuarios -->
  <form action="ListarUsuarios" method="get">
    <button class="btn" type="submit">Mantenimiento de Usuarios</button>
  </form>

  <!-- Registro de Visitantes -->
  <form action="RegistroVisitantes" method="get">
    <button class="btn" type="submit">Registro de Visitantes</button>
  </form>

  <!-- Escanear QR -->
  <form action="EscanearQR" method="get">
    <button class="btn" type="submit">Escanear Código QR</button>
  </form>

  <!-- Cerrar sesión o salir -->
  <form action="Logout" method="post">
    <button class="btn" type="submit">Cerrar Sesión</button>
  </form>
</body>
</html>

