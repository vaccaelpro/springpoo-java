package com.sena.springpoo.persistence;

import com.sena.springpoo.exceptions.DatabaseException;
import com.sena.springpoo.modells.Ingreso;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class persistenceIngreso {

    private static final Logger logger = LoggerFactory.getLogger(persistenceIngreso.class);

    public boolean save(Ingreso ingreso) {
        logger.debug("Intentando guardar ingreso: monto={}", ingreso.getMonto());
        String sql = "INSERT INTO ingreso (monto, fecha) VALUES (?, ?)";
        Connection conn = conexion.getInstancia();

        if (conn == null) {
            throw new DatabaseException("No se pudo conectar con la base de datos (MySQL apagado).");
        }

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDouble(1, ingreso.getMonto());
            ps.setDate(2, Date.valueOf(ingreso.getFecha()));
            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) ingreso.setIdIngreso(keys.getInt(1));
                }
                logger.info("Ingreso guardado correctamente con ID: {}", ingreso.getIdIngreso());
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error al guardar ingreso: {}", e.getMessage(), e);
            throw new DatabaseException("Error de base de datos al guardar ingreso: " + e.getMessage(), e);
        }
        return false;
    }

    public List<Ingreso> findAll() {
        logger.debug("Consultando todos los ingresos.");
        List<Ingreso> lista = new ArrayList<>();
        String sql = "SELECT * FROM ingreso";
        Connection conn = conexion.getInstancia();

        if (conn == null) {
            throw new DatabaseException("No se pudo conectar con la base de datos (MySQL apagado).");
        }

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Ingreso i = new Ingreso();
                i.setIdIngreso(rs.getInt("id_ingreso"));
                i.setMonto(rs.getDouble("monto"));
                i.setFecha(rs.getDate("fecha").toLocalDate());
                lista.add(i);
            }
            logger.info("findAll() de ingresos devolvió {} registros.", lista.size());
        } catch (SQLException e) {
            logger.error("Error al consultar ingresos: {}", e.getMessage(), e);
            throw new DatabaseException("Error de base de datos al consultar ingresos: " + e.getMessage(), e);
        }
        return lista;
    }

    public Ingreso findById(int id) {
        logger.debug("Buscando ingreso con ID: {}", id);
        String sql = "SELECT * FROM ingreso WHERE id_ingreso = ?";
        Connection conn = conexion.getInstancia();

        if (conn == null) {
            throw new DatabaseException("No se pudo conectar con la base de datos (MySQL apagado).");
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Ingreso i = new Ingreso();
                    i.setIdIngreso(rs.getInt("id_ingreso"));
                    i.setMonto(rs.getDouble("monto"));
                    i.setFecha(rs.getDate("fecha").toLocalDate());
                    logger.debug("Ingreso encontrado.");
                    return i;
                }
            }
        } catch (SQLException e) {
            logger.error("Error al buscar ingreso ID {}: {}", id, e.getMessage(), e);
            throw new DatabaseException("Error de base de datos al buscar ingreso.", e);
        }
        return null;
    }

    public boolean update(Ingreso ingreso) {
        logger.debug("Actualizando ingreso ID: {}", ingreso.getIdIngreso());
        String sql = "UPDATE ingreso SET monto = ?, fecha = ? WHERE id_ingreso = ?";
        Connection conn = conexion.getInstancia();

        if (conn == null) {
            throw new DatabaseException("No se pudo conectar con la base de datos (MySQL apagado).");
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, ingreso.getMonto());
            ps.setDate(2, Date.valueOf(ingreso.getFecha()));
            ps.setInt(3, ingreso.getIdIngreso());
            boolean ok = ps.executeUpdate() > 0;
            if (ok) logger.info("Ingreso ID {} actualizado correctamente.", ingreso.getIdIngreso());
            return ok;
        } catch (SQLException e) {
            logger.error("Error al actualizar ingreso ID {}: {}", ingreso.getIdIngreso(), e.getMessage(), e);
            throw new DatabaseException("Error de base de datos al actualizar ingreso.", e);
        }
    }

    public boolean delete(int id) {
        logger.debug("Eliminando ingreso ID: {}", id);
        String sql = "DELETE FROM ingreso WHERE id_ingreso = ?";
        Connection conn = conexion.getInstancia();

        if (conn == null) {
            throw new DatabaseException("No se pudo conectar con la base de datos (MySQL apagado).");
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) logger.info("Ingreso ID {} eliminado.", id);
            return ok;
        } catch (SQLException e) {
            logger.error("Error al eliminar ingreso ID {}: {}", id, e.getMessage(), e);
            throw new DatabaseException("Error de base de datos al eliminar ingreso.", e);
        }
    }
}