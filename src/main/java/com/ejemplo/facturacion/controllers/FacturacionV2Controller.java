package com.ejemplo.facturacion.controllers;

import java.net.URI;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.ejemplo.facturacion.services.FacturaService;
import com.ejemplo.facturacion.valueobjects.Factura;
import com.ejemplo.facturacion.valueobjects.Orden;

@RestController
public class FacturacionV2Controller {
    @Autowired
    FacturaService facturaService;

    // POST: Inicia el proceso asíncrono
    @PostMapping("/v2/factura")
    public ResponseEntity<String> calcularFactura(@RequestBody Orden orden) throws InterruptedException {
        String idFactura = facturaService.iniciarFacturaAsincrona(orden);

        // creamos la URL donde consultaremos el resultado
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{idFactura}")
                .buildAndExpand(idFactura)
                .toUri();

        // Regresamos 202
        return ResponseEntity.accepted().location(location).build();
    }

    // GET: Consulta el estado
    @GetMapping("/v2/factura/{idFactura}")
    public ResponseEntity<Factura> buscarFactura(@PathVariable String idFactura) {
        Optional<Factura> resultado = facturaService.obtenerFacturaAsincrona(idFactura);

        if (resultado == null) {
            // 404 Not Found
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (resultado.isEmpty()) {
            // 204 No Content
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            // 200 OK + JSON
            return new ResponseEntity<>(resultado.get(), HttpStatus.OK);
        }
    }
}