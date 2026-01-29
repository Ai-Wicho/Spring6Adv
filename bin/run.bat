@echo off
echo Iniciando servicio de Facturacion...
cd ..
rem Asume que ya hiciste 'mvn package' y el jar esta en target
start "FacturacionAPI" java -jar target/facturacion-0.0.1-SNAPSHOT.jar > bin/service.log 2>&1
echo Servicio iniciado. Revisa bin/service.log para detalles.