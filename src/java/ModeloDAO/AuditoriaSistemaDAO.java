/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ModeloDAO;

import Config.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
}


