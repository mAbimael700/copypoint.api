package com.copypoint.api.domain.saleprofile.service;

import com.copypoint.api.domain.profile.Profile;
import com.copypoint.api.domain.profile.ProfileRepository;
import com.copypoint.api.domain.sale.Sale;
import com.copypoint.api.domain.sale.SaleStatus;
import com.copypoint.api.domain.sale.dto.SaleDTO;
import com.copypoint.api.domain.sale.repository.SaleRepository;
import com.copypoint.api.domain.saleprofile.SaleProfile;
import com.copypoint.api.domain.saleprofile.SaleProfileId;
import com.copypoint.api.domain.saleprofile.dto.SaleProfileCreationDTO;
import com.copypoint.api.domain.saleprofile.dto.SaleProfileDTO;
import com.copypoint.api.domain.saleprofile.dto.SaleProfileUpdateDTO;
import com.copypoint.api.domain.saleprofile.repository.SaleProfileRepository;
import com.copypoint.api.domain.service.repository.ServiceRepository;
import com.copypoint.api.infra.exchangerate.service.ExchangeRateService;
import com.copypoint.api.infra.utils.CurrencyUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
@Service
public class SaleProfileService {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private SaleProfileRepository saleProfileRepository;

    @Autowired
    private ExchangeRateService exchangeRateService;

    /**
     * Agregar un profile a una venta PENDING
     */
    @Transactional
    public SaleDTO addProfileToSale(Long saleId, SaleProfileCreationDTO saleProfileDTO) {
        log.debug("Iniciando proceso para agregar profile {} a venta {}", saleProfileDTO.profileId(), saleId);

        // Validar que la venta existe y está en estado PENDING
        Optional<Sale> saleOpt = saleRepository.findById(saleId);
        if (saleOpt.isEmpty()) {
            log.error("Venta no encontrada con ID: {}", saleId);
            throw new RuntimeException("Venta no encontrada con ID: " + saleId);
        }

        Sale sale = saleOpt.get();
        if (sale.getStatus() != SaleStatus.PENDING) {
            log.warn("Intento de agregar producto a venta en estado no válido. Sale ID: {}, Estado: {}", saleId, sale.getStatus());
            throw new RuntimeException("Solo se pueden agregar productos a ventas en estado PENDING. Estado actual: " + sale.getStatus());
        }

        // Validar que el profile existe
        Optional<Profile> profile = profileRepository.findById(saleProfileDTO.profileId());
        if (profile.isEmpty()) {
            log.error("Profile no encontrado con ID: {}", saleProfileDTO.profileId());
            throw new RuntimeException("Profile not found with ID: " + saleProfileDTO.profileId());
        }
        Optional<com.copypoint.api.domain.service.Service> service = serviceRepository.findById(saleProfileDTO.serviceId());
        if (service.isEmpty()) {
            log.error("Service no encontrado con ID: {}", saleProfileDTO.serviceId());
            throw new RuntimeException("Service not found with ID: " + saleProfileDTO.serviceId());
        }

        Profile profileEntity = profile.get();
        com.copypoint.api.domain.service.Service serviceEntity = service.get();

        // Verificar si ya existe este profile en la venta
        SaleProfileId saleProfileId = new SaleProfileId(saleId, saleProfileDTO.profileId(), saleProfileDTO.serviceId());
        Optional<SaleProfile> existingSaleProfile = saleProfileRepository.findById(saleProfileId);

        // Obtener el precio unitario con conversión de moneda si es necesaria
        Double unitPrice = getConvertedUnitPrice(profileEntity, sale);
        log.debug("Precio unitario calculado para profile {}: {} {}", profileEntity.getId(), unitPrice, sale.getCurrency());

        if (existingSaleProfile.isPresent()) {
            // Si ya existe, actualizar la cantidad y subtotal
            SaleProfile saleProfile = existingSaleProfile.get();
            int newQuantity = saleProfile.getQuantity() + saleProfileDTO.quantity();
            saleProfile.setQuantity(newQuantity);
            // Recalcular subtotal con el precio posiblemente convertido
            saleProfile.setUnitPrice(unitPrice);
            saleProfile.setSubtotal(newQuantity * unitPrice);
            saleProfileRepository.save(saleProfile);
            log.info("SaleProfile actualizado - Sale ID: {}, Profile ID: {}, Nueva cantidad: {}", saleId, saleProfileDTO.profileId(), newQuantity);
        } else {
            // Si no existe, crear nuevo SaleProfile
            Double subtotal = unitPrice * saleProfileDTO.quantity();

            SaleProfile newSaleProfile = SaleProfile.builder()
                    .id(saleProfileId)
                    .sale(sale)
                    .service(serviceEntity)
                    .profile(profileEntity)
                    .unitPrice(unitPrice)
                    .quantity(saleProfileDTO.quantity())
                    .subtotal(subtotal)
                    .build();

            saleProfileRepository.save(newSaleProfile);
            log.info("Nuevo SaleProfile creado - Sale ID: {}, Profile ID: {}, Cantidad: {}, Subtotal: {}",
                    saleId, saleProfileDTO.profileId(), saleProfileDTO.quantity(), subtotal);
        }

        // SOLUCIÓN: Refrescar la entidad Sale desde la base de datos
        // para asegurar que tiene los SaleProfiles más actualizados
        sale = saleRepository.findById(saleId).orElseThrow();

        // Recalcular el total de la venta
        updateSaleTotal(sale);

        // Actualizar timestamp y guardar
        sale.setUpdatedAt(LocalDateTime.now());
        Sale updatedSale = saleRepository.save(sale);

        log.info("Profile agregado exitosamente a venta. Sale ID: {}, Total actualizado: {} {}",
                saleId, updatedSale.getTotal(), updatedSale.getCurrency());

        return new SaleDTO(updatedSale);
    }

