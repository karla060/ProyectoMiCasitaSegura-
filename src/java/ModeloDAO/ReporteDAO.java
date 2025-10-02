/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ModeloDAO;

import Modelo.ReporteMantenimiento;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.DriverManager;

public class ReporteDAO {

    // Constructor vacío ya no necesita recibir Connection
    public ReporteDAO() { }

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





/*
package ModeloDAO;

import Modelo.ReporteMantenimiento;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class ReporteDAO {
    private Connection con;

    public ReporteDAO(Connection con) {
        this.con = con;
    }

    public void guardar(ReporteMantenimiento reporte) throws Exception {
        String sql = "INSERT INTO reportes_mantenimiento(tipo_inconveniente, descripcion, fecha_hora, id_usuario) VALUES (?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, reporte.getTipoInconveniente());
            ps.setString(2, reporte.getDescripcion());
            ps.setTimestamp(3, new java.sql.Timestamp(reporte.getFechaHora().getTime()));
            ps.setInt(4, reporte.getIdUsuario());
            ps.executeUpdate();
        }
    }
}
*/
