package com.tamarcado.shared.dto.response;

public record AddressResponse(
        String cep,
        String street,
        String neighborhood,
        String city,
        String state
) {}
