/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ModeloDAO;

import Config.Conexion;
import Modelo.Reserva;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservaDAO {
    private Connection con;

    public ReservaDAO(Connection con) {
        this.con = con;
    }
 
    
       // Constructor que abre conexión automáticamente
    public ReservaDAO() {
        Conexion conexion = new Conexion();
        this.con = conexion.getConnection();
    }

    
    
    public List<Reserva> listarPorUsuario(String correoUsuario) throws SQLException {
    List<Reserva> lista = new ArrayList<>();
    String sql = "SELECT * FROM reservas WHERE estado='activa' AND residente_correo = ?";
    try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setString(1, correoUsuario);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Reserva r = new Reserva();
                r.setId(rs.getInt("id"));
                r.setSalon(rs.getString("salon"));
                r.setResidenteNombre(rs.getString("residente_nombre"));
                r.setResidenteCorreo(rs.getString("residente_correo"));
                r.setFecha(rs.getDate("fecha"));
                r.setHoraInicio(rs.getTime("hora_inicio"));
                r.setHoraFin(rs.getTime("hora_fin"));
                r.setEstado(rs.getString("estado"));
                lista.add(r);
            }
        }
    }
    return lista;
}

    
/*
    public List<Reserva> listar() throws SQLException {
        List<Reserva> lista = new ArrayList<>();
        String sql = "SELECT * FROM reservas WHERE estado='activa'";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Reserva r = new Reserva();
                r.setId(rs.getInt("id"));
                r.setSalon(rs.getString("salon"));
                r.setResidenteNombre(rs.getString("residente_nombre"));
                r.setResidenteCorreo(rs.getString("residente_correo"));
                r.setFecha(rs.getDate("fecha"));
                r.setHoraInicio(rs.getTime("hora_inicio"));
                r.setHoraFin(rs.getTime("hora_fin"));
                r.setEstado(rs.getString("estado"));
                lista.add(r);
            }
        }
        return lista;
    }
*/
    public int registrar(Reserva r) throws SQLException {
        String sql = "INSERT INTO reservas(salon, residente_nombre, residente_correo, fecha, hora_inicio, hora_fin, estado) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getSalon());
            ps.setString(2, r.getResidenteNombre());
            ps.setString(3, r.getResidenteCorreo());
            ps.setDate(4, r.getFecha());
            ps.setTime(5, r.getHoraInicio());
            ps.setTime(6, r.getHoraFin());
            ps.setString(7, r.getEstado());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return 0;
    }

    public boolean cancelar(int id) throws SQLException {
        String sql = "UPDATE reservas SET estado = 'cancelada' WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean estaDisponible(String salon, Date fecha, Time hi, Time hf) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservas "
                   + "WHERE salon = ? AND fecha = ? AND estado = 'activa' "
                   + "AND ((hora_inicio < ? AND hora_fin > ?) "
                   + "OR (hora_inicio < ? AND hora_fin > ?) "
                   + "OR (hora_inicio >= ? AND hora_fin <= ?))";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, salon);
            ps.setDate(2, fecha);
            ps.setTime(3, hf);
            ps.setTime(4, hi);
            ps.setTime(5, hi);
            ps.setTime(6, hf);
            ps.setTime(7, hi);
            ps.setTime(8, hf);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        }
        return false;
    }
}