    public Page<SaleProfileDTO> getBySaleId(Long saleId, Pageable pageable) {
        log.debug("Obteniendo SaleProfiles para venta ID: {} con paginación", saleId);
        return saleProfileRepository.findByIdSaleId(saleId, pageable).map(SaleProfileDTO::new);
    }

    /**
     * Modificar la cantidad de un profile específico en una venta PENDING
     */
    @Transactional
    public SaleDTO updateProfileInSale(Long saleId, Long profileId, Long serviceId, Integer newQuantity) {
        log.debug("Actualizando cantidad de profile {} en venta {} a {}", profileId, saleId, newQuantity);

        // Validar que la venta existe y está en estado PENDING
        Optional<Sale> saleOpt = saleRepository.findById(saleId);
        if (saleOpt.isEmpty()) {
            log.error("Venta no encontrada con ID: {}", saleId);
            throw new RuntimeException("Venta no encontrada con ID: " + saleId);
        }

        Sale sale = saleOpt.get();
        if (sale.getStatus() != SaleStatus.PENDING) {
            log.warn("Intento de modificar producto en venta con estado no válido. Sale ID: {}, Estado: {}", saleId, sale.getStatus());
            throw new RuntimeException("Solo se pueden modificar productos en ventas en estado PENDING. Estado actual: " + sale.getStatus());
        }

        // Validar que la cantidad es positiva
        if (newQuantity <= 0) {
            log.error("Cantidad inválida: {}. Debe ser mayor a 0", newQuantity);
            throw new RuntimeException("La cantidad debe ser mayor a 0");
        }

        // Buscar el SaleProfile existente
        SaleProfileId saleProfileId = new SaleProfileId(saleId, profileId, serviceId);
        Optional<SaleProfile> existingSaleProfile = saleProfileRepository.findById(saleProfileId);

        if (existingSaleProfile.isEmpty()) {
            log.error("SaleProfile no encontrado - Sale ID: {}, Profile ID: {}, Service ID: {}", saleId, profileId, serviceId);
            throw new RuntimeException("Profile no encontrado en la venta. Sale ID: " + saleId + ", Profile ID: " + profileId);
        }

        // Actualizar la cantidad y subtotal, manteniendo la conversión de moneda
        SaleProfile saleProfile = existingSaleProfile.get();
        saleProfile.setQuantity(newQuantity);
        saleProfile.setSubtotal(saleProfile.getUnitPrice() * newQuantity);
        saleProfileRepository.save(saleProfile);

        // Refrescar la entidad Sale desde la base de datos
        sale = saleRepository.findById(saleId).orElseThrow();

        // Recalcular el total de la venta
        updateSaleTotal(sale);

        // Actualizar timestamp y guardar
        sale.setUpdatedAt(LocalDateTime.now());
        Sale updatedSale = saleRepository.save(sale);

        log.info("SaleProfile actualizado exitosamente - Sale ID: {}, Profile ID: {}, Nueva cantidad: {}, Total venta: {} {}",
                saleId, profileId, newQuantity, updatedSale.getTotal(), updatedSale.getCurrency());

        return new SaleDTO(updatedSale);
    }

