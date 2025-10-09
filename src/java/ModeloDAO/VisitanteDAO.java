/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ModeloDAO;

import Config.Conexion;
import Modelo.Visitante;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VisitanteDAO {

    // Ya no mantenemos una conexión persistente
    private final Conexion cn = new Conexion();
    private Connection con;
        // Constructor: mantiene la conexión abierta mientras dure el DAO
    public VisitanteDAO() {
        this.con = new Conexion().getConnection();
    }

    
    // 🔹 Listar visitantes por correo del residente
public List<Visitante> listarPorUsuario(String correoResidente) {
    List<Visitante> lista = new ArrayList<>();
    String sql = "SELECT id, nombre, dpi, tipo_visita, fecha_visita, intentos, correo, residente, qr_codigo, fecha_registro, dentro " +
                 "FROM visitantes WHERE residente = ? ORDER BY id DESC";

    try (Connection con = cn.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, correoResidente);

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(map(rs));
            }
        }

    } catch (SQLException e) {
        System.err.println("[VisitanteDAO] Error al listar por usuario: " + e.getMessage());
        e.printStackTrace();
    }

    return lista;
}


    
    
   // Listar todos los visitantes (últimos primero)
    public List<Visitante> listar() {
        List<Visitante> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, dpi, tipo_visita, fecha_visita, intentos, correo, residente, qr_codigo, fecha_registro, dentro " +
                     "FROM visitantes ORDER BY id DESC";

        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(map(rs));
            }

        } catch (SQLException e) {
            System.err.println("[VisitanteDAO] Error al listar: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }

    // Obtener visitante por ID
    public Visitante obtenerPorId(int id) {
        String sql = "SELECT id, nombre, dpi, tipo_visita, fecha_visita, intentos, correo, residente, qr_codigo, fecha_registro, dentro " +
                     "FROM visitantes WHERE id = ?";

        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("[VisitanteDAO] Error al obtener por ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // Registrar visitante y retornar ID generado
    public int registrarYRetornarId(Visitante v) throws SQLException {
        String sql = "INSERT INTO visitantes (nombre, dpi, tipo_visita, fecha_visita, intentos, correo, residente, qr_codigo, fecha_registro) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";

        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, v.getNombre());
            ps.setString(2, v.getDpi());
            ps.setString(3, v.getTipoVisita());
            if (v.getFechaVisita() != null) {
                ps.setDate(4, new java.sql.Date(v.getFechaVisita().getTime()));
            } else {
                ps.setNull(4, Types.DATE);
            }
            ps.setInt(5, v.getIntentos() != null ? v.getIntentos() : 0);
            ps.setString(6, v.getCorreo());
            ps.setString(7, v.getResidente());
            ps.setString(8, v.getQrCodigo());

            int filas = ps.executeUpdate();
            if (filas == 0) throw new SQLException("Registro no afectó filas");

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }

            throw new SQLException("No se pudo obtener ID generado");
        }
    }

    // Eliminar visitante
    public boolean eliminar(int id) {
        String sql = "DELETE FROM visitantes WHERE id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[VisitanteDAO] Error al eliminar ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Actualizar QR
    public boolean actualizarQrCodigo(int id, String qrCodigo) {
        String sql = "UPDATE visitantes SET qr_codigo = ? WHERE id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, qrCodigo);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[VisitanteDAO] Error al actualizar QR ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    
    
    // Actualizar estado dentro
      public void actualizarEstado(int idVisitante, int dentro) {
        String sql = "UPDATE visitantes SET dentro=? WHERE id=?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, dentro);
            ps.setInt(2, idVisitante);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[VisitanteDAO] Error actualizarEstado: " + e.getMessage());
        }
    }


    
        // Restar intento si corresponde
   public void restarIntentoSiCorresponde(int idVisitante) {
        String sql = "UPDATE visitantes "
                   + "SET intentos = COALESCE(intentos,0) - 1 "
                   + "WHERE id=? AND tipo_visita='Por intentos' AND COALESCE(intentos,0) > 0";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVisitante);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[VisitanteDAO] Error restarIntento: " + e.getMessage());
        }
    }
    
   
    // Registrar auditoría
    public void registrarAuditoria(int idVisitante, String accion) {
        String sql = "INSERT INTO auditoria_visitantes (id_visitante, accion) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVisitante);
            ps.setString(2, accion);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[VisitanteDAO] Error registrarAuditoria: " + e.getMessage());
        }
    }

        // Mapear ResultSet -> Visitante
    private Visitante map(ResultSet rs) throws SQLException {
        Visitante v = new Visitante();
        v.setId(rs.getInt("id"));
        v.setNombre(rs.getString("nombre"));
        v.setDpi(rs.getString("dpi"));
        v.setTipoVisita(rs.getString("tipo_visita"));
        v.setIntentos(rs.getInt("intentos"));
        v.setFechaVisita(rs.getDate("fecha_visita"));
        v.setCorreo(rs.getString("correo"));
        v.setResidente(rs.getString("residente"));
        v.setQrCodigo(rs.getString("qr_codigo"));
        v.setDentro(rs.getInt("dentro"));

        Timestamp ts = rs.getTimestamp("fecha_registro");
        if (ts != null) v.setFechaRegistro(new Date(ts.getTime()));

        return v;
    }
}

