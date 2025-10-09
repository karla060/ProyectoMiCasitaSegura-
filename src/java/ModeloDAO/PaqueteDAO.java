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
import Modelo.Paquete;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaqueteDAO {

    private Connection con;

    public PaqueteDAO() {
        this.con = new Conexion().getConnection();
    }

   
   public boolean registrar(Paquete p) {
    String sql = "INSERT INTO paqueteria (numero_guia, id_residente, fecha_registro, entregado) VALUES (?, ?, NOW(), 0)";
    try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setString(1, p.getNumeroGuia());
        ps.setInt(2, p.getIdResidente());
        int filas = ps.executeUpdate();
        return filas > 0;
    } catch (SQLException e) {
        System.err.println("Error al registrar paquete: " + e.getMessage());
        return false;
    }
}


    // Listar paquetes pendientes con nombre de residente y número de casa
   // Listar paquetes pendientes con nombre de residente y número de casa
public List<Paquete> listarPendientes() {
    List<Paquete> lista = new ArrayList<>();
    String sql = "SELECT p.id_paq, p.numero_guia, p.id_residente, p.fecha_registro, " +
                 "u.nombres, u.apellidos, c.nombre AS numero_casa " +
                 "FROM paqueteria p " +
                 "JOIN usuarios u ON p.id_residente = u.id_usuario " +
                 "LEFT JOIN catalogos c ON u.id_casa = c.id AND c.catalogo = 2 " +
                 "WHERE p.entregado = 0";

    try (PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            Paquete p = new Paquete();
            p.setIdPaquete(rs.getInt("id_paq"));
            p.setNumeroGuia(rs.getString("numero_guia"));
            p.setIdResidente(rs.getInt("id_residente"));
            p.setNombreResidente(rs.getString("nombres") + " " + rs.getString("apellidos"));
            p.setNumeroCasa(rs.getString("numero_casa"));
            p.setFechaRegistro(rs.getTimestamp("fecha_registro"));
            lista.add(p);
        }

    } catch (SQLException e) {
        System.err.println("Error listando paquetes: " + e.getMessage());
    }
    return lista;
}


    public boolean marcarEntregado(int id) {
    String sql = "UPDATE paqueteria SET entregado = 1, fecha_entrega = NOW() WHERE id_paq = ?";
    try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, id);
        return ps.executeUpdate() > 0;
    } catch (SQLException e) {
        System.err.println("Error al marcar como entregado: " + e.getMessage());
        return false;
    }
}

    
    public Paquete obtenerPorId(int id) {
    Paquete p = null;
    String sql = "SELECT p.id_paq, p.numero_guia, p.id_residente, u.nombres, u.apellidos " +
                 "FROM paqueteria p " +
                 "JOIN usuarios u ON p.id_residente = u.id_usuario " +
                 "WHERE p.id_paq = ?";
    try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, id);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                p = new Paquete();
                p.setIdPaquete(rs.getInt("id_paq"));
                p.setNumeroGuia(rs.getString("numero_guia"));
                p.setIdResidente(rs.getInt("id_residente"));
                p.setNombreResidente(rs.getString("nombres") + " " + rs.getString("apellidos"));
            }
        }
    } catch (SQLException e) {
        System.err.println("Error obteniendo paquete: " + e.getMessage());
    }
    return p;
}

    
}
