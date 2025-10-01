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


import Modelo.Pago;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PagoDAO {
    private Connection con;

    public PagoDAO(Connection con) {
        this.con = con;
    }

    public void registrarPago(Pago pago) throws SQLException {
        String sql = "INSERT INTO pagos (id_usuario, tipo_pago, cantidad, fecha_pago, observaciones, numero_tarjeta, fecha_vencimiento, cvv, nombre_titular, mora, total) "
                   + "VALUES (?, ?, ?, NOW(), ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pago.getIdUsuario());
            ps.setString(2, pago.getTipoPago());
            ps.setDouble(3, pago.getCantidad());
            ps.setString(4, pago.getObservaciones());
            ps.setString(5, pago.getNumeroTarjeta());
            ps.setDate(6, new java.sql.Date(pago.getFechaVencimiento().getTime()));
            ps.setString(7, pago.getCvv());
            ps.setString(8, pago.getNombreTitular());
            ps.setDouble(9, pago.getMora());
            ps.setDouble(10, pago.getTotal());
            ps.executeUpdate();
        }
    }

    public List<Pago> listarPagosPorUsuario(int idUsuario) throws SQLException {
        List<Pago> lista = new ArrayList<>();
        String sql = "SELECT * FROM pagos WHERE id_usuario=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Pago p = new Pago();
                    p.setIdPago(rs.getInt("id_pago"));
                    p.setIdUsuario(rs.getInt("id_usuario"));
                    p.setTipoPago(rs.getString("tipo_pago"));
                    p.setCantidad(rs.getDouble("cantidad"));
                    p.setFechaPago(rs.getTimestamp("fecha_pago"));
                    p.setObservaciones(rs.getString("observaciones"));
                    p.setNumeroTarjeta(rs.getString("numero_tarjeta"));
                    p.setFechaVencimiento(rs.getDate("fecha_vencimiento"));
                    p.setCvv(rs.getString("cvv"));
                    p.setNombreTitular(rs.getString("nombre_titular"));
                    p.setMora(rs.getDouble("mora"));
                    p.setTotal(rs.getDouble("total"));
                    lista.add(p);
                }
            }
        }
        return lista;
    }
    
    public Pago obtenerUltimoPagoPorTipo(int idUsuario, String tipoPago) throws SQLException {
    String sql = "SELECT * FROM pagos WHERE id_usuario=? AND tipo_pago=? ORDER BY fecha_pago DESC LIMIT 1";
    try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, idUsuario);
        ps.setString(2, tipoPago);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                Pago p = new Pago();
                p.setIdPago(rs.getInt("id_pago"));
                p.setIdUsuario(rs.getInt("id_usuario"));
                p.setTipoPago(rs.getString("tipo_pago"));
                p.setCantidad(rs.getDouble("cantidad"));
                p.setFechaPago(rs.getTimestamp("fecha_pago"));
                p.setObservaciones(rs.getString("observaciones"));
                p.setNumeroTarjeta(rs.getString("numero_tarjeta"));
                p.setFechaVencimiento(rs.getDate("fecha_vencimiento"));
                p.setCvv(rs.getString("cvv"));
                p.setNombreTitular(rs.getString("nombre_titular"));
                p.setMora(rs.getDouble("mora"));
                p.setTotal(rs.getDouble("total"));
                return p;
            }
        }
    }
    return null; // No hay pagos previos de ese tipo
}

}

