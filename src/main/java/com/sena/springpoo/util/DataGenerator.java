package com.sena.springpoo.util;

import com.sena.springpoo.modells.Aprendiz;
import com.sena.springpoo.persistence.persistenceAprendiz;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Random;

public class DataGenerator {
    private static final Logger logger = LoggerFactory.getLogger(DataGenerator.class);
    private final persistenceAprendiz persistence = new persistenceAprendiz();
    private final Random random = new Random();

    public void generateData(int count) {
        logger.info("Iniciando generación de {} registros...", count);
        String[] nombres = {"Juan", "Maria", "Carlos", "Ana", "Luis", "Elena", "Pedro", "Sofia", "Diego", "Laura"};
        String[] apellidos = {"Gomez", "Rodriguez", "Lopez", "Martinez", "Perez", "Sanchez", "Ramirez", "Torres", "Flores", "Rivera"};
        String[] tiposDoc = {"CC", "TI", "CE"};
        String[] roles = {"APRENDIZ", "INSTRUCTOR", "ADMIN"};
        String[] tiposApoyo = {"regular", "transporte", "alimentacion"};

        for (int i = 0; i < count; i++) {
            Aprendiz a = new Aprendiz();
            String nombre = nombres[random.nextInt(nombres.length)];
            String apellido = apellidos[random.nextInt(apellidos.length)];
            
            a.setPrimerNombre(nombre);
            a.setSegundoNombre("Gen" + i);
            a.setPrimerApellido(apellido);
            a.setSegundoApellido("Batch");
            a.setTipoDocumento(tiposDoc[random.nextInt(tiposDoc.length)]);
            a.setDocumento(String.valueOf(1000000000L + i));
            a.setCelular("300" + (1000000 + i));
            a.setGrupoFormacion("ADSO-2026-" + (i % 10));
            a.setCorreo(nombre.toLowerCase() + i + "@sena.edu.co");
            a.setContrasena("Sena1234*"); // This will be hashed by persistenceAprendiz
            a.setRol(roles[random.nextInt(roles.length)]);
            a.setTipoApoyo(tiposApoyo[random.nextInt(tiposApoyo.length)]);
            a.setFechaRegistro(LocalDate.now());
            
            persistence.save(a);
            
            if (i % 500 == 0) {
                logger.info("Generados {} registros...", i);
            }
        }
        logger.info("Generación de datos completada.");
    }
}
