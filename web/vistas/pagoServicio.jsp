<%-- 
    Document   : pagoServicio
    Created on : 30/09/2025, 03:49:08 PM
    Author     : mpelv
--%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="Modelo.Usuarios" %>
<%@ page import="javax.servlet.http.HttpSession" %>
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
    <h1>Pagar Servicio - <%= usuario.getNombres() %></h1>

    <form method="post" action="PagoServlet" id="formPago">
        <div class="mb-3">
            <label>Tipo de pago *</label>
            <select name="tipoPago" id="tipoPago" class="form-control" required onchange="actualizarTotal()">
                <option value="">Seleccione...</option>
                <option value="Mantenimiento">Mantenimiento</option>
                <option value="Multa">Multa</option>
                <option value="Reinstalación de servicios">Reinstalación de servicios</option>
            </select>
        </div>

        <div class="mb-3">
            <label>Mes a pagar</label>
            <input type="text" id="mesAPagar" class="form-control" value="<%= mesAPagarStr %>" readonly>
        </div>

        <div class="mb-3">
            <label>Mora (Q.)</label>
            <input type="text" id="mora" class="form-control" value="0.00" readonly>
        </div>

        <div class="mb-3">
            <label>Total (Q.)</label>
            <input type="text" id="total" class="form-control" value="0.00" readonly>
        </div>

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
        <button type="button" class="btn btn-secondary" onclick="limpiarCampos()">Limpiar</button>
        <a href="<%= request.getContextPath() %>/GestionarPagos" class="btn btn-warning">Volver</a>

    </form>
</div>

<script>
const cantidadBase = <%= cantidad %>;
const fechaLimite = new Date("<%= new java.text.SimpleDateFormat("yyyy-MM-dd").format(fechaLimite) %>");

function actualizarTotal() {
    const tipo = document.getElementById("tipoPago").value;
    let mora = 0;
    if(tipo === "Mantenimiento") {
        const hoy = new Date();
        if(hoy > fechaLimite) {
            const diffTime = hoy - fechaLimite;
            const diasRetraso = Math.floor(diffTime / (1000*60*60*24));
            mora = diasRetraso * 25;
        }
    }
    document.getElementById("mora").value = mora.toFixed(2);

    let total = 0;
    switch(tipo){
        case "Mantenimiento": total = cantidadBase; break;
        case "Multa": total = 250; break;
        case "Reinstalación de servicios": total = 750; break;
    }
    total += mora;
    document.getElementById("total").value = total.toFixed(2);
    habilitarBoton();
}

function habilitarBoton() {
    const tipoPago = document.getElementById("tipoPago").value;
    const observaciones = document.querySelector("input[name='observaciones']").value;
    const numTarjeta = document.querySelector("input[name='numeroTarjeta']").value;
    const fechaVenc = document.querySelector("input[name='fechaVencimiento']").value;
    const cvv = document.querySelector("input[name='cvv']").value;
    const nombreTitular = document.querySelector("input[name='nombreTitular']").value;

    const btn = document.getElementById("btnRegistrar");
    btn.disabled = !(tipoPago && observaciones && numTarjeta && fechaVenc && cvv && nombreTitular);
}

function limpiarCampos() {
    document.getElementById("formPago").reset();
    document.getElementById("mora").value = "0.00";
    document.getElementById("total").value = "0.00";
    document.getElementById("btnRegistrar").disabled = true;
}

// ✅ Se agregan listeners globales a todos los campos del formulario
document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("#formPago input, #formPago select")
        .forEach(el => el.addEventListener("input", habilitarBoton));
});
</script>
</body>
</html>
