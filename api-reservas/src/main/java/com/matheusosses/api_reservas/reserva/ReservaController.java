package com.matheusosses.api_reservas.reserva;

import com.matheusosses.api_reservas.reserva.dto.NewReservaDto;
import com.matheusosses.api_reservas.reserva.dto.ReservaDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservas")
@RequiredArgsConstructor
@Tag(name = "Reservas", description = "Endpoints para gerenciamento de reservas de mesas")
public class ReservaController {

    private final ReservaService service;

    @PostMapping
    @Operation(summary = "Solicitar uma nova reserva",
        description = "Este endpoint recebe o pedido e envia para processamento assíncrono.")
    public ResponseEntity<ReservaDto> solicitarReserva(@RequestBody NewReservaDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.solicitarReserva(dto));
    }
}
