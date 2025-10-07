<%-- 
    Document   : pagoServicio
    Created on : 30/09/2025, 03:49:08 PM
    Author     : mpelv
--%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="Modelo.Usuarios" %>
<%@ page import="javax.servlet.http.HttpSession" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

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

    // Atributos enviados por el servlet
    String mesAPagarStr = (String) request.getAttribute("mesAPagarStr");
    Double cantidad = (Double) request.getAttribute("cantidad");
    java.util.Date fechaLimite = (java.util.Date) request.getAttribute("fechaLimite");

    String success = request.getParameter("success");
%>

<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<title>Pagar Servicio</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container py-5">

    <%-- Mensajes de error enviados desde el servlet --%>
<%
    String mensajeError = (String) request.getAttribute("mensajeError");
    String mensaje = (String) request.getAttribute("mensaje");
    if (mensajeError != null) {
%>
<div class="alert alert-danger alert-dismissible fade show" role="alert">
     <%= mensajeError %>
    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
</div>
<% } else if (mensaje != null) { %>
<div class="alert alert-warning alert-dismissible fade show" role="alert">
    <%= mensaje %>
    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
</div>
<% } %>

    

    <h1>Pagos de Servicios - <%= usuario.getNombres() %></h1>

    <form method="post" action="PagoServlet" id="formPago">

       <!-- Selección de tipo de pago -->
<div class="mb-3">
    <label>Tipo de pago *</label>
    <select name="tipoPago" id="tipoPago" class="form-control" required>
    <option value="">Seleccione...</option>
    <c:forEach var="tipo" items="${tiposPago}">
        <option value="${tipo.nombre}">${tipo.nombre}</option>
    </c:forEach>
</select>

</div>


        <!-- Botón Consultar -->
        <div class="mb-3">
            <button type="button" class="btn btn-primary" id="btnConsultar" disabled>Consultar</button>
        </div>

        <!-- Campos que aparecen después de consultar -->
        <div id="camposPago" style="display:none;">

           <!-- Mes a pagar (visible solo lectura) -->
        <div class="mb-3">
        <label>Mes a pagar</label>
            <input type="text" id="mesAPagar" class="form-control" value="<%= mesAPagarStr != null ? mesAPagarStr : "" %>" readonly>
              <!-- Hidden que envía la fecha real al servlet -->
            <input type="hidden" name="mesAPagar" id="mesAPagarHidden" value="<%= new java.text.SimpleDateFormat("yyyy-MM-dd").format(fechaLimite) %>">
        </div>

            <div class="mb-3">
                <label>Mora (Q.)</label>
                <input type="text" id="mora" class="form-control" value="0.00" readonly>
            </div>

            <div class="mb-3">
                <label>Total (Q.)</label>
                <input type="text" id="total" class="form-control" value="0.00" readonly>
            </div>

            <input type="hidden" name="mora" id="moraHidden" value="0.00">
            <input type="hidden" name="total" id="totalHidden" value="0.00">

            <div class="mb-3">
                <label>Observaciones *</label>
                <input type="text" name="observaciones" class="form-control" required>
            </div>

            <div class="mb-3">
                <label>Número de tarjeta *</label>
                <input type="text" name="numeroTarjeta" class="form-control" required>
            </div>

            <div class="mb-3">
                <label>Fecha vencimiento *</label>
                <input type="date" name="fechaVencimiento" class="form-control" required>
            </div>

            <div class="mb-3">
                <label>CVV *</label>
                <input type="text" name="cvv" class="form-control" required>
            </div>

            <div class="mb-3">
                <label>Nombre Titular *</label>
                <input type="text" name="nombreTitular" class="form-control" required>
            </div>

            <button type="submit" class="btn btn-success" id="btnRegistrar" disabled>Registrar Pago</button>

        </div>

        <button type="button" class="btn btn-secondary" onclick="limpiarCampos()">Limpiar</button>
        <a href="<%= request.getContextPath() %>/GestionarPagos" class="btn btn-warning">Volver</a>

    </form>
</div>
<script>
const tipoPagoSelect = document.getElementById("tipoPago");
const btnConsultar = document.getElementById("btnConsultar");
const camposPago = document.getElementById("camposPago");
const cantidadBase = <%= cantidad %>;
const fechaLimite = new Date("<%= new java.text.SimpleDateFormat("yyyy-MM-dd").format(fechaLimite) %>");

