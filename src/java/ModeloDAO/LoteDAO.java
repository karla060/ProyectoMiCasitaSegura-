
package ModeloDAO;

import Config.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import Modelo.Lote;

public class LoteDAO {
    public List<Lote> listar() throws SQLException {
        String sql = "SELECT id, nombre AS nombre_lote FROM catalogos WHERE catalogo = 1 ORDER BY nombre";
        List<Lote> lista = new ArrayList<>();

        try (Connection con = new Conexion().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Lote(
                  rs.getInt("id"),
                  rs.getString("nombre_lote")
                ));
            }
        }
        return lista;
    }
}
