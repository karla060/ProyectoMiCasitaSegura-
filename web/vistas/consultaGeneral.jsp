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
body {
  background: linear-gradient(135deg, #f0f4f8, #d9e6f2);
  height: 100vh;
  overflow: hidden;
}

h5 {
  font-weight: 600;
  color: #0d6efd;
}

#listaConvs {
  max-height: 85vh;
  overflow-y: auto;
  border-right: 1px solid #ddd;
  padding: 15px;
  background: #fff;
  border-radius: 15px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

#chatContainer {
  display: flex;
  flex-direction: column;
  height: 85vh;
  border-radius: 15px;
  background: #ffffff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  padding: 0;
}

#chatArea {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  background: #f9fafc;
  border-radius: 15px 15px 0 0;
}

.chat-bubble {
  display: inline-block;
  padding: 12px 18px;
  margin: 8px;
  border-radius: 18px;
  max-width: 70%;
  word-wrap: break-word;
  position: relative;
  font-size: 0.95rem;
  line-height: 1.4;
  box-shadow: 0 1px 4px rgba(0,0,0,0.1);
  transition: all 0.3s ease;
}

.chat-bubble.left {
  background-color: #e6e6e6;
  color: #333;
  align-self: flex-start;
  border-bottom-left-radius: 5px;
}

.chat-bubble.right {
  background: linear-gradient(135deg, #0d6efd, #468ef7);
  color: #fff;
  align-self: flex-end;
  border-bottom-right-radius: 5px;
}

.chat-bubble small {
  font-size: 0.75rem;
  opacity: 0.8;
}

.conversacion-activa {
  background: linear-gradient(135deg, #d1e7dd, #bcd7c9) !important;
  font-weight: 600;
}

.list-group-item a {
  text-decoration: none;
  color: #333;
}

.list-group-item:hover {
  background-color: #e8f0fe;
  cursor: pointer;
}

#chatInputWrapper {
  padding: 12px 16px;
  border-top: 1px solid #ddd;
  background: #fff;
  border-radius: 0 0 15px 15px;
}

#inputMensaje {
  border-radius: 25px;
  padding-left: 20px;
}

.btn-success {
  border-radius: 25px;
  padding: 8px 20px;
}

::-webkit-scrollbar {
  width: 8px;
}
::-webkit-scrollbar-thumb {
  background-color: rgba(0,0,0,0.2);
  border-radius: 4px;
}
::-webkit-scrollbar-thumb:hover {
  background-color: rgba(0,0,0,0.3);
}
</style>
</head>
<body>
<div class="container-fluid py-3">
<div class="row g-3">

    <!-- Lista de conversaciones -->
    <div class="col-3" id="listaConvs">
        <h5 class="mb-3">Conversaciones</h5>
  <% 
    HttpSession sesion = request.getSession(false);
    if (sesion != null && sesion.getAttribute("usuario") != null) {
        Modelo.Usuarios usuario = (Modelo.Usuarios) sesion.getAttribute("usuario");
        int idRol = usuario.getIdRol();
        if (idRol != 2) { 
  %>
      <a href="crearConversacion" class="btn btn-sm btn-primary mb-2 w-100">Nueva conversación</a>
  <% 
        } 
    } 
  %>
      <a href="<%= request.getContextPath() %>/vistas/comunicacionInterna.jsp" class="btn btn-sm btn-secondary w-100 mb-3">Cancelar</a>

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
                <div class="px-4 pt-3 border-bottom">
                    <h5>
                        <c:choose>
                            <c:when test="${usuarioActual.id == conv.residenteId}">
                                Chat con <span class="text-primary">${conv.agenteNombre}</span>
                            </c:when>
                            <c:otherwise>
                                Chat con <span class="text-success">${conv.residenteNombre}</span>
                            </c:otherwise>
                        </c:choose>
                    </h5>
                </div>

                <div id="chatArea">
                    <c:forEach var="msg" items="${conv.mensajes}">
                        <div class="chat-bubble ${msg.emisorId == usuarioActual.id ? 'right' : 'left'}">
                            ${msg.mensaje} <br>
                            <small><c:out value="${msg.fechaHora}" /></small>
                        </div>
                    </c:forEach>
                </div>

                <div id="chatInputWrapper" class="d-flex">
                    <form id="formMensaje" class="d-flex w-100">
                        <input type="hidden" id="conversacionId" value="${conv.id}" />
                        <input type="hidden" id="receptorId" value="${usuarioActual.id == conv.residenteId ? conv.agenteId : conv.residenteId}" />
                        <input type="text" id="inputMensaje" class="form-control me-2" placeholder="Escribe tu mensaje..." required />
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

conectarWebSocket();
</script>
</body>
</html>
