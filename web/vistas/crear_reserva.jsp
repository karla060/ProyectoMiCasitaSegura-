<%-- 
    Document   : crear_reserva
    Created on : 10/09/2025, 08:51:32 PM
    Author     : gp
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="javax.servlet.http.HttpSession" %>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Crear Reserva</title>
    <!-- Bootstrap 5 CDN -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

<div class="container mt-5">
    <div class="card shadow-lg">
        <div class="card-header bg-primary text-white">
            <h3 class="mb-0">Registro de Reservas</h3>
        </div>
        <div class="card-body">

            <%
                HttpSession sess = request.getSession(false);
                String residenteNombre = "";
                if (sess != null && sess.getAttribute("usuarioNombre") != null) {
                    residenteNombre = (String) sess.getAttribute("usuarioNombre");
                }
            %>

            <form action="ReservaServlet" method="post">
                <input type="hidden" name="accion" value="registrar">

                <!-- Salón -->
                <div class="mb-3">
                    <label class="form-label">Salón para reservar:</label>
                    <select name="salon" class="form-select" required>
                        <option value="">--Seleccione--</option>
                        <option value="Salón">Salón</option>
                        <option value="Piscina">Piscina</option>
                    </select>
                </div>

                <!-- Residente -->
                <div class="mb-3">
                    <label class="form-label">Persona que reserva:</label>
                    <input type="text" name="residente" class="form-control"
                           value="<%= residenteNombre %>" readonly>
                </div>

                <!-- Fecha -->
                <div class="mb-3">
                    <label class="form-label">Fecha para reservar:</label>
                    <input type="date" name="fecha" class="form-control" required>
                </div>

                <!-- Hora inicio -->
                <div class="mb-3">
                    <label class="form-label">Hora Inicio:</label>
                    <input type="time" name="horaInicio" class="form-control" required>
                </div>

                <!-- Hora fin -->
                <div class="mb-3">
                    <label class="form-label">Hora Fin:</label>
                    <input type="time" name="horaFin" class="form-control" required>
                </div>

                <!-- Botones -->
                <div class="d-flex justify-content-between">
                    <a href="ReservaServlet?accion=listar" class="btn btn-secondary">Cancelar</a>
                    <button type="submit" id="btnRegistrar" class="btn btn-success" disabled>Registrar Reserva</button>

                </div>
            </form>

            <!-- Mensajes -->
            <div class="mt-3">
                <%
                    if (request.getAttribute("error") != null) {
                %>
                <div class="alert alert-danger"><%= request.getAttribute("error") %></div>
                <%
                    } else if (request.getAttribute("msg") != null) {
                %>
                <div class="alert alert-success"><%= request.getAttribute("msg") %></div>
                <%
                    }
                %>
            </div>

        </div>
    </div>
</div>

<!-- Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>

<script>
    // Seleccionamos todos los campos obligatorios
    const salon = document.querySelector('select[name="salon"]');
    const fecha = document.querySelector('input[name="fecha"]');
    const horaInicio = document.querySelector('input[name="horaInicio"]');
    const horaFin = document.querySelector('input[name="horaFin"]');
    const btnRegistrar = document.getElementById('btnRegistrar');

    function validarCampos() {
        if (salon.value && fecha.value && horaInicio.value && horaFin.value) {
            btnRegistrar.disabled = false;
        } else {
            btnRegistrar.disabled = true;
        }
    }

    // Validar al cambiar cada campo
    salon.addEventListener('change', validarCampos);
    fecha.addEventListener('input', validarCampos);
    horaInicio.addEventListener('input', validarCampos);
    horaFin.addEventListener('input', validarCampos);
</script>


</body>
</html>



