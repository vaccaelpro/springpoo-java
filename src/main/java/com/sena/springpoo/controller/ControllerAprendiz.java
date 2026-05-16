package com.sena.springpoo.controller;

import com.sena.springpoo.exceptions.BadRequestException;
import com.sena.springpoo.exceptions.ResourceNotFoundException;
import com.sena.springpoo.modells.Aprendiz;
import com.sena.springpoo.persistence.persistenceAprendiz;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@RestController
@RequestMapping("/aprendiz")
public class ControllerAprendiz {

    private static final Logger logger = LoggerFactory.getLogger(ControllerAprendiz.class);

    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    @Value("${cors.allowed-origin:http://localhost:3000}")
    private String allowedOrigin;

    private final persistenceAprendiz persistence = new persistenceAprendiz();

    @PostMapping("/registrar")
    @CrossOrigin(origins = "${cors.allowed-origin:http://localhost:3000}")
    public ResponseEntity<String> registrar(
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            @RequestParam("aprendiz") String aprendizJson) throws Exception {

        logger.info("POST /aprendiz/registrar - inicio");

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        Aprendiz aprendiz = mapper.readValue(aprendizJson, Aprendiz.class);

        if (aprendiz.getPrimerNombre() == null || aprendiz.getPrimerNombre().isEmpty() ||
                aprendiz.getPrimerApellido() == null || aprendiz.getPrimerApellido().isEmpty() ||
                aprendiz.getDocumento() == null || aprendiz.getDocumento().isEmpty() ||
                aprendiz.getCorreo() == null || aprendiz.getCorreo().isEmpty()) {
            logger.warn("POST /aprendiz/registrar - campos obligatorios faltantes");
            throw new BadRequestException("Faltan campos obligatorios.");
        }

        if (foto != null && !foto.isEmpty()) {
            String safeFileName = guardarFoto(foto);
            aprendiz.setFoto(safeFileName);
        }

        aprendiz.setFechaRegistro(LocalDate.now());
        boolean guardado = persistence.save(aprendiz);
        if (guardado) {
            logger.info("POST /aprendiz/registrar - aprendiz creado con ID: {}", aprendiz.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Aprendiz registrado con ID: " + aprendiz.getId());
        }
        throw new BadRequestException("No se pudo registrar el aprendiz.");
    }

    @GetMapping("/listar")
    @CrossOrigin(origins = "${cors.allowed-origin:http://localhost:3000}")
    public ResponseEntity<Object> listar(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        
        logger.info("GET /aprendiz/listar - page: {}, size: {}, search: {}", page, size, search);
        
        int offset = (page - 1) * size;
        List<Aprendiz> lista = persistence.findPaginated(offset, size, search);
        int totalElements = persistence.countAll(search);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        PaginatedResponse response = new PaginatedResponse(
                lista,
                totalElements,
                totalPages,
                page,
                size
        );

        return ResponseEntity.ok(response);
    }

    // Helper class for paginated response
    public static class PaginatedResponse {
        private List<Aprendiz> content;
        private int totalElements;
        private int totalPages;
        private int currentPage;
        private int pageSize;

        public PaginatedResponse(List<Aprendiz> content, int totalElements, int totalPages, int currentPage, int pageSize) {
            this.content = content;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.currentPage = currentPage;
            this.pageSize = pageSize;
        }

        // Getters
        public List<Aprendiz> getContent() { return content; }
        public int getTotalElements() { return totalElements; }
        public int getTotalPages() { return totalPages; }
        public int getCurrentPage() { return currentPage; }
        public int getPageSize() { return pageSize; }
    }

    @DeleteMapping("/eliminar/{id}")
    @CrossOrigin(origins = "${cors.allowed-origin:http://localhost:3000}")
    public ResponseEntity<String> eliminar(@PathVariable int id) {
        logger.info("DELETE /aprendiz/eliminar/{}", id);
        boolean eliminado = persistence.delete(id);
        if (eliminado) {
            return ResponseEntity.ok("Aprendiz eliminado correctamente.");
        }
        throw new ResourceNotFoundException("No se encontró el aprendiz con ID: " + id);
    }

    @GetMapping("/buscar/{id}")
    @CrossOrigin(origins = "${cors.allowed-origin:http://localhost:3000}")
    public ResponseEntity<Object> buscar(@PathVariable int id) {
        logger.info("GET /aprendiz/buscar/{}", id);
        Aprendiz aprendiz = persistence.findById(id);
        if (aprendiz == null) {
            throw new ResourceNotFoundException("No se encontró el aprendiz con ID: " + id);
        }
        return ResponseEntity.ok(aprendiz);
    }

    @PutMapping("/actualizar")
    @CrossOrigin(origins = "${cors.allowed-origin:http://localhost:3000}")
    public ResponseEntity<String> actualizar(
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            @RequestParam("aprendiz") String aprendizJson) throws Exception {

        logger.info("PUT /aprendiz/actualizar - inicio");

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        Aprendiz aprendiz = mapper.readValue(aprendizJson, Aprendiz.class);

        if (aprendiz.getId() <= 0) {
            throw new BadRequestException("Se requiere un ID válido.");
        }

        if (aprendiz.getPrimerNombre() == null || aprendiz.getPrimerNombre().isEmpty() ||
                aprendiz.getPrimerApellido() == null || aprendiz.getPrimerApellido().isEmpty() ||
                aprendiz.getDocumento() == null || aprendiz.getDocumento().isEmpty() ||
                aprendiz.getCorreo() == null || aprendiz.getCorreo().isEmpty()) {
            logger.warn("PUT /aprendiz/actualizar - campos obligatorios faltantes");
            throw new BadRequestException("Faltan campos obligatorios.");
        }

        if (foto != null && !foto.isEmpty()) {
            String safeFileName = guardarFoto(foto);
            aprendiz.setFoto(safeFileName);
        } else {
            Aprendiz existente = persistence.findById(aprendiz.getId());
            if (existente != null) {
                aprendiz.setFoto(existente.getFoto());
            }
        }

        boolean actualizado = persistence.update(aprendiz);
        if (actualizado) {
            logger.info("PUT /aprendiz/actualizar - aprendiz ID {} actualizado.", aprendiz.getId());
            return ResponseEntity.ok("Aprendiz actualizado correctamente.");
        }
        throw new ResourceNotFoundException("No se encontró el aprendiz con ID: " + aprendiz.getId());
    }


    @PostMapping("/generar-datos")
    @CrossOrigin(origins = "${cors.allowed-origin:http://localhost:3000}")
    public ResponseEntity<String> generarDatos(@RequestParam(defaultValue = "10000") int cantidad) {
        logger.info("POST /aprendiz/generar-datos - cantidad: {}", cantidad);
        new Thread(() -> {
            new com.sena.springpoo.util.DataGenerator().generateData(cantidad);
        }).start();
        return ResponseEntity.ok("Proceso de generación de " + cantidad + " registros iniciado en segundo plano.");
    }

    private String guardarFoto(MultipartFile foto) throws Exception {
        String originalName = foto.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            throw new BadRequestException("El archivo de foto no tiene nombre.");
        }

        String extension = obtenerExtension(originalName).toLowerCase();
        if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
            logger.warn("Intento de subida de archivo con extensión no permitida: {}", extension);
            throw new BadRequestException(
                    "Tipo de archivo no permitido. Solo se aceptan: " + EXTENSIONES_PERMITIDAS);
        }

        String safeFileName = UUID.randomUUID() + "." + extension;
        Path path = Paths.get("uploads", safeFileName);
        Files.createDirectories(path.getParent());
        Files.copy(foto.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

        logger.debug("Foto guardada como: {}", safeFileName);
        return safeFileName;
    }

    private String obtenerExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex >= 0 && dotIndex < fileName.length() - 1)
                ? fileName.substring(dotIndex + 1)
                : "";
    }
}