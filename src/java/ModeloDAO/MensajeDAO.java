package ModeloDAO;

import Modelo.Mensaje;
import Config.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class MensajeDAO {
    private Connection con = new Conexion().getConnection();

    public boolean guardarMensaje(Mensaje msg) {
        String sql = "INSERT INTO mensajes (conversacion_id, emisor_id, contenido, fecha_envio) VALUES (?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, msg.getConversacionId());
            ps.setInt(2, msg.getEmisorId());
            ps.setString(3, msg.getMensaje());
            ps.setTimestamp(4, new Timestamp(msg.getFechaHora().getTime()));
            return ps.executeUpdate() > 0;
        } catch(SQLException e) { e.printStackTrace(); return false; }
    }

    public List<Mensaje> listarMensajes(int conversacionId) {
        List<Mensaje> lista = new ArrayList<>();
        String sql = "SELECT * FROM mensajes WHERE conversacion_id=? ORDER BY fecha_envio ASC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, conversacionId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                Mensaje m = new Mensaje(
                    rs.getInt("id"),
                    rs.getInt("conversacion_id"),
                    rs.getInt("emisor_id"),
                    rs.getString("contenido"),
                    rs.getTimestamp("fecha_envio")
                );
                lista.add(m);
            }
        } catch(SQLException e) { e.printStackTrace(); }
        return lista;
    }


    // Obtener destinatario de la conversación
    public int obtenerDestinatario(int conversacionId, int emisorId) {
        String sql = "SELECT residente_id, agente_id FROM conversacion WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, conversacionId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                int residenteId = rs.getInt("residente_id");
                int agenteId = rs.getInt("agente_id");
                return (emisorId == residenteId) ? agenteId : residenteId;
            }
        } catch(SQLException e){ e.printStackTrace(); }
        return -1;
    }
}
