package ModeloDAO;

import Config.Conexion;
import Modelo.Incidente;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IncidenteDAO {

    public List<Incidente> listar() throws SQLException {
        String sql = "SELECT id, residente_id, tipo, fecha_hora, descripcion FROM incidentes ORDER BY fecha_hora DESC";
        List<Incidente> lista = new ArrayList<>();

        try (Connection con = new Conexion().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Incidente(
                        rs.getInt("id"),
                        rs.getInt("residente_id"),
                        rs.getString("tipo"),
                        rs.getTimestamp("fecha_hora"),
                        rs.getString("descripcion")
                ));
            }
        }

        return lista;
    }

   
      public int guardar(Incidente incidente) throws SQLException {
        String sql = "INSERT INTO incidentes (residente_id, tipo, fecha_hora, descripcion) VALUES (?, ?, ?, ?)";
        int idGenerado = -1;

        try (Connection con = new Conexion().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, incidente.getResidenteId());
            ps.setString(2, incidente.getTipo());
            ps.setTimestamp(3, new Timestamp(incidente.getFechaHora().getTime()));
            ps.setString(4, incidente.getDescripcion());

            int filas = ps.executeUpdate();

            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                    }
                }
            }
        }

        return idGenerado;
    }
    
    public List<String> listarTipos() throws SQLException {
        String sql = "SELECT id, nombre FROM catalogos WHERE catalogo = 4 ORDER BY nombre";
        List<String> lista = new ArrayList<>();

        try (Connection con = new Conexion().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(rs.getString("nombre"));
            }
        }

        return lista;
    }
}
