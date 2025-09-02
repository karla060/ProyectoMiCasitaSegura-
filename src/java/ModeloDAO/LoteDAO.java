/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ModeloDAO;

import Config.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import Modelo.Lote;

public class LoteDAO {
    public List<Lote> listar() throws SQLException {
        String sql = "SELECT id_lote, nombre_lote FROM lotes ORDER BY nombre_lote";
        List<Lote> lista = new ArrayList<>();

        try (Connection con = new Conexion().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Lote(
                  rs.getInt("id_lote"),
                  rs.getString("nombre_lote")
                ));
            }
        }
        return lista;
    }

}
