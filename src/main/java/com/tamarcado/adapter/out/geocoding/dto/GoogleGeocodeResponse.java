package com.tamarcado.adapter.out.geocoding.dto;

import java.util.List;

public record GoogleGeocodeResponse(
    String status,
    List<Result> results
) {

    public record Result(
        Geometry geometry
    ) {}

    public record Geometry(
        Location location
    ) {}

    public record Location(
        double lat,
        double lng
    ) {}
}
