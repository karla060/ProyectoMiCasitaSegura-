/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ModeloDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import Modelo.Roles;

public class RolesDAO {

    private static final String URL = "jdbc:mysql://localhost:3308/casita";
    private static final String USER = "root";
    private static final String PASS = "";

    public static List<Roles> obtenerTodos() throws SQLException {
        List<Roles> lista = new ArrayList<>();
        String sql = "SELECT id_rol, nombre_rol FROM roles ORDER BY nombre_rol";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id   = rs.getInt("id_rol");
                String nombre = rs.getString("nombre_rol");
                lista.add(new Roles(id, nombre));
            }
        }

        return lista;
    }
}
