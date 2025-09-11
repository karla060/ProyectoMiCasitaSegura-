/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ModeloDAO;

import Config.Conexion;
import Modelo.Visitante;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VisitanteDAO {

    // Ya no mantenemos una conexión persistente
    private final Conexion cn = new Conexion();
    private Connection con;
        // Constructor: mantiene la conexión abierta mientras dure el DAO
    public VisitanteDAO() {
        this.con = new Conexion().getConnection();
    }

    // Listar todos los visitantes (últimos primero)
    public List<Visitante> listar() {
        List<Visitante> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, dpi, tipo_visita, fecha_visita, intentos, correo, residente, qr_codigo, fecha_registro, dentro " +
                     "FROM visitantes ORDER BY id DESC";

        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(map(rs));
            }

        } catch (SQLException e) {
            System.err.println("[VisitanteDAO] Error al listar: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }

    // Obtener visitante por ID
    public Visitante obtenerPorId(int id) {
        String sql = "SELECT id, nombre, dpi, tipo_visita, fecha_visita, intentos, correo, residente, qr_codigo, fecha_registro, dentro " +
                     "FROM visitantes WHERE id = ?";

        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("[VisitanteDAO] Error al obtener por ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // Registrar visitante y retornar ID generado
    public int registrarYRetornarId(Visitante v) throws SQLException {
        String sql = "INSERT INTO visitantes (nombre, dpi, tipo_visita, fecha_visita, intentos, correo, residente, qr_codigo, fecha_registro) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";

        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, v.getNombre());
            ps.setString(2, v.getDpi());
            ps.setString(3, v.getTipoVisita());
            if (v.getFechaVisita() != null) {
                ps.setDate(4, new java.sql.Date(v.getFechaVisita().getTime()));
            } else {
                ps.setNull(4, Types.DATE);
            }
            ps.setInt(5, v.getIntentos() != null ? v.getIntentos() : 0);
            ps.setString(6, v.getCorreo());
            ps.setString(7, v.getResidente());
            ps.setString(8, v.getQrCodigo());

            int filas = ps.executeUpdate();
            if (filas == 0) throw new SQLException("Registro no afectó filas");

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }

            throw new SQLException("No se pudo obtener ID generado");
        }
    }

    // Eliminar visitante
    public boolean eliminar(int id) {
        String sql = "DELETE FROM visitantes WHERE id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[VisitanteDAO] Error al eliminar ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Actualizar QR
    public boolean actualizarQrCodigo(int id, String qrCodigo) {
        String sql = "UPDATE visitantes SET qr_codigo = ? WHERE id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, qrCodigo);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[VisitanteDAO] Error al actualizar QR ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    
    
    // Actualizar estado dentro
      public void actualizarEstado(int idVisitante, int dentro) {
        String sql = "UPDATE visitantes SET dentro=? WHERE id=?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, dentro);
            ps.setInt(2, idVisitante);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[VisitanteDAO] Error actualizarEstado: " + e.getMessage());
        }
    }


    
        // Restar intento si corresponde
   public void restarIntentoSiCorresponde(int idVisitante) {
        String sql = "UPDATE visitantes "
                   + "SET intentos = COALESCE(intentos,0) - 1 "
                   + "WHERE id=? AND tipo_visita='Por intentos' AND COALESCE(intentos,0) > 0";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVisitante);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[VisitanteDAO] Error restarIntento: " + e.getMessage());
        }
    }
    
   
    // Registrar auditoría
    public void registrarAuditoria(int idVisitante, String accion) {
        String sql = "INSERT INTO auditoria_visitantes (id_visitante, accion) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVisitante);
            ps.setString(2, accion);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[VisitanteDAO] Error registrarAuditoria: " + e.getMessage());
        }
    }

        // Mapear ResultSet -> Visitante
    private Visitante map(ResultSet rs) throws SQLException {
        Visitante v = new Visitante();
        v.setId(rs.getInt("id"));
        v.setNombre(rs.getString("nombre"));
        v.setDpi(rs.getString("dpi"));
        v.setTipoVisita(rs.getString("tipo_visita"));
        v.setIntentos(rs.getInt("intentos"));
        v.setFechaVisita(rs.getDate("fecha_visita"));
        v.setCorreo(rs.getString("correo"));
        v.setResidente(rs.getString("residente"));
        v.setQrCodigo(rs.getString("qr_codigo"));
        v.setDentro(rs.getInt("dentro"));

        Timestamp ts = rs.getTimestamp("fecha_registro");
        if (ts != null) v.setFechaRegistro(new Date(ts.getTime()));

        return v;
    }
}





