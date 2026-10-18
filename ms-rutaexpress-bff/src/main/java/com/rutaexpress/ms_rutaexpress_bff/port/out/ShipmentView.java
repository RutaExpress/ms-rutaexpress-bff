package com.rutaexpress.ms_rutaexpress_bff.port.out;

// Envío tal como lo responde shipments (contrato). Fechas como texto: el BFF no las procesa
public record ShipmentView(
        Long id,
        String trackingNumber,
        String serviceId,
        String originAddress,
        String destinationAddress,
        String recipientName,
        String recipientEmail,
        String customerId,
        String status,
        String createdAt,
        String updatedAt
) {}
