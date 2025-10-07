/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ModeloDAO;

import Config.Conexion;
import Modelo.ReporteMantenimiento;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReporteDAO {

    // Constructor vacío ya no necesita recibir Connection
    public ReporteDAO() { }
    
    
    // 🔹 Obtener lista de tipos de inconvenientes desde catálogo
public List<String> listarTiposInconvenientes() throws SQLException {
    List<String> tipos = new ArrayList<>();
    String sql = "SELECT nombre FROM catalogos WHERE catalogo = 6 ORDER BY id";

    try (Connection con = new Conexion().getConnection();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            tipos.add(rs.getString("nombre"));
        }
    }

    return tipos;
}


    public void guardar(ReporteMantenimiento reporte) throws Exception {
        // Configura tu conexión según tu BD
        String url = "jdbc:mysql://localhost:3308/casita";
        String user = "root";
        String pass = "";

        String sql = "INSERT INTO reportes_mantenimiento(tipo_inconveniente, descripcion, fecha_hora, id_usuario) VALUES (?,?,?,?)";

        try (Connection con = DriverManager.getConnection(url, user, pass);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, reporte.getTipoInconveniente());
            ps.setString(2, reporte.getDescripcion());
            ps.setTimestamp(3, new java.sql.Timestamp(reporte.getFechaHora().getTime()));
            ps.setInt(4, reporte.getIdUsuario());
            ps.executeUpdate();
        }
    }
}