// Limitar fecha de vencimiento a hoy o posterior
document.addEventListener("DOMContentLoaded", () => {
    const fechaInput = document.querySelector("input[name='fechaVencimiento']");
    if (fechaInput) {
        const hoy = new Date();
        const año = hoy.getFullYear();
        const mes = String(hoy.getMonth() + 1).padStart(2, '0');
        const dia = String(hoy.getDate()).padStart(2, '0');
        fechaInput.min = `${año}-${mes}-${dia}`; // establece la fecha mínima
    }

    // Habilitar botón Consultar cuando se selecciona tipo de pago
    tipoPagoSelect.addEventListener("change", () => {
        btnConsultar.disabled = tipoPagoSelect.value === "";
    });

    // Acción botón Consultar
    btnConsultar.addEventListener("click", () => {
        const tipo = tipoPagoSelect.value;
        window.location.href = "PagoServlet?tipoPago=" + encodeURIComponent(tipo);
    });

    // Mostrar campos después de consultar
    <% if (request.getAttribute("tipoPago") != null && request.getAttribute("cantidad") != null) { %>
    camposPago.style.display = "block";
    const tipoPago = "<%= request.getAttribute("tipoPago") %>";
    if(tipoPago){
        tipoPagoSelect.value = tipoPago;
    }
    actualizarTotal(); // recalcular mora y total
    <% } %>

    // Escuchar cambios en inputs requeridos para habilitar botón
    document.querySelectorAll("#formPago input[required], #formPago select[required]")
        .forEach(el => el.addEventListener("input", habilitarBoton));

    // Antes de enviar el formulario
   document.getElementById("formPago").addEventListener("submit", (e) => {
    const fechaVenc = document.querySelector("input[name='fechaVencimiento']").value;
    if (fechaVenc) {
        // Obtenemos fecha de hoy ignorando la hora
        const hoy = new Date();
        const hoySoloFecha = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate());

        // Obtenemos fecha ingresada ignorando la hora
        const partes = fechaVenc.split("-");
        const fechaIngresada = new Date(partes[0], partes[1]-1, partes[2]);

        // Comparación correcta, hoy cuenta
        if (fechaIngresada < hoySoloFecha) {
            e.preventDefault(); // Bloquea el envío

            // Mostrar mensaje tipo Bootstrap
            let contenedor = document.querySelector(".alert-container");
            if (!contenedor) {
                contenedor = document.createElement("div");
                contenedor.classList.add("alert-container", "mb-3");
                document.querySelector(".container").prepend(contenedor);
            }
            contenedor.innerHTML = `
                <div class="alert alert-danger alert-dismissible fade show" role="alert">
                    La fecha de vencimiento debe ser hoy o posterior.
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
                </div>
            `;
            return;
        }
    }

    // Copiar valores a hidden antes de enviar
    document.getElementById("mesAPagarHidden").value = document.getElementById("mesAPagarHidden").value;
    document.getElementById("moraHidden").value = document.getElementById("mora").value;
    document.getElementById("totalHidden").value = document.getElementById("total").value;
    });

});

// Calcula mora y total
function actualizarTotal() {
    const tipo = tipoPagoSelect.value;
    let mora = 0;

    if(tipo === "Mantenimiento") {
        const hoy = new Date();
        hoy.setHours(0,0,0,0);
        const fechaLim = new Date(fechaLimite);
        fechaLim.setHours(0,0,0,0);
        if(hoy > fechaLim) {
            const diffTime = hoy - fechaLim;
            const diasRetraso = Math.floor(diffTime / (1000*60*60*24));
            mora = diasRetraso * 25;
        }
    }

    document.getElementById("mora").value = mora.toFixed(2);
    document.getElementById("moraHidden").value = mora.toFixed(2);

    let total = 0;
    switch(tipo){
        case "Mantenimiento": total = cantidadBase; break;
        case "Multa": total = 250; break;
        case "Reinstalación de servicios": total = 750; break;
    }
    total += mora;

    document.getElementById("total").value = total.toFixed(2);
    document.getElementById("totalHidden").value = total.toFixed(2);

    habilitarBoton();
}

// Habilita botón registrar si todos los campos requeridos están completos
function habilitarBoton() {
    const tipoPago = tipoPagoSelect.value;
    const observaciones = document.querySelector("input[name='observaciones']").value;
    const numTarjeta = document.querySelector("input[name='numeroTarjeta']").value;
    const fechaVenc = document.querySelector("input[name='fechaVencimiento']").value;
    const cvv = document.querySelector("input[name='cvv']").value;
    const nombreTitular = document.querySelector("input[name='nombreTitular']").value;

    const btn = document.getElementById("btnRegistrar");
    btn.disabled = !(tipoPago && observaciones && numTarjeta && fechaVenc && cvv && nombreTitular);
}

// Limpiar formulario
function limpiarCampos() {
    document.getElementById("formPago").reset();
    camposPago.style.display = "none";
    document.getElementById("mora").value = "0.00";
    document.getElementById("total").value = "0.00";
    document.getElementById("moraHidden").value = "0.00";
    document.getElementById("totalHidden").value = "0.00";
    document.getElementById("btnRegistrar").disabled = true;
}
</script>
 


</body>
</html>
