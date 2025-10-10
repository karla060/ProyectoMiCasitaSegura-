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

      // Registrar un nuevo pago
    public void registrarPago(Pago pago) throws SQLException {
    String sql = "INSERT INTO pagos (id_usuario, id_catalogo, cantidad, fecha_pago, mes_pagado, observaciones, numero_tarjeta, fecha_vencimiento, cvv, nombre_titular, mora, total) "
               + "VALUES (?, ?, ?, NOW(), ?, ?, ?, ?, ?, ?, ?, ?)";

    try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, pago.getIdUsuario());
        ps.setInt(2, pago.getIdCatalogo());
        ps.setDouble(3, pago.getCantidad());

        // Mes pagado puede ser nulo
        if (pago.getMesPagado() != null) {
            ps.setDate(4, new java.sql.Date(pago.getMesPagado().getTime()));
        } else {
            ps.setNull(4, java.sql.Types.DATE);
        }

        ps.setString(5, pago.getObservaciones());
        ps.setString(6, pago.getNumeroTarjeta());

        // Validar fecha de vencimiento
        if (pago.getFechaVencimiento() != null) {
            ps.setDate(7, new java.sql.Date(pago.getFechaVencimiento().getTime()));
        } else {
            ps.setNull(7, java.sql.Types.DATE);
        }

        ps.setString(8, pago.getCvv());
        ps.setString(9, pago.getNombreTitular());
        ps.setDouble(10, pago.getMora());
        ps.setDouble(11, pago.getTotal());

        ps.executeUpdate();
    }
}


   // Listar todos los pagos de un usuario
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
                    p.setIdCatalogo(rs.getInt("id_catalogo"));
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

    // Obtener último pago por catálogo
    public Pago obtenerUltimoPagoPorCatalogo(int idUsuario, int idCatalogo) throws SQLException {
        String sql = "SELECT * FROM pagos WHERE id_usuario=? AND id_catalogo=? ORDER BY mes_pagado DESC LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idCatalogo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Pago p = new Pago();
                    p.setIdPago(rs.getInt("id_pago"));
                    p.setIdUsuario(rs.getInt("id_usuario"));
                    p.setIdCatalogo(rs.getInt("id_catalogo"));
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
                    return p;
                }
            }
        }
        return null;
    }

   // Obtener meses de mantenimiento atrasados
public List<java.util.Date> obtenerMesesAtrasados(int idUsuario) throws SQLException {
    List<java.util.Date> mesesAtrasados = new ArrayList<>();
    
    String sql = "SELECT mes_pagado FROM pagos " +
                 "WHERE id_usuario = ? AND id_catalogo = ? " +
                 "AND mes_pagado < CURDATE() " +
                 "ORDER BY mes_pagado ASC";
    
    try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, idUsuario);
        ps.setInt(2, 77); // ID de mantenimiento
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                mesesAtrasados.add(rs.getDate("mes_pagado"));
            }
        }
    }
    
    return mesesAtrasados;
}

// Verificar si necesita reinstalación (3 o más meses sin pagar mantenimiento)
public boolean necesitaReinstalacion(int idUsuario) throws SQLException {
    AuditoriaSistemaDAO auditoriaDAO = new AuditoriaSistemaDAO();
    java.util.Date fechaCreacion = auditoriaDAO.obtenerFechaCreacionUsuarioPorId(idUsuario);
    if (fechaCreacion == null) return false;

    Calendar inicio = Calendar.getInstance();
    inicio.setTime(fechaCreacion);
    Calendar hoy = Calendar.getInstance();

    int mesesAtrasados = 0;

    String sql = "SELECT mes_pagado FROM pagos " +
                 "WHERE id_usuario = ? AND id_catalogo = ? " +
                 "ORDER BY mes_pagado ASC";
    
    try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, idUsuario);
        ps.setInt(2, 77); // ID de mantenimiento
        try (ResultSet rs = ps.executeQuery()) {
            List<java.util.Date> pagos = new ArrayList<>();
            while (rs.next()) {
                pagos.add(rs.getDate("mes_pagado"));
            }

            Calendar mesActual = (Calendar) inicio.clone();
            int i = 0;
            while (mesActual.before(hoy)) {
                java.util.Date mesPagado = (i < pagos.size()) ? pagos.get(i) : null;
                if (mesPagado == null || mesPagado.before(mesActual.getTime())) {
                    mesesAtrasados++;
                }
                mesActual.add(Calendar.MONTH, 1);
                i++;
            }
        }
    }

    return mesesAtrasados >= 3;
}

// Verificar si multa ya fue pagada
public boolean multaPagada(int idUsuario, java.util.Date mes) throws SQLException {
    String sql = "SELECT COUNT(*) FROM pagos WHERE id_usuario=? AND id_catalogo=? AND mes_pagado=?";
    try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, idUsuario);
        ps.setInt(2, 78); // ID de multa
        ps.setDate(3, new java.sql.Date(mes.getTime()));
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1) > 0;
        }
    }
    return false;
}

// Contar meses atrasados de mantenimiento
public int contarMesesAtrasados(int idUsuario, java.util.Date fechaCreacionUsuario) throws SQLException {
    String sql = "SELECT mes_pagado FROM pagos WHERE id_usuario=? AND id_catalogo=? ORDER BY mes_pagado DESC LIMIT 1";
    java.util.Date ultimoPago = null;

    try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, idUsuario);
        ps.setInt(2, 77); // ID de mantenimiento
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) ultimoPago = rs.getDate("mes_pagado");
        }
    }

    java.util.Date inicio = (ultimoPago != null) ? ultimoPago : fechaCreacionUsuario;
    Calendar calInicio = Calendar.getInstance();
    calInicio.setTime(inicio);
    calInicio.add(Calendar.MONTH, 1);
    Calendar calHoy = Calendar.getInstance();
    int mesesAtrasados = 0;
    while (calInicio.before(calHoy)) {
        mesesAtrasados++;
        calInicio.add(Calendar.MONTH, 1);
    }
    return mesesAtrasados;
}

// Reiniciar último mes de mantenimiento
public void reiniciarEstadoMantenimiento(int idUsuario) throws SQLException {
    String sql = "UPDATE pagos SET mes_pagado = CURDATE() WHERE id_usuario = ? AND id_catalogo=? ORDER BY mes_pagado DESC LIMIT 1";
    try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, idUsuario);
        ps.setInt(2, 77); // ID de mantenimiento
        ps.executeUpdate();
    }
}






}
