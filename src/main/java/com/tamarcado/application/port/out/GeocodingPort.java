package com.tamarcado.application.port.out;

import com.tamarcado.shared.dto.response.AddressResponse;
import com.tamarcado.shared.dto.response.CoordinatesResponse;

public interface GeocodingPort {

    /**
     * Converte endereço completo em coordenadas (latitude, longitude)
     * @param street Rua
     * @param number Número
     * @param city Cidade
     * @param state Estado (UF)
     * @param cep CEP
     * @return Coordenadas (latitude, longitude)
     */
    CoordinatesResponse addressToCoordinates(String street, String number, String city, String state, String cep);

    /**
     * Busca endereço completo por CEP
     * @param cep CEP (formato: 00000-000 ou 00000000)
     * @return Endereço completo
     */
    AddressResponse cepToAddress(String cep);
}
