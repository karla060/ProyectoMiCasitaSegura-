package ModeloDAO;

import Modelo.Conversacion;
import Config.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConversacionDAO {
    private Connection con = new Conexion().getConnection();
    private MensajeDAO mensajeDAO = new MensajeDAO();

     public List<Conversacion> listarPorUsuario(int usuarioId, int idRol) {
        List<Conversacion> lista = new ArrayList<>();
        String sql;
        if (idRol == 3) { // Residente
            sql = "SELECT c.*, r.nombres AS residenteNombre, a.nombres AS agenteNombre " +
                  "FROM conversaciones c " +
                  "JOIN usuarios r ON c.residente_id = r.id_usuario " +
                  "JOIN usuarios a ON c.agente_id = a.id_usuario " +
                  "WHERE c.residente_id = ?";
        } else { // Agente
            sql = "SELECT c.*, r.nombres AS residenteNombre, a.nombres AS agenteNombre " +
                  "FROM conversaciones c " +
                  "JOIN usuarios r ON c.residente_id = r.id_usuario " +
                  "JOIN usuarios a ON c.agente_id = a.id_usuario " +
                  "WHERE c.agente_id = ?";
        }

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                Conversacion conv = new Conversacion(
                    rs.getInt("id"),
                    rs.getInt("residente_id"),
                    rs.getInt("agente_id")
                );
                conv.setResidenteNombre(rs.getString("residenteNombre"));
                conv.setAgenteNombre(rs.getString("agenteNombre"));
                conv.setMensajes(mensajeDAO.listarMensajes(conv.getId()));
                lista.add(conv);
            }
        } catch(SQLException e){ e.printStackTrace(); }
        return lista;
    }
     
     // ConversacionDAO.java
public boolean existeConversacion(int idResidente, int idAgente) {
    String sql = "SELECT COUNT(*) FROM conversaciones WHERE residente_id = ? AND agente_id = ?";
    try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, idResidente);
        ps.setInt(2, idAgente);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) > 0; // retorna true si ya existe
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}

     

    // Crear nueva conversación
   public void crearConversacion(int idResidente, int idAgente) {
        String sql = "INSERT INTO conversaciones (residente_id, agente_id) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idResidente);
            ps.setInt(2, idAgente);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error creando conversación: " + e.getMessage());
        }
    }
   
}
