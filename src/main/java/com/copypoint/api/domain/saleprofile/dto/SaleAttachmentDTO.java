package com.copypoint.api.domain.saleprofile.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SaleAttachmentDTO(
        @NotNull(message = "Profile ID es requerido")
        Long profileId,

        @NotNull(message = "Service ID es requerido")
        Long serviceId,

        @NotNull(message = "Número de copias es requerido")
        @Min(value = 1, message = "El número de copias debe ser mayor a 0")
        Integer copies,

        /**
         * Si es true, usará las páginas calculadas del attachment para determinar la cantidad
         * Si es false o null, mantendrá la cantidad existente del SaleProfile
         */
        Boolean useCalculatedQuantity
) {
    /**
     * Constructor con valores por defecto
     */
    public SaleAttachmentDTO(Long profileId, Long serviceId, Integer copies) {
        this(profileId, serviceId, copies, true);
    }

    /**
     * Constructor mínimo con 1 copia por defecto
     */
    public SaleAttachmentDTO(Long profileId, Long serviceId) {
        this(profileId, serviceId, 1, true);
    }
}