/*
    package ModeloDAO;

import Config.Conexion;
import Modelo.Visitante;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VisitanteDAO {

    private final Conexion cn = new Conexion(); // Clase de conexión

    // Listar todos los visitantes (últimos primero)
    public List<Visitante> listar() {
        List<Visitante> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, dpi, tipo_visita, fecha_visita, intentos, correo, residente, qr_codigo, fecha_registro, dentro " +
                     "FROM visitantes ORDER BY id DESC";

        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(map(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    // Obtener visitante por ID
    public Visitante obtenerPorId(int id) {
        String sql = "SELECT id, nombre, dpi, tipo_visita, fecha_visita, intentos, correo, residente, qr_codigo, fecha_registro, dentro " +
                     "FROM visitantes WHERE id = ?";

        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Registrar visitante y retornar ID
    public int registrarYRetornarId(Visitante v) throws SQLException {
        String sql = "INSERT INTO visitantes " +
                     "(nombre, dpi, tipo_visita, fecha_visita, intentos, correo, residente, qr_codigo, fecha_registro) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";

        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, v.getNombre());
            ps.setString(2, v.getDpi());
            ps.setString(3, v.getTipoVisita());
            if (v.getFechaVisita() != null) ps.setDate(4, new java.sql.Date(v.getFechaVisita().getTime()));
            else ps.setNull(4, Types.DATE);
            ps.setInt(5, v.getIntentos() != null ? v.getIntentos() : 0);
            ps.setString(6, v.getCorreo());
            ps.setString(7, v.getResidente());
            ps.setString(8, v.getQrCodigo());

            int filas = ps.executeUpdate();
            if (filas == 0) throw new SQLException("Registro no afectó filas");

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }

            throw new SQLException("No se pudo obtener ID generado");
        }
    }

    // Eliminar visitante
    public boolean eliminar(int id) {
        String sql = "DELETE FROM visitantes WHERE id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Actualizar QR
    public boolean actualizarQrCodigo(int id, String qrCodigo) {
        String sql = "UPDATE visitantes SET qr_codigo = ? WHERE id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, qrCodigo);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Actualizar estado "dentro"
    public boolean actualizarEstado(int idVisitante, int dentro) {
        String sql = "UPDATE visitantes SET dentro = ? WHERE id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, dentro);
            ps.setInt(2, idVisitante);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Restar intento si corresponde
    public boolean restarIntentoSiCorresponde(int idVisitante) {
        String sql = "UPDATE visitantes " +
                     "SET intentos = COALESCE(intentos,0) - 1 " +
                     "WHERE id = ? AND TRIM(UPPER(tipo_visita)) = 'POR INTENTOS' AND COALESCE(intentos,0) > 0";

        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idVisitante);
            int filas = ps.executeUpdate();
            System.out.println("[DAO] Intentos restados (ID=" + idVisitante + "): " + filas);
            return filas > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Registrar auditoría
    public void registrarAuditoria(int idVisitante, String accion) {
        String sql = "INSERT INTO auditoria_visitantes (id_visitante, accion) VALUES (?, ?)";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idVisitante);
            ps.setString(2, accion);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Mapeo ResultSet -> Visitante
    private Visitante map(ResultSet rs) throws SQLException {
        Visitante v = new Visitante();
        v.setId(rs.getInt("id"));
        v.setNombre(rs.getString("nombre"));
        v.setDpi(rs.getString("dpi"));
        String tipo = rs.getString("tipo_visita");
        v.setTipoVisita(tipo != null ? tipo.trim() : null);

        Date fv = rs.getDate("fecha_visita");
        if (fv != null) v.setFechaVisita(new java.util.Date(fv.getTime()));

        int intentos = rs.getInt("intentos");
        v.setIntentos(rs.wasNull() ? 0 : intentos);

        v.setCorreo(rs.getString("correo"));
        v.setResidente(rs.getString("residente"));
        v.setQrCodigo(rs.getString("qr_codigo"));

        Timestamp ts = rs.getTimestamp("fecha_registro");
        if (ts != null) v.setFechaRegistro(new java.util.Date(ts.getTime()));

        v.setDentro(rs.getInt("dentro"));
        return v;
    }
}
*/





