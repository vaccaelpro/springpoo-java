package com.sena.springpoo.controller;

import com.sena.springpoo.exceptions.BadRequestException;
import com.sena.springpoo.exceptions.ResourceNotFoundException;
import com.sena.springpoo.modells.Ingreso;
import com.sena.springpoo.persistence.persistenceIngreso;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller("nuevocontroller")
@RequestMapping("/ingreso")
@CrossOrigin(origins = "*")
@ResponseBody
public class NuevoController {

    private final persistenceIngreso persistence = new persistenceIngreso();

    @PostMapping("/registrar")
    public ResponseEntity<String> registrar(
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "Accept-Language", defaultValue = "es") String lang) {

        String montoStr = body.get("monto");
        String fechaStr = body.get("fecha");

        if (montoStr == null || fechaStr == null ||
                montoStr.isEmpty() || fechaStr.isEmpty()) {
            throw new BadRequestException(lang.startsWith("en")
                    ? "Missing required fields."
                    : "Faltan campos obligatorios.");
        }

        double monto;
        try {
            monto = Double.parseDouble(montoStr);
        } catch (NumberFormatException e) {
            throw new BadRequestException(lang.startsWith("en")
                    ? "Invalid amount format."
                    : "Formato de monto inválido.");
        }

        if (monto <= 0) {
            throw new BadRequestException(lang.startsWith("en")
                    ? "Amount must be greater than 0."
                    : "El monto debe ser mayor a 0.");
        }

        LocalDate fecha;
        try {
            fecha = LocalDate.parse(fechaStr);
        } catch (Exception e) {
            throw new BadRequestException(lang.startsWith("en")
                    ? "Invalid date format: " + e.getMessage()
                    : "Formato de fecha inválido: " + e.getMessage());
        }

        Ingreso ingreso = new Ingreso();
        ingreso.setMonto(monto);
        ingreso.setFecha(fecha);

        boolean guardado = persistence.save(ingreso);

        if (guardado) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(lang.startsWith("en")
                            ? "Income registered with ID: " + ingreso.getIdIngreso()
                            : "Ingreso registrado con ID: " + ingreso.getIdIngreso());
        }
        throw new BadRequestException(lang.startsWith("en")
                ? "Could not save the income."
                : "No se pudo guardar el ingreso.");
    }

    @GetMapping("/listar")
    public ResponseEntity<Object> listar(
            @RequestParam(value = "montoMinimo", required = false, defaultValue = "0") double montoMinimo,
            @RequestHeader(value = "Accept-Language", defaultValue = "es") String lang) {

        List<Ingreso> lista = persistence.findAll();

        if (montoMinimo > 0) {
            lista = lista.stream()
                    .filter(i -> i.getMonto() >= montoMinimo)
                    .toList();
        }

        if (lista.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(lista);
    }

    @GetMapping("/buscar/{id}")
    public ResponseEntity<Object> buscar(
            @PathVariable int id,
            @RequestHeader(value = "Accept-Language", defaultValue = "es") String lang) {

        Ingreso ingreso = persistence.findById(id);

        if (ingreso == null) {
            throw new ResourceNotFoundException(lang.startsWith("en")
                    ? "Income with ID " + id + " not found."
                    : "No se encontró el ingreso con ID: " + id);
        }

        return ResponseEntity.status(HttpStatus.OK).body(ingreso);
    }

    @PutMapping("/actualizar")
    public ResponseEntity<String> actualizar(
            @ModelAttribute Ingreso ingreso,
            @RequestHeader(value = "Accept-Language", defaultValue = "es") String lang) {

        if (ingreso.getIdIngreso() <= 0) {
            throw new BadRequestException(lang.startsWith("en")
                    ? "A valid ID is required."
                    : "Se requiere un ID válido.");
        }

        if (ingreso.getMonto() <= 0 || ingreso.getFecha() == null) {
            throw new BadRequestException(lang.startsWith("en")
                    ? "Missing required fields."
                    : "Faltan campos obligatorios.");
        }

        boolean actualizado = persistence.update(ingreso);

        if (actualizado) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(lang.startsWith("en")
                            ? "Income updated successfully."
                            : "Ingreso actualizado correctamente.");
        }
        throw new ResourceNotFoundException(lang.startsWith("en")
                ? "Income with ID " + ingreso.getIdIngreso() + " not found."
                : "No se encontró el ingreso con ID: " + ingreso.getIdIngreso());
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<String> eliminar(
            @PathVariable int id,
            @RequestHeader(value = "Accept-Language", defaultValue = "es") String lang) {

        boolean eliminado = persistence.delete(id);

        if (eliminado) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(lang.startsWith("en")
                            ? "Income with ID " + id + " deleted successfully."
                            : "Ingreso con ID " + id + " eliminado correctamente.");
        }
        throw new ResourceNotFoundException(lang.startsWith("en")
                ? "Income with ID " + id + " not found."
                : "No se encontró el ingreso con ID: " + id);
    }
}