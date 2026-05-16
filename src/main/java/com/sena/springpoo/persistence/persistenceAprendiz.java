package com.sena.springpoo.persistence;

import com.sena.springpoo.exceptions.DatabaseException;
import com.sena.springpoo.modells.Aprendiz;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class persistenceAprendiz {

    private static final Logger logger = LoggerFactory.getLogger(persistenceAprendiz.class);

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public persistenceAprendiz() {
        String alterSql = "ALTER TABLE aprendiz ADD COLUMN foto VARCHAR(255);";
        try {
            Connection conn = conexion.getInstancia();
            if (conn != null) {
                try (Statement st = conn.createStatement()) {
                    st.execute(alterSql);
                } catch (SQLException e) {
                    logger.debug("Columna 'foto' ya existe o no se pudo agregar: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.warn("Advertencia en constructor persistenceAprendiz: {}", e.getMessage());
        }
    }

    public boolean save(Aprendiz aprendiz) {
        logger.debug("Intentando guardar aprendiz: documento={}", aprendiz.getDocumento());

        String sql = "INSERT INTO aprendiz (" +
                "primer_nombre, segundo_nombre, primer_apellido, segundo_apellido, " +
                "tipo_documento, documento, celular, grupo_formacion, " +
                "correo, contrasena, rol, tipo_apoyo, fecha_registro, foto" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = conexion.getInstancia();

        if (conn == null) {
            throw new DatabaseException("No se pudo conectar con la base de datos. Verifique que el servidor MySQL esté activo.");
        }

        String hashedPassword = passwordEncoder.encode(aprendiz.getContrasena());

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1,  aprendiz.getPrimerNombre());
            ps.setString(2,  aprendiz.getSegundoNombre());
            ps.setString(3,  aprendiz.getPrimerApellido());
            ps.setString(4,  aprendiz.getSegundoApellido());
            ps.setString(5,  aprendiz.getTipoDocumento());
            ps.setString(6,  aprendiz.getDocumento());
            ps.setString(7,  aprendiz.getCelular());
            ps.setString(8,  aprendiz.getGrupoFormacion());
            ps.setString(9,  aprendiz.getCorreo());
            ps.setString(10, hashedPassword);
            ps.setString(11, aprendiz.getRol());
            ps.setString(12, aprendiz.getTipoApoyo());
            ps.setDate(13,   Date.valueOf(aprendiz.getFechaRegistro()));
            ps.setString(14, aprendiz.getFoto());

            int filas = ps.executeUpdate();

            if (filas > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        aprendiz.setId(keys.getInt(1));
                    }
                }
                logger.info("Aprendiz guardado correctamente con ID: {}", aprendiz.getId());
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error al guardar aprendiz en la BD: {}", e.getMessage(), e);
            throw new DatabaseException("Error en la base de datos al guardar: " + e.getMessage(), e);
        }

        return false;
    }

    public List<Aprendiz> findAll() {
        logger.debug("Consultando todos los aprendices.");
        List<Aprendiz> lista = new ArrayList<>();
        String sql = "SELECT * FROM aprendiz";

        Connection conn = conexion.getInstancia();
        if (conn == null) {
            throw new DatabaseException("No se pudo conectar con la base de datos. Verifique que el servidor MySQL esté activo.");
        }

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapRow(rs));
            }
            logger.info("findAll() devolvió {} registro(s).", lista.size());

        } catch (SQLException e) {
            logger.error("Error al consultar aprendices: {}", e.getMessage(), e);
            throw new DatabaseException("Error en la base de datos al consultar: " + e.getMessage(), e);
        }

        return lista;
    }

    public int countAll(String query) {
        String sql = "SELECT COUNT(*) FROM aprendiz";
        if (query != null && !query.isEmpty()) {
            sql += " WHERE primer_nombre LIKE ? OR primer_apellido LIKE ? OR documento LIKE ? OR correo LIKE ?";
        }

        Connection conn = conexion.getInstancia();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (query != null && !query.isEmpty()) {
                String q = "%" + query + "%";
                ps.setString(1, q);
                ps.setString(2, q);
                ps.setString(3, q);
                ps.setString(4, q);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al contar aprendices: {}", e.getMessage(), e);
        }
        return 0;
    }

    public List<Aprendiz> findPaginated(int offset, int limit, String query) {
        List<Aprendiz> lista = new ArrayList<>();
        String sql = "SELECT * FROM aprendiz";
        if (query != null && !query.isEmpty()) {
            sql += " WHERE primer_nombre LIKE ? OR primer_apellido LIKE ? OR documento LIKE ? OR correo LIKE ?";
        }
        sql += " ORDER BY id DESC LIMIT ? OFFSET ?";

        Connection conn = conexion.getInstancia();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            if (query != null && !query.isEmpty()) {
                String q = "%" + query + "%";
                ps.setString(idx++, q);
                ps.setString(idx++, q);
                ps.setString(idx++, q);
                ps.setString(idx++, q);
            }
            ps.setInt(idx++, limit);
            ps.setInt(idx++, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error al consultar aprendices paginados: {}", e.getMessage(), e);
        }
        return lista;
    }

    public boolean delete(int id) {
        logger.debug("Intentando eliminar aprendiz con ID: {}", id);
        String sql = "DELETE FROM aprendiz WHERE id = ?";
        Connection conn = conexion.getInstancia();

        if (conn == null) {
            throw new DatabaseException("No se pudo conectar con la base de datos. Verifique que el servidor MySQL esté activo.");
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            if (filas > 0) {
                logger.info("Aprendiz con ID {} eliminado correctamente.", id);
            }
            return filas > 0;
        } catch (SQLException e) {
            logger.error("Error al eliminar aprendiz con ID {}: {}", id, e.getMessage(), e);
            throw new DatabaseException("Error en la base de datos al eliminar: " + e.getMessage(), e);
        }
    }

    public Aprendiz findById(int id) {
        logger.debug("Buscando aprendiz con ID: {}", id);
        String sql = "SELECT * FROM aprendiz WHERE id = ?";
        Connection conn = conexion.getInstancia();
        if (conn == null) {
            throw new DatabaseException("No se pudo conectar con la base de datos. Verifique que el servidor MySQL esté activo.");
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    logger.debug("Aprendiz con ID {} encontrado.", id);
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al buscar aprendiz con ID {}: {}", id, e.getMessage(), e);
            throw new DatabaseException("Error en la base de datos al buscar: " + e.getMessage(), e);
        }
        logger.debug("Aprendiz con ID {} no encontrado.", id);
        return null;
    }

    public boolean update(Aprendiz aprendiz) {
        logger.debug("Intentando actualizar aprendiz con ID: {}", aprendiz.getId());

        String contrasena = aprendiz.getContrasena();
        if (contrasena != null && !contrasena.startsWith("$2a$")) {
            contrasena = passwordEncoder.encode(contrasena);
        }

        String sql = "UPDATE aprendiz SET " +
                "primer_nombre = ?, segundo_nombre = ?, primer_apellido = ?, segundo_apellido = ?, " +
                "tipo_documento = ?, documento = ?, celular = ?, grupo_formacion = ?, " +
                "correo = ?, contrasena = ?, rol = ?, tipo_apoyo = ?, foto = ? " +
                "WHERE id = ?";

        Connection conn = conexion.getInstancia();
        if (conn == null) {
            throw new DatabaseException("No se pudo conectar con la base de datos. Verifique que el servidor MySQL esté activo.");
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1,  aprendiz.getPrimerNombre());
            ps.setString(2,  aprendiz.getSegundoNombre());
            ps.setString(3,  aprendiz.getPrimerApellido());
            ps.setString(4,  aprendiz.getSegundoApellido());
            ps.setString(5,  aprendiz.getTipoDocumento());
            ps.setString(6,  aprendiz.getDocumento());
            ps.setString(7,  aprendiz.getCelular());
            ps.setString(8,  aprendiz.getGrupoFormacion());
            ps.setString(9,  aprendiz.getCorreo());
            ps.setString(10, contrasena);
            ps.setString(11, aprendiz.getRol());
            ps.setString(12, aprendiz.getTipoApoyo());
            ps.setString(13, aprendiz.getFoto());
            ps.setInt(14,    aprendiz.getId());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                logger.info("Aprendiz con ID {} actualizado correctamente.", aprendiz.getId());
            }
            return filas > 0;
        } catch (SQLException e) {
            logger.error("Error al actualizar aprendiz con ID {}: {}", aprendiz.getId(), e.getMessage(), e);
            throw new DatabaseException("Error en la base de datos al actualizar: " + e.getMessage(), e);
        }
    }

    private Aprendiz mapRow(ResultSet rs) throws SQLException {
        Aprendiz a = new Aprendiz();
        a.setId(rs.getInt("id"));
        a.setPrimerNombre(rs.getString("primer_nombre"));
        a.setSegundoNombre(rs.getString("segundo_nombre"));
        a.setPrimerApellido(rs.getString("primer_apellido"));
        a.setSegundoApellido(rs.getString("segundo_apellido"));
        a.setTipoDocumento(rs.getString("tipo_documento"));
        a.setDocumento(rs.getString("documento"));
        a.setCelular(rs.getString("celular"));
        a.setGrupoFormacion(rs.getString("grupo_formacion"));
        a.setCorreo(rs.getString("correo"));
        a.setContrasena(rs.getString("contrasena"));
        a.setRol(rs.getString("rol"));
        a.setTipoApoyo(rs.getString("tipo_apoyo"));
        a.setFechaRegistro(rs.getDate("fecha_registro").toLocalDate());
        a.setFoto(rs.getString("foto"));
        return a;
    }
}