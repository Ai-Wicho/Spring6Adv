@echo off
echo Deteniendo servicio de Facturacion...
taskkill /FI "WINDOWTITLE eq FacturacionAPI"
echo Servicio detenido.