/*
package ModeloDAO;



import Config.Conexion;
import Modelo.Visitante;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VisitanteDAO {

    private final Conexion cn = new Conexion(); // Ajusta a tu clase de conexión

    // Listar todos (últimos primero)
   public List<Visitante> listar() {
    List<Visitante> out = new ArrayList<>();
    String sql = "SELECT id, nombre, dpi, tipo_visita, fecha_visita, intentos, correo, residente, qr_codigo, fecha_registro " +
                 "FROM visitantes ORDER BY id DESC";
    Connection con = cn.getConnection();
    try (PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        while (rs.next()) out.add(map(rs));
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return out;
}


    // Obtener por id
    public Visitante obtenerPorId(int id) {
        String sql = "SELECT id, nombre, dpi, tipo_visita, fecha_visita, intentos, correo, residente, qr_codigo, fecha_registro " +
                     "FROM visitantes WHERE id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

 public int registrarYRetornarId(Visitante v) throws SQLException {
    String sql = "INSERT INTO visitantes " +
        "(nombre, dpi, tipo_visita, fecha_visita, intentos, correo, residente, qr_codigo, fecha_registro) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";

    Connection con = cn.getConnection();
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
        ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, v.getNombre());
        ps.setString(2, v.getDpi());
        ps.setString(3, v.getTipoVisita());
        if (v.getFechaVisita() != null) ps.setDate(4, new java.sql.Date(v.getFechaVisita().getTime()));
        else ps.setNull(4, java.sql.Types.DATE);
        ps.setInt(5, v.getIntentos());
        ps.setString(6, v.getCorreo());
        ps.setString(7, v.getResidente());
        ps.setString(8, v.getQrCodigo());

        int rows = ps.executeUpdate();
        if (rows == 0) throw new SQLException("INSERT no afectó filas");

        rs = ps.getGeneratedKeys();
        if (rs.next()) return rs.getInt(1);

        // Fallback con la MISMA conexión
        try (PreparedStatement ps2 = con.prepareStatement("SELECT LAST_INSERT_ID()");
             ResultSet rs2 = ps2.executeQuery()) {
            if (rs2.next()) return rs2.getInt(1);
        }
        throw new SQLException("No se obtuvo ID generado");
    } finally {
        if (rs != null) try { rs.close(); } catch (Exception ignore) {}
        if (ps != null) try { ps.close(); } catch (Exception ignore) {}
        // NO cierres 'con' aquí; es la singleton
    }
}




    // Eliminar por id (FA06 cancelar) — si luego manejas estados, cambia a UPDATE estado = 'CANCELADO'
  public boolean eliminar(int id) {
    String sql = "DELETE FROM visitantes WHERE id = ?";
    Connection con = cn.getConnection();
    try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, id);
        return ps.executeUpdate() > 0; // true si borró registros
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}



    // (Opcional) Guardar token/código QR si decides persistirlo
    public boolean actualizarQrCodigo(int id, String qrCodigo) {
        String sql = "UPDATE visitantes SET qr_codigo = ? WHERE id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, qrCodigo);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Mapeo ResultSet -> Visitante
    private Visitante map(ResultSet rs) throws SQLException {
        Visitante v = new Visitante();
        v.setId(rs.getInt("id"));
        v.setNombre(rs.getString("nombre"));
        v.setDpi(rs.getString("dpi"));
        v.setTipoVisita(rs.getString("tipo_visita") == null ? null : rs.getString("tipo_visita").trim());

        //v.setTipoVisita(rs.getString("tipo_visita"));

        Date sqlDate = rs.getDate("fecha_visita");
        if (sqlDate != null) v.setFechaVisita(new java.util.Date(sqlDate.getTime()));

        // Si la columna existe, úsala; si tu esquema la tiene NOT NULL, no habrá problema
        int intentos = 0;
        try { intentos = rs.getInt("intentos"); if (rs.wasNull()) intentos = 0; } catch (SQLException ignore) {}
        v.setIntentos(intentos);

        String correo = null;
        try { correo = rs.getString("correo"); } catch (SQLException ignore) {}
        v.setCorreo(correo);

        v.setResidente(rs.getString("residente"));

        try { v.setQrCodigo(rs.getString("qr_codigo")); } catch (SQLException ignore) {}

        Timestamp ts = null;
        try { ts = rs.getTimestamp("fecha_registro"); } catch (SQLException ignore) {}
        if (ts != null) v.setFechaRegistro(new java.util.Date(ts.getTime()));

        return v;
    }
    
        // Busca un visitante por su ID.
    public Visitante buscarPorId(int id) {
        String sql = 
        "SELECT id, nombre, dpi, tipo_visita, fecha_visita, " +
        "intentos, correo, residente, qr_codigo, fecha_registro, dentro " +
        "FROM visitantes WHERE id = ?";
;
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Visitante v = new Visitante();
                    v.setId(rs.getInt("id"));
                    v.setNombre(rs.getString("nombre"));
                    v.setDpi(rs.getString("dpi"));
                    v.setTipoVisita(rs.getString("tipo_visita") == null ? null : rs.getString("tipo_visita").trim());
                    //v.setTipoVisita(rs.getString("tipo_visita"));
                    Date fv = rs.getDate("fecha_visita");
                    if (fv != null) {
                        v.setFechaVisita(new java.util.Date(fv.getTime()));
                    }
                    int cnt = rs.getInt("intentos");
                    v.setIntentos(rs.wasNull() ? 0 : cnt);
                    v.setCorreo(rs.getString("correo"));
                    v.setResidente(rs.getString("residente"));
                    v.setQrCodigo(rs.getString("qr_codigo"));
                    Timestamp ts = rs.getTimestamp("fecha_registro");
                    if (ts != null) {
                        v.setFechaRegistro(new java.util.Date(ts.getTime()));
                    }
                    v.setDentro(rs.getInt("dentro"));
                    return v;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error buscando visitante por ID: " + e.getMessage());
        }
        return null;
    }

    // Actualiza el campo 'dentro' de un visitante.
    public void actualizarEstado(int idVisitante, int dentro) {
        String sql = "UPDATE visitantes SET dentro = ? WHERE id = ?";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, dentro);
            ps.setInt(2, idVisitante);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error actualizando estado (visitante): " + e.getMessage());
        }
    }

    
    
 //Resta un intento si el visitante es de tipo 'Por intentos' y tiene intentos > 0.
 //@param idVisitante el ID del visitante
 //@return true si se restó correctamente (fila afectada), false en caso contrario
 
    
    
    // Resta un intento en Java y luego persiste el nuevo valor.
 //@return true si se actualiza la BD, false si no corresponde o falla.

    
    public boolean restarIntentoSiCorresponde(int idVisitante) {
    String sql = "UPDATE visitantes " +
                 "SET intentos = COALESCE(intentos,0) - 1 " +
                 "WHERE id = ? AND TRIM(UPPER(tipo_visita)) = 'POR INTENTOS' AND COALESCE(intentos,0) > 0";

    try (Connection con = cn.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, idVisitante);
        int filas = ps.executeUpdate();
        System.out.println("[DAO] Intentos restados (ID=" + idVisitante + "): " + filas);
        return filas > 0;

    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}
    
        public void registrarAuditoria(int idVisitante, String accion) {
        String sql = "INSERT INTO auditoria_visitantes (id_visitante, accion) VALUES (?, ?)";
        try (Connection con = cn.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idVisitante);
            ps.setString(2, accion);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
*/