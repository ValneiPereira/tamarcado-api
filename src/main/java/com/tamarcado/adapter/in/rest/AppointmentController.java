package com.tamarcado.adapter.in.rest;

import com.tamarcado.adapter.in.rest.contract.AppointmentControllerApi;

import com.tamarcado.application.service.AppointmentService;
import com.tamarcado.domain.model.appointment.AppointmentStatus;
import com.tamarcado.shared.dto.request.CreateAppointmentRequest;
import com.tamarcado.shared.dto.response.ApiResponse;
import com.tamarcado.shared.dto.response.AppointmentProfessionalResponse;
import com.tamarcado.shared.dto.response.AppointmentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AppointmentController implements AppointmentControllerApi {

    private final AppointmentService appointmentService;

    @Override
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(CreateAppointmentRequest request) {
        log.debug("Criando novo agendamento");
        var appointment = appointmentService.createAppointment(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(appointment, "Agendamento criado com sucesso"));
    }

    @Override
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getAppointmentsByClient(AppointmentStatus status) {
        log.debug("Listando agendamentos do cliente com status: {}", status);

        var appointments = appointmentService.getAppointmentsByClient(status);
        return ResponseEntity.ok(ApiResponse.success(appointments, "Agendamentos encontrados com sucesso"));
    }

    @Override
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointmentById(UUID id) {
        log.debug("Buscando agendamento: {}", id);

        AppointmentResponse appointment = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(ApiResponse.success(appointment, "Agendamento encontrado com sucesso"));
    }

    @Override
    public ResponseEntity<ApiResponse<String>> cancelAppointment(UUID id) {
        log.debug("Cancelando agendamento: {}", id);
        appointmentService.cancelAppointment(id);

        return ResponseEntity
                .ok(ApiResponse.success("Agendamento cancelado com sucesso", "Agendamento cancelado com sucesso"));
    }

    @Override
    public ResponseEntity<ApiResponse<List<AppointmentProfessionalResponse>>> getAppointmentsByProfessional(
            AppointmentStatus status) {
        log.debug("Listando agendamentos do profissional com status: {}", status);

        List<AppointmentProfessionalResponse> appointments = appointmentService.getAppointmentsByProfessional(status);

        return ResponseEntity.ok(
                ApiResponse.success(appointments, "Agendamentos encontrados com sucesso"));
    }

    @Override
    public ResponseEntity<ApiResponse<String>> acceptAppointment(UUID id) {
        log.debug("Aceitando agendamento: {}", id);

        appointmentService.acceptAppointment(id);

        return ResponseEntity.ok(
                ApiResponse.success("Agendamento aceito com sucesso", "Agendamento aceito com sucesso"));
    }

    @Override
    public ResponseEntity<ApiResponse<String>> rejectAppointment(UUID id) {
        log.debug("Rejeitando agendamento: {}", id);

        appointmentService.rejectAppointment(id);

        return ResponseEntity.ok(
                ApiResponse.success("Agendamento rejeitado com sucesso", "Agendamento rejeitado com sucesso"));
    }

    @Override
    public ResponseEntity<ApiResponse<String>> completeAppointment(UUID id) {
        log.debug("Completando agendamento: {}", id);

        appointmentService.completeAppointment(id);

        return ResponseEntity.ok(
                ApiResponse.success("Agendamento completado com sucesso", "Agendamento completado com sucesso"));
    }
}