    /**
     * Versión alternativa usando DTO para mayor consistencia
     */
    @Transactional
    public SaleDTO updateProfileInSale(Long saleId, Long profileId, Long serviceId, SaleProfileUpdateDTO updateDTO) {
        return updateProfileInSale(saleId, profileId, serviceId, updateDTO.quantity());
    }

    /**
     * Valida si el código de moneda es válido según ISO 4217
     */
    private boolean isValidCurrencyCode(String currencyCode) {
        return CurrencyUtils.isValidCurrencyCode(currencyCode);
    }

    /**
     * Obtiene la tasa de cambio entre dos monedas con cache
     */
    @Cacheable(value = "exchangeRates", key = "#fromCurrency + '-' + #toCurrency")
    public BigDecimal getCachedExchangeRate(String fromCurrency, String toCurrency) {
        log.debug("Obteniendo tasa de cambio de {} a {} (sin cache)", fromCurrency, toCurrency);
        return exchangeRateService.getExchangeRate(fromCurrency, toCurrency);
    }

    /**
     * Obtiene el precio unitario del profile convertido a la moneda de la venta si es necesario
     */
    private Double getConvertedUnitPrice(Profile profile, Sale sale) {
        // Obtener y normalizar las monedas
        String storeCurrency = CurrencyUtils.normalizeCurrencyCode(sale.getCopypoint().getStore().getCurrency());
        String saleCurrency = CurrencyUtils.normalizeCurrencyCode(sale.getCurrency());

        // Validar códigos de moneda
        if (storeCurrency == null) {
            log.warn("Código de moneda del store inválido: {}", sale.getCopypoint().getStore().getCurrency());
            return profile.getUnitPrice();
        }

        if (saleCurrency == null) {
            log.warn("Código de moneda de la venta inválido: {}", sale.getCurrency());
            return profile.getUnitPrice();
        }

        // Si las monedas son iguales, no hay conversión necesaria
        if (CurrencyUtils.areCurrenciesEqual(storeCurrency, saleCurrency)) {
            log.debug("Misma moneda para store y venta ({}), no se requiere conversión", storeCurrency);
            return profile.getUnitPrice();
        }

        // Validar que el precio original es válido
        if (!CurrencyUtils.isValidPositiveAmount(profile.getUnitPrice())) {
            log.warn("Precio unitario inválido para profile ID {}: {}", profile.getId(), profile.getUnitPrice());
            return 0.0;
        }

        try {
            log.info("Convirtiendo precio de {} {} a {} para profile ID: {}",
                    storeCurrency, profile.getUnitPrice(), saleCurrency, profile.getId());

            // Obtener la tasa de cambio y convertir (usando cache)
            BigDecimal exchangeRate = getCachedExchangeRate(storeCurrency, saleCurrency);
            Double result = CurrencyUtils.convertAmount(profile.getUnitPrice(), exchangeRate, saleCurrency);

            log.info("Conversión exitosa - Precio original: {} {}, Tasa: {}, Precio convertido: {} {}",
                    profile.getUnitPrice(), storeCurrency, exchangeRate, result, saleCurrency);

            return result;

        } catch (Exception e) {
            // En caso de error en la conversión, registrar el error y usar precio original
            log.error("Error convirtiendo moneda de {} a {} para profile ID {}: {}",
                    storeCurrency, saleCurrency, profile.getId(), e.getMessage(), e);
            return profile.getUnitPrice();
        }
    }

