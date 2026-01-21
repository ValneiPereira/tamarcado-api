package com.tamarcado.adapter.out.geocoding.dto;

public record ViaCepResponse(
        String cep,
        String logradouro,
        String complemento,
        String bairro,
        String localidade,
        String uf,
        boolean erro
) {}
