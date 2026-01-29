package com.ejemplo.facturacion.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.ejemplo.facturacion.valueobjects.Articulo;
import com.ejemplo.facturacion.valueobjects.Factura;
import com.ejemplo.facturacion.valueobjects.Orden;

@Service
public class FacturaService {
    // Simulamos una base de datos en memoria
    private Map<String, Optional<Factura>> facturas = new HashMap<>();

    @Autowired
    @Lazy
    private FacturaService autoReferencia;

    // Metodo legacy
    public Factura generarFactura(final Orden orden) throws InterruptedException {
        Factura factura = new Factura();
        String idFactura = generarIdFactura();
        facturas.put(idFactura, Optional.empty());

        Thread.sleep(5000); // proceso lento simulado

        BigDecimal subtotal = calcularSubtotal(orden.getArticulos());
        BigDecimal iva = subtotal.multiply(BigDecimal.valueOf(0.16)).setScale(2, RoundingMode.UP);
        BigDecimal total = subtotal.add(iva);

        factura.setId(idFactura);
        factura.setArticulos(orden.getArticulos());
        factura.setSubtotal(subtotal);
        factura.setIva(iva);
        factura.setTotal(total);

        facturas.put(factura.getId(), Optional.of(factura));
        return factura;
    }

    // paso 1 del background task
    public String iniciarFacturaAsincrona(final Orden orden) throws InterruptedException {
        String idFactura = generarIdFactura();

        facturas.put(idFactura, Optional.empty());

        autoReferencia.crearFacturaAsincrona(idFactura, orden);

        return idFactura;
    }

    // paso 2 del background task
    public Optional<Factura> obtenerFacturaAsincrona(final String idFactura) {
        if (facturas.containsKey(idFactura)) {
            return facturas.get(idFactura);
        }
        return null; // Retornamos null si el ID no existe
    }

    // paso 3 del background task
    @Async // async hace el proceso
    public void crearFacturaAsincrona(final String idFactura, final Orden orden) throws InterruptedException {
        Thread.sleep(5000);

        Factura factura = new Factura();
        BigDecimal subtotal = calcularSubtotal(orden.getArticulos());
        BigDecimal iva = subtotal.multiply(BigDecimal.valueOf(0.16)).setScale(2, RoundingMode.UP);
        BigDecimal total = subtotal.add(iva);

        factura.setId(idFactura);
        factura.setArticulos(orden.getArticulos());
        factura.setSubtotal(subtotal);
        factura.setIva(iva);
        factura.setTotal(total);

        // Factura lista
        facturas.put(idFactura, Optional.of(factura));
    }

    private BigDecimal calcularSubtotal(List<Articulo> articulos) {
        BigDecimal subtotal = BigDecimal.ZERO;
        if (articulos != null) {
            for (final Articulo articulo : articulos) {
                BigDecimal precioUnitario = articulo.getPrecioUnitario();
                BigDecimal cantidad = BigDecimal.valueOf(articulo.getCantidad());
                BigDecimal totalArticulo = precioUnitario.multiply(cantidad);
                subtotal = subtotal.add(totalArticulo);
            }
        }
        return subtotal;
    }

    private String generarIdFactura() {
        return String.valueOf(Instant.now().toEpochMilli());
    }

    // tests
    public Map<String, Optional<Factura>> getFacturas() {
        return facturas;
    }

    public void setFacturas(Map<String, Optional<Factura>> facturas) {
        this.facturas = facturas;
    }
}