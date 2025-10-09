<%-- 
    Document   : paqueteria
    Created on : 7/10/2025, 02:15:58 PM
    Author     : mpelv
--%>
<%@page contentType="text/html; charset=UTF-8"%>
<%@page import="java.util.*, Modelo.Usuarios, Modelo.Paquete"%>

<!DOCTYPE html>
<html>
<head>
    <title>Recepción de Paquetería</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>



<body class="bg-light">

<div class="container py-4">
    <h3 class="text-center mb-4">📦 Recepción de Paquetería</h3>

    <!-- Mensajes de sesión -->
    <%
        String msg = (String) session.getAttribute("msg");
        if (msg != null) {
    %>
        <div class="alert alert-info alert-dismissible fade show" role="alert">
            <%= msg %>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    <%
            session.removeAttribute("msg");
        }
    %>

    <!-- Formulario de registro de paquete -->
    <form action="${pageContext.request.contextPath}/PaqueteServlet" method="post" class="card shadow p-4 mb-5">
        <div class="mb-3">
            <label for="numeroGuia" class="form-label">Número de guía</label>
            <input type="text" name="numeroGuia" id="numeroGuia" class="form-control" required>
        </div>

       <div class="mb-3">
    <label for="idResidente" class="form-label">Nombre del destinatario</label>
    <select name="idResidente" id="idResidente" class="form-select" required onchange="actualizarCasa()">
        <option value="">Seleccione un residente</option>
        <%
            List<Usuarios> residentes = (List<Usuarios>) request.getAttribute("residentes");
            if (residentes != null) {
                for (Usuarios r : residentes) {
        %>
                    <option value="<%= r.getId() %>" data-casa="<%= r.getNumeroCasa() %>">
                        <%= r.getNombres() %> <%= r.getApellidos() %>
                    </option>
        <%
                }
            }
        %>
    </select>
</div>

<div class="mb-3">
    <label for="numeroCasa" class="form-label">Número de casa</label>
    <input type="text" id="numeroCasa" name="numeroCasa" class="form-control" readonly>
</div>


        <div class="d-flex justify-content-between">
            <button type="submit" class="btn btn-primary">Guardar</button>
            <button type="reset" class="btn btn-secondary">Limpiar</button>
        </div>
    </form>

    <!-- Lista de paquetes pendientes -->
    <div class="mt-3">
        <h5> Lista de paquetes pendientes de entrega</h5>
        <%
            List<Paquete> paquetes = (List<Paquete>) request.getAttribute("paquetes");
            if (paquetes == null || paquetes.isEmpty()) {
        %>
            <div class="alert alert-info mt-3">No hay paquetería pendiente de entregar</div>
        <%
            } else {
        %>
            <!-- Filtro de búsqueda -->
            <input class="form-control mb-3" id="filtro" type="text" placeholder="Buscar...">

            <table class="table table-bordered table-striped" id="tablaPaquetes">
                <thead class="table-dark">
                    <tr>
                        <th>#</th>
                        <th>Número de guía</th>
                        <th>Residente</th>
                        <th>Casa</th>
                        <th>Acción</th>
                    </tr>
                </thead>
                <tbody>
                <%
                    int index = 1;
                    for (Paquete p : paquetes) {
                %>
                    <tr>
                        <td><%= index++ %></td>
                        <td><%= p.getNumeroGuia() %></td>
                        <td><%= p.getNombreResidente() %></td>
                        <td><%= p.getNumeroCasa() != null ? p.getNumeroCasa() : "-" %></td>
                       <td>
    <button type="button" 
            class="btn btn-success btn-sm btn-entregar" 
            data-id="<%= p.getIdPaquete() %>">
        Entregar
    </button>
</td>
                    </tr>
                <%
                    }
                %>
                </tbody>
            </table>
        <%
            }
        %>
    </div>
</div>

    
    
<!-- Modal de confirmación -->
<div class="modal fade" id="confirmModal" tabindex="-1" aria-labelledby="confirmModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content">
      <div class="modal-header bg-warning text-dark">
        <h5 class="modal-title" id="confirmModalLabel">Confirmar entrega</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
      </div>
      <div class="modal-body">
        ¿Está seguro de realizar la entrega de este paquete?
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">No</button>
        <button type="button" id="confirmYesBtn" class="btn btn-success">Sí</button>
      </div>
    </div>
  </div>
</div>

    
<script>
document.addEventListener('DOMContentLoaded', function() {

    // Actualizar número de casa automáticamente al seleccionar residente
    function actualizarCasa() {
        const select = document.getElementById("idResidente");
        const inputCasa = document.getElementById("numeroCasa");
        const casa = select.options[select.selectedIndex].dataset.casa;
        inputCasa.value = casa || "";
    }
    document.getElementById("idResidente").addEventListener("change", actualizarCasa);

    // Filtro de búsqueda en la tabla
    const filtro = document.getElementById("filtro");
    if (filtro) {
        filtro.addEventListener("keyup", function () {
            const filter = this.value.toLowerCase();
            document.querySelectorAll("#tablaPaquetes tbody tr").forEach(row => {
                row.style.display = row.textContent.toLowerCase().includes(filter) ? "" : "none";
            });
        });
    }

    // Confirmación al entregar paquete
    const confirmModal = new bootstrap.Modal(document.getElementById('confirmModal'));
    let paqueteId = null;

    document.querySelectorAll('.btn-entregar').forEach(btn => {
        btn.addEventListener('click', function() {
            paqueteId = this.dataset.id;
            confirmModal.show();
        });
    });

    document.getElementById('confirmYesBtn').addEventListener('click', function() {
        if(paqueteId) {
            window.location.href = '${pageContext.request.contextPath}/PaqueteServlet?accion=entregar&id=' + paqueteId;
        }
    });

});
</script>


<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
