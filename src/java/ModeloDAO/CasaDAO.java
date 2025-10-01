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
import Modelo.Casa;

public class CasaDAO {
    public List<Casa> listar() throws SQLException {
        String sql = "SELECT id, nombre AS numero_casa FROM catalogos WHERE catalogo = 2 ORDER BY id";
        List<Casa> lista = new ArrayList<>();

        try (Connection con = new Conexion().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Casa(
                  rs.getInt("id"),
                  Integer.parseInt(rs.getString("numero_casa"))
                ));
            }
        }
        return lista;
    }
}

