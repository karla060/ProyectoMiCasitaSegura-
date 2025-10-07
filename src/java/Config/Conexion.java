/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    // URL de conexión con parámetros recomendados
    private static final String URL =
        "jdbc:mysql://localhost:3308/casita"
      + "?useSSL=false"
      + "&serverTimezone=UTC"
      + "&autoReconnect=true";
    private static final String USER = "root";
    private static final String PASS = "";

    // Registro del driver una sola vez
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver MySQL no encontrado", e);
        }
    }

    /**
     * Abre y devuelve una conexión nueva.
     * Quien llame debe cerrarla
     * (idealmente con try-with-resources).
     */
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e) {
            throw new RuntimeException("Error al abrir conexión con la base de datos", e);
        }
    }
}