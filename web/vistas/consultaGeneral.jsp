<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="Modelo.Conversacion, Modelo.Mensaje, java.util.List" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Consulta General</title>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css">
<style>
body { background: #f8f9fa; }
#listaConvs { max-height: 80vh; overflow-y: auto; }
#chatContainer { max-height: 80vh; display: flex; flex-direction: column; border-left: 1px solid #ccc; }
#chatArea { flex: 1; overflow-y: auto; padding: 10px; display: flex; flex-direction: column; }
.chat-bubble { display: inline-block; padding: 10px; margin: 5px; border-radius: 15px; max-width: 70%; word-wrap: break-word; }
.left { background-color: #e2e2e2; text-align: left; }
.right { background-color: #0d6efd; color: white; text-align: right; align-self: flex-end; }
.conversacion-activa { background-color: #d1e7dd; }
#chatInputWrapper { padding: 10px; border-top: 1px solid #ccc; background: #fff; }
</style>
</head>
<body>
<div class="container-fluid">
<div class="row">

    <!-- Lista de conversaciones -->
    <div class="col-3 bg-light" id="listaConvs">
        <h5>Conversaciones</h5>
  <% 
    // Obtener la sesión y el usuario actual
    HttpSession sesion = request.getSession(false);
    if (sesion != null && sesion.getAttribute("usuario") != null) {
        Modelo.Usuarios usuario = (Modelo.Usuarios) sesion.getAttribute("usuario");
        int idRol = usuario.getIdRol();
        
        // Mostrar el botón solo si NO es residente (rol = 3)
        if (idRol != 2) { 
%>
    <a href="crearConversacion" class="btn btn-sm btn-primary mb-2">Crear nueva conversación</a>
<% 
        } 
    } 
%>

<a href="<%= request.getContextPath() %>/vistas/comunicacionInterna.jsp" class="btn btn-sm btn btn-secondary mb-2">Cancelar</a>

        <ul class="list-group">
            <c:forEach var="conv" items="${conversaciones}">
                <li class="list-group-item ${conv.id == convSeleccionadaId ? 'conversacion-activa' : ''}">
                    <a href="consultaGeneral?convId=${conv.id}">
                        <c:choose>
                            <c:when test="${usuarioActual.id == conv.residenteId}">
                                Agente: ${conv.agenteNombre}
                            </c:when>
                            <c:otherwise>
                                Residente: ${conv.residenteNombre}
                            </c:otherwise>
                        </c:choose>
                    </a>
                </li>
            </c:forEach>
        </ul>
    </div>

    <!-- Chat -->
    <div class="col-9" id="chatContainer">
        <c:forEach var="conv" items="${conversaciones}">
            <c:if test="${conv.id == convSeleccionadaId}">
                <h5 class="mt-2 mb-2">
                    <c:choose>
                        <c:when test="${usuarioActual.id == conv.residenteId}">
                            Chat con Agente: ${conv.agenteNombre}
                        </c:when>
                        <c:otherwise>
                            Chat con Residente: ${conv.residenteNombre}
                        </c:otherwise>
                    </c:choose>
                </h5>

                <!-- Contenedor de mensajes -->
                <div id="chatArea">
                    <c:forEach var="msg" items="${conv.mensajes}">
                        <div class="chat-bubble ${msg.emisorId == usuarioActual.id ? 'right' : 'left'}">
                            ${msg.mensaje} <br>
                            <small><c:out value="${msg.fechaHora}" /></small>
                        </div>
                    </c:forEach>
                </div>

                <!-- Formulario de envío -->
                <div id="chatInputWrapper">
                    <form id="formMensaje" class="d-flex">
                        <input type="hidden" id="conversacionId" value="${conv.id}" />
                        <input type="hidden" id="receptorId" value="${usuarioActual.id == conv.residenteId ? conv.agenteId : conv.residenteId}" />
                        <input type="text" id="inputMensaje" class="form-control me-2" placeholder="Escribe tu mensaje" required />
                        <button class="btn btn-success" type="submit">Enviar</button>
                    </form>
                </div>
            </c:if>
        </c:forEach>
    </div>

</div>
</div>

<script>
const usuarioId = <c:out value="${usuarioActual.id}" />;

// Función para mostrar mensaje en chat
function agregarMensaje(data) {
    const conversacionId = parseInt(document.getElementById('conversacionId').value);
    if(data.conversacionId === conversacionId){
        const div = document.createElement('div');
        div.className = data.emisorId === usuarioId ? 'chat-bubble right' : 'chat-bubble left';
        const fecha = new Date(data.fechaHora).toLocaleString();
        div.innerHTML = data.mensaje + " <br><small>" + fecha + "</small>";
        const chatArea = document.getElementById('chatArea');
        chatArea.appendChild(div);
        chatArea.scrollTop = chatArea.scrollHeight;
    }
}

// Conectar WebSocket
function conectarWebSocket() {
    const ws = new WebSocket("ws://" + location.host + "<%= request.getContextPath() %>/chat/" + usuarioId);

    ws.onopen = () => console.log("Conectado al WebSocket");

    ws.onmessage = (event) => {
        const data = JSON.parse(event.data);
        agregarMensaje(data);
    };

    ws.onclose = () => {
        console.log("WebSocket cerrado, reconectando en 2s...");
        setTimeout(conectarWebSocket, 2000);
    };

    // Enviar mensaje
    document.getElementById('formMensaje').addEventListener('submit', function(e){
        e.preventDefault();
        const mensaje = document.getElementById('inputMensaje').value.trim();
        if(!mensaje) return;

        const conversacionId = parseInt(document.getElementById('conversacionId').value);
        const receptorId = parseInt(document.getElementById('receptorId').value);

        const mensajeJson = JSON.stringify({
            conversacionId,
            emisorId: usuarioId,
            receptorId,
            mensaje
        });

        ws.send(mensajeJson);
        document.getElementById('inputMensaje').value = '';
    });
}

// Inicializar WebSocket
conectarWebSocket();
</script>
</body>
</html>