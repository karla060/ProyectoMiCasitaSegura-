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

    // ✅ Registrar un nuevo pago
    public void registrarPago(Pago pago) throws SQLException {
       String sql = "INSERT INTO pagos (id_usuario, tipo_pago, cantidad, fecha_pago, mes_pagado, observaciones, numero_tarjeta, fecha_vencimiento, cvv, nombre_titular, mora, total) "
           + "VALUES (?, ?, ?, NOW(), ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pago.getIdUsuario());
            ps.setString(2, pago.getTipoPago());
            ps.setDouble(3, pago.getCantidad());
            //  Manejo de mes_pagado opcional
            if (pago.getMesPagado() != null) {
                ps.setDate(4, new java.sql.Date(pago.getMesPagado().getTime()));
            } else {
                ps.setNull(4, java.sql.Types.DATE);
           }
            ps.setString(5, pago.getObservaciones());
            ps.setString(6, pago.getNumeroTarjeta());
            ps.setDate(7, new java.sql.Date(pago.getFechaVencimiento().getTime()));
            ps.setString(8, pago.getCvv());
            ps.setString(9, pago.getNombreTitular());
            ps.setDouble(10, pago.getMora());
            ps.setDouble(11, pago.getTotal());
            ps.executeUpdate();
        }
    }

    // ✅ Listar todos los pagos de un usuario
    public List<Pago> listarPagosPorUsuario(int idUsuario) throws SQLException {
        List<Pago> lista = new ArrayList<>();
        String sql = "SELECT * FROM pagos WHERE id_usuario=? ORDER BY fecha_pago DESC";
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
                    p.setMesPagado(rs.getDate("mes_pagado")); 
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
   String sql = "SELECT * FROM pagos WHERE id_usuario=? AND tipo_pago=? ORDER BY mes_pagado DESC LIMIT 1";

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
                p.setMesPagado(rs.getDate("mes_pagado")); // ✅ nuevo campo
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
    return null; // no hay pagos previos de ese tipo
}

    
  public List<java.util.Date> obtenerMesesAtrasados(int idUsuario) throws SQLException {
    List<java.util.Date> mesesAtrasados = new ArrayList<>();
    
    String sql = "SELECT mes_pagado FROM pagos " +
                 "WHERE id_usuario = ? AND tipo_pago = 'Mantenimiento' " +
                 "AND mes_pagado < CURDATE() " +
                 "ORDER BY mes_pagado ASC"; // orden ascendente, del más antiguo al más reciente
    
    try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, idUsuario);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                mesesAtrasados.add(rs.getDate("mes_pagado"));
            }
        }
    }
    
    return mesesAtrasados; // lista vacía si no hay atrasos
}


    
    
   public boolean necesitaReinstalacion(int idUsuario) throws SQLException {
    String sql = "SELECT COUNT(*) FROM pagos " +
                 "WHERE id_usuario = ? AND tipo_pago = 'Mantenimiento' " +
                 "AND mes_pagado < CURDATE()"; 
    try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, idUsuario);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) >= 2; // Reinstalación si hay 2 o más pagos atrasados
            }
        }
    }
    return false;
}

public boolean multaPagada(int idUsuario, Date mes) throws SQLException {
    String sql = "SELECT COUNT(*) FROM pagos WHERE id_usuario=? AND tipo_pago='Multa' AND mes_pagado=?";
    try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, idUsuario);
        ps.setDate(2, new java.sql.Date(mes.getTime()));
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) > 0; // true si ya se pagó la multa
            }
        }
    }
    return false;
}

}
