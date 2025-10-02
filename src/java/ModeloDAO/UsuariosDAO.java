/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ModeloDAO;

import java.sql.SQLException;

import Config.Conexion;
import Modelo.Usuarios;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuariosDAO {

    private Connection con;

    // ID de rol “Residente” en tu tabla roles
    public static final int ID_ROL_RESIDENTE = 3;

    public UsuariosDAO() {
        this.con = new Conexion().getConnection();
        
    }

    public UsuariosDAO(Connection con) {
    this.con = con;
}
    
    
    
    public boolean insertar(Usuarios usuario) {
        String sql = "INSERT INTO usuarios (dpi, nombres, apellidos, correo, contrasena, id_rol, id_lote, id_casa, activo) VALUES (?,?,?,?,?,?,?,?,?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, usuario.getDpi());
            ps.setString(2, usuario.getNombres());
            ps.setString(3, usuario.getApellidos());
            ps.setString(4, usuario.getCorreo());
            ps.setString(5, usuario.getContrasena());
            ps.setInt(6, usuario.getIdRol());

            if (usuario.getIdLote() != null) {
                ps.setInt(7, usuario.getIdLote());
            } else {
                ps.setNull(7, Types.INTEGER);
            }

            if (usuario.getIdCasa() != null) {
                ps.setInt(8, usuario.getIdCasa());
            } else {
                ps.setNull(8, Types.INTEGER);
            }

            ps.setBoolean(9, usuario.isActivo());

            int filas = ps.executeUpdate();
            if (filas == 0) return false;

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    usuario.setId(rs.getInt(1)); // asigna el ID autogenerado
                }
            }
            return true;

        } catch (SQLException e) {
            System.err.println("Error insertando usuario: " + e.getMessage());
            return false;
        }
    }

  
    public void eliminar(int idUsuario) {
        String sql = "DELETE FROM usuarios WHERE id_usuario = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            int filas = ps.executeUpdate();
            System.out.println("Filas eliminadas: " + filas);
        } catch (SQLException ex) {
            System.err.println("Error eliminando usuario: " + ex.getMessage());
        }
    }

  
    public List<Usuarios> listar() {
        List<Usuarios> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuarios";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Usuarios u = new Usuarios(
                    rs.getInt("id_usuario"),
                    rs.getString("dpi"),
                    rs.getString("nombres"),
                    rs.getString("apellidos"),
                    rs.getString("correo"),
                    rs.getString("contrasena"),
                    rs.getInt("id_rol"),
                    rs.getObject("id_lote") != null ? rs.getInt("id_lote") : null,
                    rs.getObject("id_casa") != null ? rs.getInt("id_casa") : null,
                    rs.getBoolean("activo"),
                    rs.getInt("dentro")
                );
                lista.add(u);
            }
        } catch (SQLException e) {
            System.err.println("Error listando usuarios: " + e.getMessage());
        }
        return lista;
    }

  
    public Usuarios buscarPorId(int id) {
        String sql = "SELECT * FROM usuarios WHERE id_usuario = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuarios u = new Usuarios();
                    u.setId(rs.getInt("id_usuario"));
                    u.setDpi(rs.getString("dpi"));
                    u.setNombres(rs.getString("nombres"));
                    u.setApellidos(rs.getString("apellidos"));
                    u.setCorreo(rs.getString("correo"));
                    u.setContrasena(rs.getString("contrasena"));
                    u.setIdRol(rs.getInt("id_rol"));
                    u.setIdLote(rs.getObject("id_lote") != null ? rs.getInt("id_lote") : null);
                    u.setIdCasa(rs.getObject("id_casa") != null ? rs.getInt("id_casa") : null);
                    u.setActivo(rs.getBoolean("activo"));
                    u.setDentro(rs.getInt("dentro"));
                    u.setDentro(rs.getInt("dentro"));

                    return u;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error buscando usuario por ID: " + e.getMessage());
        }
        return null;
    }
    
    public void actualizarEstado(int idUsuario, int dentro) {
    String sql = "UPDATE usuarios SET dentro=? WHERE id_usuario=?";
    try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, dentro);
        ps.setInt(2, idUsuario);
        ps.executeUpdate();
    } catch (SQLException e) {
        System.err.println("Error actualizando estado: " + e.getMessage());
        }
    }
    
    public void registrarAuditoria(int idUsuario, String accion) {
    String sql = "INSERT INTO auditoria (id_usuario, accion) VALUES (?, ?)";
    try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, idUsuario);
        ps.setString(2, accion); // "entrada" o "salida"
        ps.executeUpdate();
    } catch (SQLException e) {
        System.err.println("Error registrando auditoría: " + e.getMessage());
    }
}

    
    public List<Usuarios> buscarResidentes(String nombres,
                                       String apellidos,
                                       Integer idLote,
                                       Integer idCasa) {
    List<Usuarios> lista = new ArrayList<>();
    StringBuilder sql = new StringBuilder(
      "SELECT u.*, " +
      "  (SELECT nombre FROM catalogos WHERE id = u.id_lote) AS nombre_lote, " +
      "  (SELECT nombre FROM catalogos WHERE id = u.id_casa) AS numero_casa " +
      "FROM usuarios u " +
      "WHERE u.id_rol = ? AND u.activo = TRUE"
    );

    if (nombres != null && !nombres.isEmpty())
        sql.append(" AND LOWER(u.nombres) LIKE ? ");
    if (apellidos != null && !apellidos.isEmpty())
        sql.append(" AND LOWER(u.apellidos) LIKE ? ");
    if (idLote != null)
        sql.append(" AND u.id_lote = ? ");
    if (idCasa != null)
        sql.append(" AND u.id_casa = ? ");

    try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
        int idx = 1;
        ps.setInt(idx++, ID_ROL_RESIDENTE);
        if (nombres != null && !nombres.isEmpty())
            ps.setString(idx++, "%" + nombres.toLowerCase() + "%");
        if (apellidos != null && !apellidos.isEmpty())
            ps.setString(idx++, "%" + apellidos.toLowerCase() + "%");
        if (idLote != null)
            ps.setInt(idx++, idLote);
        if (idCasa != null)
            ps.setInt(idx++, idCasa);

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapRow(rs));
            }
        }
    } catch (SQLException e) {
        System.err.println("Error buscarResidentes: " + e.getMessage());
    }
    return lista;
}

    
/*
    public List<Usuarios> buscarResidentes(String nombres,
                                       String apellidos,
                                       Integer idLote,
                                       Integer idCasa) {
    List<Usuarios> lista = new ArrayList<>();
   StringBuilder sql = new StringBuilder(
  "SELECT u.*, l.nombre_lote " +
  "  FROM usuarios u " +
  "  LEFT JOIN lotes l ON u.id_lote = l.id_lote " +
  " WHERE u.id_rol = ? AND u.activo = TRUE"
);
if (nombres != null && !nombres.isEmpty())
  sql.append(" AND LOWER(u.nombres)   LIKE ? ");
if (apellidos != null && !apellidos.isEmpty())
  sql.append(" AND LOWER(u.apellidos) LIKE ? ");
if (idLote != null)
  sql.append(" AND u.id_lote = ? ");
if (idCasa != null)
  sql.append(" AND u.id_casa = ? ");


    try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
        int idx = 1;
        ps.setInt(idx++, ID_ROL_RESIDENTE);
        if (nombres != null && !nombres.isEmpty())
            ps.setString(idx++, "%" + nombres.toLowerCase() + "%");
        if (apellidos != null && !apellidos.isEmpty())
            ps.setString(idx++, "%" + apellidos.toLowerCase() + "%");
        if (idLote != null) ps.setInt(idx++, idLote);
        if (idCasa != null) ps.setInt(idx++, idCasa);

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapRow(rs));
            }
        }
    } catch (SQLException e) {
        System.err.println("Error buscarResidentes: " + e.getMessage());
    }
    return lista;
}
*/
private Usuarios mapRow(ResultSet rs) throws SQLException {
    Usuarios u = new Usuarios();
    u.setId(rs.getInt("id_usuario"));
    u.setDpi(rs.getString("dpi"));
    u.setNombres(rs.getString("nombres"));
    u.setApellidos(rs.getString("apellidos"));
    u.setCorreo(rs.getString("correo"));
    u.setContrasena(rs.getString("contrasena"));
    u.setIdRol(rs.getInt("id_rol"));
    // sigue leyendo id_lote y id_casa
    u.setIdLote(rs.getObject("id_lote") != null ? rs.getInt("id_lote") : null);
    u.setIdCasa(rs.getObject("id_casa") != null ? rs.getInt("id_casa") : null);
    // ahora solo traemos el nombre del lote
    u.setNombreLote(rs.getString("nombre_lote"));
     u.setNumeroCasa(rs.getString("numero_casa"));   // <-- agregar esta propiedad en POJO
    // si no tienes propiedad numeroCasa en tu POJO, quítalo
    u.setActivo(rs.getBoolean("activo"));
    u.setDentro(rs.getInt("dentro"));
    return u;
}


       public List<String> obtenerCorreosAdmins() throws Exception {
        List<String> correos = new ArrayList<>();
        String sql = "SELECT correo FROM usuarios WHERE id_rol = 1 AND activo = 1";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                correos.add(rs.getString("correo"));
            }
        }
        return correos;
    }



}
