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
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import Modelo.TipoDePago;
public class TipoDePagoDAO {
 
     public List<TipoDePago> listar() throws SQLException {
        String sql = "SELECT id, nombre FROM catalogos WHERE catalogo = 3 ORDER BY id";
        List<TipoDePago> lista = new ArrayList<>();

        try (Connection con = new Conexion().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new TipoDePago(
                    rs.getInt("id"),
                    rs.getString("nombre")
                ));
            }
        }
        return lista;
    }
    
}
