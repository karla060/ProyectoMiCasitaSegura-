<%-- 
    Document   : FA01_registro_de_visitantes
    Created on : 31/08/2025, 06:49:27 PM
    Author     : mpelv
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <title>Registro de Visitante</title>
    <%-- Usa contextPath para rutas estables --%>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/bootstrap.min.css"/>

    <script>
        function actualizarCampos() {
            const tipo = document.getElementById("tipoVisita").value;
            document.getElementById("grupoIntentos").style.display = (tipo === "Por intentos") ? "block" : "none";
            document.getElementById("grupoFecha").style.display = (tipo === "Visita") ? "block" : "none";
            validarFormulario();
        }

        function validarFormulario() {
            const nombre = document.getElementById("nombre").value.trim();
            const tipo = document.getElementById("tipoVisita").value;
            const fecha = document.getElementById("fechaVisita").value;
            const intentos = document.getElementById("intentos").value;

            let habilitar = false;

            if (nombre !== "" && tipo !== "") {
                if (tipo === "Visita" && fecha !== "") {
                    habilitar = true;
                } else if (tipo === "Por intentos" && intentos !== "" && parseInt(intentos, 10) > 0) {
                    habilitar = true;
                }
            }

            document.getElementById("btnRegistrar").disabled = !habilitar;

            // Solo muestra el mensaje si no hay un error del servidor en pantalla
            const hayErrorServidor = !!document.getElementById("errorServidor");
            document.getElementById("mensajeValidacion").style.display = (!habilitar && !hayErrorServidor) ? "block" : "none";
        }

        document.addEventListener("DOMContentLoaded", function () {
            // Validación en vivo
            document.getElementById("nombre").addEventListener("input", validarFormulario);
            document.getElementById("tipoVisita").addEventListener("change", actualizarCampos);
            document.getElementById("fechaVisita").addEventListener("input", validarFormulario);
            document.getElementById("intentos").addEventListener("input", validarFormulario);

            // Evita doble envío
            const form = document.getElementById("frmRegistro");
            form.addEventListener("submit", function () {
                const btn = document.getElementById("btnRegistrar");
                btn.disabled = true;
                setTimeout(() => btn.disabled = true, 0);
            });

            // Inicializa visibilidad según selección actual
            actualizarCampos();
            const fecha = document.getElementById("fechaVisita");
if (fecha) {
    const hoy = new Date();
    const año = hoy.getFullYear();
    const mes = String(hoy.getMonth() + 1).padStart(2, '0');
    const dia = String(hoy.getDate()).padStart(2, '0');
    fecha.min = `${año}-${mes}-${dia}`;
}


            // Si el tipo es “Visita”, limita fecha mínima a hoy
            /*const fecha = document.getElementById("fechaVisita");
            if (fecha) {
                const hoy = new Date().toISOString().split("T")[0];
                fecha.min = hoy;
            }*/
        });
    </script>
</head>
<body class="p-4">
<div class="container">
    <h3>Registro de Visitantes</h3>

    <%-- Mensaje de error del servidor (si existe) --%>
    <% if (request.getAttribute("error") != null) { %>
        <div id="errorServidor" class="alert alert-danger"><%= request.getAttribute("error") %></div>
    <% } %>

    <form id="frmRegistro" method="post" action="<%=request.getContextPath()%>/VisitanteServlet">
        <input type="hidden" name="accion" value="registrar"/>

        <div class="mb-3">
            <label for="nombre">Nombre del visitante *</label>
            <input type="text" name="nombre" id="nombre" class="form-control"
                   value="<%= request.getParameter("nombre") != null ? request.getParameter("nombre") : "" %>"
                   required/>
        </div>

        <div class="mb-3">
            <label for="dpi">DPI del visitante</label>
          <input type="text"  name="dpi"  id="dpi" class="form-control"  pattern="[0-9 ]+"  title="Solo números y espacios"  maxlength="20"/>
        </div>

        <div class="mb-3">
            <label for="tipoVisita">Tipo de visita *</label>
            <select name="tipoVisita" id="tipoVisita" class="form-select" required>
                <option value="">-- Seleccione --</option>
                <option value="Visita" <%= "Visita".equals(request.getParameter("tipoVisita")) ? "selected" : "" %>>Visita</option>
                <option value="Por intentos" <%= "Por intentos".equals(request.getParameter("tipoVisita")) ? "selected" : "" %>>Por intentos</option>
            </select>
        </div>

        <div class="mb-3" id="grupoFecha" style="display:none;">
            <label for="fechaVisita">Fecha de visita *</label>
            <input type="date" name="fechaVisita" id="fechaVisita" class="form-control"
                   value="<%= request.getParameter("fechaVisita") != null ? request.getParameter("fechaVisita") : "" %>"/>
        </div>

        <div class="mb-3" id="grupoIntentos" style="display:none;">
            <label for="intentos">Intentos *</label>
            <input type="number" name="intentos" id="intentos" class="form-control" min="1"
                   value="<%= request.getParameter("intentos") != null ? request.getParameter("intentos") : "" %>"/>
        </div>

        <div class="mb-3">
            <label for="correo">Correo del visitante</label>
            <input type="email" name="correo" id="correo" class="form-control"
                   value="<%= request.getParameter("correo") != null ? request.getParameter("correo") : "" %>"/>
        </div>

        <div class="mb-3 d-flex gap-2">
            <button type="submit" class="btn btn-primary" id="btnRegistrar" disabled>Registrar visita</button>

            <button type="button"
                    class="btn btn-secondary"
                    onclick="window.location.href='<%=request.getContextPath()%>/VisitanteServlet?accion=listar'">
                Cerrar
            </button>
        </div>

        <div id="mensajeValidacion" class="text-danger" style="display:none;">
            Complete todos los campos obligatorios para habilitar el registro.
        </div>
    </form>

    <% if (request.getAttribute("visitanteCreado") != null) { %>
        <div class="alert alert-success mt-3">
            Registro exitoso. El código QR fue enviado por correo.
        </div>
    <% } %>
</div>
</body>
</html>