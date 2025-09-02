/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ModeloDAO;

import Modelo.Casa;
import Config.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CasaDAO {
    public List<Casa> listar() throws SQLException {
        String sql = "SELECT id_casa, numero_casa FROM casas ORDER BY id_casa";
        List<Casa> lista = new ArrayList<>();

        try (Connection con = new Conexion().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Casa(
                  rs.getInt("id_casa"),
                  rs.getInt("numero_casa")
                ));
            }
        }
        return lista;
    }
}
