package com.ejemplo.facturacion.services;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ejemplo.facturacion.valueobjects.Articulo;
import com.ejemplo.facturacion.valueobjects.Factura;
import com.ejemplo.facturacion.valueobjects.Orden;

@SpringBootTest
public class FacturaServiceTest {

    @Autowired
    private FacturaService facturaService;

    private Orden crearOrdenPrueba() {
        Orden orden = new Orden();
        List<Articulo> articulos = new ArrayList<>();
        articulos.add(new Articulo("PANTALON", 2, new BigDecimal("100.00")));
        orden.setArticulos(articulos);
        return orden;
    }

    // metodo normal
    @Test
    public void testGenerarFactura() throws InterruptedException {
        Orden orden = crearOrdenPrueba();
        Factura factura = facturaService.generarFactura(orden);
        assertNotNull(factura.getId());
        assertEquals(new BigDecimal("232.00"), factura.getTotal());
    }

    // async paso 1
    @Test
    public void testIniciarFacturaAsincrona() throws InterruptedException {
        Orden orden = crearOrdenPrueba();
        String idFactura = facturaService.iniciarFacturaAsincrona(orden);

        assertNotNull(idFactura);
        Optional<Factura> factura = facturaService.obtenerFacturaAsincrona(idFactura);
        assertNotNull(factura);
    }

    // async paso 2
    @Test
    public void testObtenerFacturaAsincronaNoExistente() {
        Optional<Factura> resultado = facturaService.obtenerFacturaAsincrona("ID_FALSO_123");
        assertNull(resultado, "Debería retornar null para IDs inexistentes");
    }

    // async paso 3
    @Test
    public void testCrearFacturaAsincronaCompleta() throws InterruptedException {
        Orden orden = crearOrdenPrueba();
        String idFactura = facturaService.iniciarFacturaAsincrona(orden);

        Thread.sleep(5500);

        Optional<Factura> resultadoFinal = facturaService.obtenerFacturaAsincrona(idFactura);
        assertTrue(resultadoFinal.isPresent(), "La factura debería estar lista después de la espera");
        assertEquals(new BigDecimal("232.00"), resultadoFinal.get().getTotal());
    }
}