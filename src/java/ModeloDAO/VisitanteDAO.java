/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ModeloDAO;

/**
 *
 * @author mpelv
 */


import Config.Conexion;
import Modelo.Visitante;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VisitanteDAO {

    private final Conexion cn = new Conexion(); // Ajusta a tu clase de conexión

    // Listar todos (últimos primero)
   public List<Visitante> listar() {
    List<Visitante> out = new ArrayList<>();
    String sql = "SELECT id, nombre, dpi, tipo_visita, fecha_visita, intentos, correo, residente, qr_codigo, fecha_registro " +
                 "FROM visitantes ORDER BY id DESC";
    Connection con = cn.getConnection();
    try (PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        while (rs.next()) out.add(map(rs));
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return out;
}


    // Obtener por id
    public Visitante obtenerPorId(int id) {
        String sql = "SELECT id, nombre, dpi, tipo_visita, fecha_visita, intentos, correo, residente, qr_codigo, fecha_registro " +
                     "FROM visitantes WHERE id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

 public int registrarYRetornarId(Visitante v) throws SQLException {
    String sql = "INSERT INTO visitantes " +
        "(nombre, dpi, tipo_visita, fecha_visita, intentos, correo, residente, qr_codigo, fecha_registro) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";

    Connection con = cn.getConnection();
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
        ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, v.getNombre());
        ps.setString(2, v.getDpi());
        ps.setString(3, v.getTipoVisita());
        if (v.getFechaVisita() != null) ps.setDate(4, new java.sql.Date(v.getFechaVisita().getTime()));
        else ps.setNull(4, java.sql.Types.DATE);
        ps.setInt(5, v.getIntentos());
        ps.setString(6, v.getCorreo());
        ps.setString(7, v.getResidente());
        ps.setString(8, v.getQrCodigo());

        int rows = ps.executeUpdate();
        if (rows == 0) throw new SQLException("INSERT no afectó filas");

        rs = ps.getGeneratedKeys();
        if (rs.next()) return rs.getInt(1);

        // Fallback con la MISMA conexión
        try (PreparedStatement ps2 = con.prepareStatement("SELECT LAST_INSERT_ID()");
             ResultSet rs2 = ps2.executeQuery()) {
            if (rs2.next()) return rs2.getInt(1);
        }
        throw new SQLException("No se obtuvo ID generado");
    } finally {
        if (rs != null) try { rs.close(); } catch (Exception ignore) {}
        if (ps != null) try { ps.close(); } catch (Exception ignore) {}
        // NO cierres 'con' aquí; es la singleton
    }
}




    // Eliminar por id (FA06 cancelar) — si luego manejas estados, cambia a UPDATE estado = 'CANCELADO'
  public boolean eliminar(int id) {
    String sql = "DELETE FROM visitantes WHERE id = ?";
    Connection con = cn.getConnection();
    try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, id);
        return ps.executeUpdate() > 0; // true si borró registros
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}



    // (Opcional) Guardar token/código QR si decides persistirlo
    public boolean actualizarQrCodigo(int id, String qrCodigo) {
        String sql = "UPDATE visitantes SET qr_codigo = ? WHERE id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, qrCodigo);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Mapeo ResultSet -> Visitante
    private Visitante map(ResultSet rs) throws SQLException {
        Visitante v = new Visitante();
        v.setId(rs.getInt("id"));
        v.setNombre(rs.getString("nombre"));
        v.setDpi(rs.getString("dpi"));
        v.setTipoVisita(rs.getString("tipo_visita"));

        Date sqlDate = rs.getDate("fecha_visita");
        if (sqlDate != null) v.setFechaVisita(new java.util.Date(sqlDate.getTime()));

        // Si la columna existe, úsala; si tu esquema la tiene NOT NULL, no habrá problema
        int intentos = 0;
        try { intentos = rs.getInt("intentos"); if (rs.wasNull()) intentos = 0; } catch (SQLException ignore) {}
        v.setIntentos(intentos);

        String correo = null;
        try { correo = rs.getString("correo"); } catch (SQLException ignore) {}
        v.setCorreo(correo);

        v.setResidente(rs.getString("residente"));

        try { v.setQrCodigo(rs.getString("qr_codigo")); } catch (SQLException ignore) {}

        Timestamp ts = null;
        try { ts = rs.getTimestamp("fecha_registro"); } catch (SQLException ignore) {}
        if (ts != null) v.setFechaRegistro(new java.util.Date(ts.getTime()));

        return v;
    }

}