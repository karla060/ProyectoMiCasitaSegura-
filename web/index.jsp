<%-- 
    Document   : index
    Created on : 20/08/2025, 09:06:29 PM
    Author     : gp
--%>

<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" %>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <title>Menú Principal</title>
  <style>
    ul { list-style: none; padding: 0; }
    li { margin: 0.5em 0; }
    .btn { display:inline-block; padding:0.6em 1.2em; background:#0066cc; color:#fff; text-decoration:none; border-radius:4px; font-family:sans-serif; border:none; cursor:pointer; }
    .btn:hover { background:#004a99; }
    .btn-lg { font-size:1.1em; }
    form { display:inline; }
  </style>
</head>
<body>
  <h1>Bienvenido al sistema</h1>

  <!-- Único botón principal -->
  <p>
    <a class="btn btn-primary btn-lg"
       href="<%= request.getContextPath() %>/VisitanteServlet?accion=listar">
      Registrar visitante
    </a>
  </p>

  <ul>
    <li>
      <a class="btn" href="<%= request.getContextPath() %>/ListarUsuarios">
        Mantenimiento de Usuarios
      </a>
    </li>
    <li>
      <a class="btn" href="<%= request.getContextPath() %>/EscanearQR">
        Escanear Código QR
      </a>
    </li>
    <li>
      <form action="<%= request.getContextPath() %>/Logout" method="post">
        <button class="btn" type="submit">Cerrar Sesión</button>
      </form>
    </li>
  </ul>
</body>
</html>