    /**
     * Convierte el descuento de la venta si es necesario
     */
    private Double getConvertedDiscount(Sale sale, Double discount) {
        if (!CurrencyUtils.isValidPositiveAmount(discount)) {
            return discount;
        }

        // Obtener y normalizar las monedas
        String storeCurrency = CurrencyUtils.normalizeCurrencyCode(sale.getCopypoint().getStore().getCurrency());
        String saleCurrency = CurrencyUtils.normalizeCurrencyCode(sale.getCurrency());

        // Validar códigos de moneda
        if (storeCurrency == null || saleCurrency == null) {
            log.warn("Códigos de moneda inválidos - Store: {}, Sale: {}",
                    sale.getCopypoint().getStore().getCurrency(), sale.getCurrency());
            return discount;
        }

        // Si las monedas son iguales, no hay conversión necesaria
        if (CurrencyUtils.areCurrenciesEqual(storeCurrency, saleCurrency)) {
            return discount;
        }

        try {
            log.debug("Convirtiendo descuento de {} {} a {} para venta ID: {}",
                    storeCurrency, discount, saleCurrency, sale.getId());

            // Obtener la tasa de cambio y convertir (usando cache)
            BigDecimal exchangeRate = getCachedExchangeRate(storeCurrency, saleCurrency);
            Double result = CurrencyUtils.convertAmount(discount, exchangeRate, saleCurrency);

            log.info("Descuento convertido de {} {} a {} {} para venta ID: {}",
                    discount, storeCurrency, result, saleCurrency, sale.getId());

            return result;

        } catch (Exception e) {
            // En caso de error en la conversión, usar descuento original
            log.error("Error convirtiendo descuento de {} a {} para venta ID {}: {}",
                    storeCurrency, saleCurrency, sale.getId(), e.getMessage(), e);
            return discount;
        }
    }

    /**
     * Recalcula el total de una venta basado en sus perfiles
     */
    private void updateSaleTotal(Sale sale) {
        log.debug("Recalculando total para venta ID: {}", sale.getId());

        // Obtener todos los subtotales
        Double[] subtotals = saleProfileRepository.findByIdSaleId(sale.getId())
                .stream()
                .mapToDouble(SaleProfile::getSubtotal)
                .boxed()
                .toArray(Double[]::new);

        // Calcular total usando utilidades de moneda
        Double total = CurrencyUtils.calculateTotal(subtotals, sale.getCurrency());

        log.debug("Subtotal calculado antes de descuento: {} {} para venta ID: {}",
                total, sale.getCurrency(), sale.getId());

        // Aplicar descuento si existe (convertido si es necesario)
        if (CurrencyUtils.isValidPositiveAmount(sale.getDiscount())) {
            Double convertedDiscount = getConvertedDiscount(sale, sale.getDiscount());
            total = CurrencyUtils.applyDiscount(total, convertedDiscount, sale.getCurrency());

            log.debug("Descuento aplicado: {} {}, Total después de descuento: {} {}",
                    convertedDiscount, sale.getCurrency(), total, sale.getCurrency());
        }

        sale.setTotal(total);
        sale.setUpdatedAt(LocalDateTime.now());

        log.info("Total final calculado para venta ID {}: {} {}",
                sale.getId(), CurrencyUtils.formatAmountWithSymbol(sale.getTotal(), sale.getCurrency()));
    }
}