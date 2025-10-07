package websocket;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.*;
import com.google.gson.Gson;
import Modelo.Mensaje;
import ModeloDAO.MensajeDAO;

@ServerEndpoint("/chat/{usuarioId}")
public class ChatWebSocket {

    // Map global usuarioId -> lista de sesiones (pestañas)
    private static final Map<Integer, List<Session>> usuariosConectados = Collections.synchronizedMap(new HashMap<>());
    private static final Gson gson = new Gson();
    private final MensajeDAO mensajeDAO = new MensajeDAO();

    @OnOpen
    public void onOpen(Session session, @PathParam("usuarioId") int usuarioId) {
        usuariosConectados.computeIfAbsent(usuarioId, k -> Collections.synchronizedList(new ArrayList<>())).add(session);
        System.out.println("Usuario conectado: " + usuarioId);
    }

    @OnMessage
    public void onMessage(String mensajeJson, Session session) {
        try {
            Map<String, Object> data = gson.fromJson(mensajeJson, Map.class);
            int conversacionId = ((Double) data.get("conversacionId")).intValue();
            int emisorId = ((Double) data.get("emisorId")).intValue();
            int receptorId = ((Double) data.get("receptorId")).intValue();
            String contenido = (String) data.get("mensaje");

            // Guardar mensaje en BD
            Mensaje msg = new Mensaje();
            msg.setConversacionId(conversacionId);
            msg.setEmisorId(emisorId);
            msg.setMensaje(contenido);
            msg.setFechaHora(new Date());

            if (!mensajeDAO.guardarMensaje(msg)) {
                System.out.println("Error al guardar mensaje en BD");
                return;
            }
            
            // Crear instancia del servicio de correo (puedes usar los datos que ya tengas en EmailService)
        service.EmailService emailService = new service.EmailService(
        "smtp.gmail.com", "587", "patzanpirirjefferson4@gmail.com", "qsym rtfd subg bgee", true
        );

        // Obtener los datos del emisor y receptor
        ModeloDAO.UsuariosDAO usuariosDAO = new ModeloDAO.UsuariosDAO();
        Modelo.Usuarios emisor = usuariosDAO.buscarPorId(emisorId);
        Modelo.Usuarios receptor = usuariosDAO.buscarPorId(receptorId);

// Enviar notificación en un hilo aparte para no bloquear el chat
if (receptor.getCorreo() != null && !receptor.getCorreo().isEmpty()) {
    new Thread(() -> emailService.notificarMensaje(emisor.getNombres(), receptor.getCorreo())).start();
}

            
            // Crear JSON para enviar a ambos usuarios
            Map<String, Object> jsonEnviar = new HashMap<>();
            jsonEnviar.put("conversacionId", msg.getConversacionId());
            jsonEnviar.put("emisorId", msg.getEmisorId());
            jsonEnviar.put("mensaje", msg.getMensaje());
            jsonEnviar.put("fechaHora", msg.getFechaHora().getTime());
            String jsonString = gson.toJson(jsonEnviar);

            // Enviar a todas las sesiones del emisor
            enviarASesiones(emisorId, jsonString);

            // Enviar a todas las sesiones del receptor
            enviarASesiones(receptorId, jsonString);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void enviarASesiones(int usuarioId, String mensaje) {
        List<Session> sesiones = usuariosConectados.get(usuarioId);
        if (sesiones != null) {
            synchronized (sesiones) {
                for (Session s : sesiones) {
                    if (s.isOpen()) {
                        s.getAsyncRemote().sendText(mensaje);
                    }
                }
            }
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("usuarioId") int usuarioId) {
        List<Session> sesiones = usuariosConectados.get(usuarioId);
        if (sesiones != null) {
            sesiones.remove(session);
            if (sesiones.isEmpty()) usuariosConectados.remove(usuarioId);
        }
        System.out.println("Usuario desconectado: " + usuarioId);
    }

    @OnError
    public void onError(Session session, Throwable thr) {
        thr.printStackTrace();
    }
}