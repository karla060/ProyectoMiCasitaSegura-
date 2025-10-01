/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ModeloDAO;

import Config.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class AuditoriaSistemaDAO {
    private final Conexion cn = new Conexion();

    public boolean registrar(String usuario, String accion, String detalle) {
        String sql = "INSERT INTO auditoria_sistema (usuario, accion, detalle, fecha) " +
                     "VALUES (?, ?, ?, NOW())";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, accion);
            ps.setString(3, detalle);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AuditoriaSistemaDAO] Error al registrar auditoría: " + e.getMessage());
            return false;
        }
    }
    
    public Date obtenerFechaCreacionUsuarioPorId(int idUsuario) {
    Date fechaCreacion = null;
    String sql = "SELECT fecha FROM auditoria_sistema " +
                 "WHERE accion = 'Creación de usuario' " +
                 "AND detalle LIKE ? " +
                 "ORDER BY fecha ASC LIMIT 1";

    try (Connection con = cn.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setString(1, "ID=" + idUsuario + "%"); // buscamos el ID al inicio del detalle
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                fechaCreacion = rs.getTimestamp("fecha");
            }
        }
    } catch (SQLException e) {
        System.err.println("[AuditoriaSistemaDAO] Error al obtener fecha creación: " + e.getMessage());
    }
    return fechaCreacion;
}

    
    
}


