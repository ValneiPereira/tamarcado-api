package com.tamarcado.shared.dto.response;

public record AddressResponse(
        String cep,
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state,
        Double latitude,
        Double longitude
) {}
