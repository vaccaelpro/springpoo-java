package com.sena.springpoo.controller;

import com.sena.springpoo.modells.Aprendiz;
import com.sena.springpoo.persistence.persistenceAprendiz;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/importar")
@CrossOrigin(origins = "${cors.allowed-origin:http://localhost:8080}")
public class ExcelAprendizController {

    private static final Logger logger = LoggerFactory.getLogger(ExcelAprendizController.class);

    private final persistenceAprendiz persistence = new persistenceAprendiz();

    @Value("${n8n.webhook.url}")
    private String n8nWebhookUrl;

    @PostMapping("/excel")
    public ResponseEntity<Map<String, Object>> importarExcel(
            @RequestParam("file") MultipartFile file) {

        logger.info("POST /importar/excel - archivo recibido: {}, tamaño: {} bytes",
                file.getOriginalFilename(), file.getSize());

        if (file.isEmpty()) {
            logger.warn("El archivo recibido está vacío.");
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "El archivo está vacío.", "insertados", 0));
        }

        List<Map<String, Object>> aprendicesInsertados = new ArrayList<>();
        List<String> errores = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(0);
            int totalFilas = sheet.getLastRowNum(); // sin contar la cabecera
            logger.info("Total de filas de datos en el Excel: {}", totalFilas);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Saltar cabecera

                // Saltar filas completamente vacías
                String primerNombre = getCellValue(row.getCell(0));
                if (primerNombre.isBlank()) continue;

                try {
                    Aprendiz aprendiz = new Aprendiz();
                    aprendiz.setPrimerNombre(primerNombre);
                    aprendiz.setSegundoNombre(getCellValue(row.getCell(1)));
                    aprendiz.setPrimerApellido(getCellValue(row.getCell(2)));
                    aprendiz.setSegundoApellido(getCellValue(row.getCell(3)));
                    aprendiz.setTipoDocumento(getCellValue(row.getCell(4)));
                    aprendiz.setDocumento(getCellValue(row.getCell(5)));
                    aprendiz.setCelular(getCellValue(row.getCell(6)));
                    aprendiz.setGrupoFormacion(getCellValue(row.getCell(7)));
                    aprendiz.setCorreo(getCellValue(row.getCell(8)));

                    String pass = getCellValue(row.getCell(9));
                    aprendiz.setContrasena(pass.isBlank() ? "Sena2024*" : pass);

                    aprendiz.setRol(getCellValue(row.getCell(10)));
                    aprendiz.setTipoApoyo(getCellValue(row.getCell(11)));
                    aprendiz.setFechaRegistro(LocalDate.now());

                    boolean guardado = persistence.save(aprendiz);

                    if (guardado) {
                        logger.info("Aprendiz guardado - ID: {}, correo: {}",
                                aprendiz.getId(), aprendiz.getCorreo());

                        Map<String, Object> resumen = new LinkedHashMap<>();
                        resumen.put("id", aprendiz.getId());
                        resumen.put("nombre", aprendiz.getPrimerNombre() + " " + aprendiz.getPrimerApellido());
                        resumen.put("correo", aprendiz.getCorreo());
                        resumen.put("documento", aprendiz.getDocumento());
                        aprendicesInsertados.add(resumen);
                    } else {
                        errores.add("Fila " + (row.getRowNum() + 1) + ": no se pudo guardar.");
                    }

                } catch (Exception e) {
                    String msg = "Fila " + (row.getRowNum() + 1) + ": " + e.getMessage();
                    logger.error(msg, e);
                    errores.add(msg);
                }
            }

        } catch (Exception e) {
            logger.error("Error al procesar el archivo Excel: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "mensaje", "Error al procesar el Excel: " + e.getMessage(),
                            "insertados", 0
                    ));
        }

        notificarN8n(aprendicesInsertados, errores);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Proceso completado.");
        respuesta.put("insertados", aprendicesInsertados.size());
        respuesta.put("errores", errores.size());
        respuesta.put("detalleErrores", errores);
        respuesta.put("aprendices", aprendicesInsertados);

        return ResponseEntity.ok(respuesta);
    }

    private void notificarN8n(List<Map<String, Object>> insertados, List<String> errores) {
        if (n8nWebhookUrl == null || n8nWebhookUrl.isBlank()) {
            logger.warn("n8n.webhook.url no está configurada. No se notificará a n8n.");
            return;
        }
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("origen", "spring-boot-importacion-excel");
            payload.put("fecha", LocalDate.now().toString());
            payload.put("totalInsertados", insertados.size());
            payload.put("totalErrores", errores.size());
            payload.put("aprendices", insertados);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(n8nWebhookUrl, request, String.class);

            logger.info("Resumen enviado a n8n exitosamente. Total insertados: {}", insertados.size());
        } catch (Exception e) {
            logger.error("Error al notificar a n8n: {}", e.getMessage(), e);
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> String.format("%.0f", cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try { yield String.format("%.0f", cell.getNumericCellValue()); }
                catch (Exception e) { yield cell.getStringCellValue().trim(); }
            }
            default -> "";
        };
    }
}
