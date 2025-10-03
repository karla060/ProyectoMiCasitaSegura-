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
import java.util.Calendar;
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
    // Obtener la fecha de creación del usuario
    AuditoriaSistemaDAO auditoriaDAO = new AuditoriaSistemaDAO();
    java.util.Date fechaCreacion = auditoriaDAO.obtenerFechaCreacionUsuarioPorId(idUsuario);
    if (fechaCreacion == null) {
        // No se encontró fecha de creación, asumimos que no requiere reinstalación
        return false;
    }

    Calendar inicio = Calendar.getInstance();
    inicio.setTime(fechaCreacion);

    Calendar hoy = Calendar.getInstance(); // fecha actual

    // Contador de meses atrasados
    int mesesAtrasados = 0;

    try (Connection con = this.con) {
        String sql = "SELECT mes_pagado FROM pagos " +
                     "WHERE id_usuario = ? AND tipo_pago = 'Mantenimiento' " +
                     "ORDER BY mes_pagado ASC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                List<java.util.Date> pagos = new ArrayList<>();
                while (rs.next()) {
                    pagos.add(rs.getDate("mes_pagado"));
                }

                // Recorrer mes por mes desde la creación hasta hoy
                Calendar mesActual = (Calendar) inicio.clone();
                int i = 0;
                while (mesActual.before(hoy)) {
                    java.util.Date mesPagado = (i < pagos.size()) ? pagos.get(i) : null;
                    if (mesPagado == null || mesPagado.before(mesActual.getTime())) {
                        // Si no hay pago para ese mes o se pagó después → contar como atraso
                        mesesAtrasados++;
                    }
                    mesActual.add(Calendar.MONTH, 1);
                    i++;
                }
            }
        }
    }

    // Retornar true si hay 3 o más meses de mantenimiento atrasados
    return mesesAtrasados >= 3;
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


public int contarMesesAtrasados(int idUsuario, Date fechaCreacionUsuario) throws SQLException {
    // Buscar último pago de mantenimiento
    String sql = "SELECT mes_pagado FROM pagos WHERE id_usuario=? AND tipo_pago='Mantenimiento' ORDER BY mes_pagado DESC LIMIT 1";
    Date ultimoPago = null;

    try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, idUsuario);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                ultimoPago = rs.getDate("mes_pagado");
            }
        }
    }

    // Si nunca ha pagado → usar fecha de creación
    Date inicio = (ultimoPago != null) ? ultimoPago : fechaCreacionUsuario;

    Calendar calInicio = Calendar.getInstance();
    calInicio.setTime(inicio);
    calInicio.add(Calendar.MONTH, 1); // se supone que el primer mes ya está cubierto

    Calendar calHoy = Calendar.getInstance();

    int mesesAtrasados = 0;
    while (calInicio.before(calHoy)) {
        mesesAtrasados++;
        calInicio.add(Calendar.MONTH, 1);
    }

    return mesesAtrasados;
}

}
