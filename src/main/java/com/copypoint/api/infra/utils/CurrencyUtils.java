package com.copypoint.api.infra.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Clase de utilidades para manejo y validación de monedas
 * <p>
 * Proporciona métodos estáticos para validar códigos de moneda,
 * formatear cantidades monetarias y manejar conversiones.
 */
@Slf4j
public final class CurrencyUtils {
    // Cache para evitar validaciones repetitivas de códigos de moneda
    private static final Map<String, Boolean> CURRENCY_VALIDATION_CACHE = new ConcurrentHashMap<>();

    // Patrón para validar códigos de moneda (3 letras mayúsculas)
    private static final Pattern CURRENCY_PATTERN = Pattern.compile("^[A-Z]{3}$");

    // Conjunto de códigos de moneda más comunes para validación rápida
    private static final Set<String> COMMON_CURRENCY_CODES = Set.of(
            "USD", "EUR", "JPY", "GBP", "AUD", "CAD", "CHF", "CNY", "SEK", "NZD",
            "MXN", "SGD", "HKD", "NOK", "KRW", "TRY", "RUB", "INR", "BRL", "ZAR",
            "PLN", "DKK", "CZK", "HUF", "ILS", "THB", "PHP", "MYR", "IDR", "CLP",
            "ARS", "COP", "PEN", "UYU", "VES", "BOB", "PYG", "GTQ", "CRC", "PAB",
            "DOP", "JMD", "TTD", "BBD", "BSD", "BZD", "XCD", "AWG", "SRD", "GYD"
    );

    // Constructor privado para evitar instanciación
    private CurrencyUtils() {
        throw new UnsupportedOperationException("Esta es una clase de utilidades y no debe ser instanciada");
    }

    /**
     * Valida si un código de moneda es válido según ISO 4217
     *
     * @param currencyCode el código de moneda a validar
     * @return true si el código es válido, false en caso contrario
     */
    public static boolean isValidCurrencyCode(String currencyCode) {
        if (!StringUtils.hasText(currencyCode)) {
            log.debug("Código de moneda vacío o nulo");
            return false;
        }

        String upperCurrencyCode = currencyCode.trim().toUpperCase();

        // Verificar en cache primero
        Boolean cachedResult = CURRENCY_VALIDATION_CACHE.get(upperCurrencyCode);
        if (cachedResult != null) {
            return cachedResult;
        }

        boolean isValid = false;

        // Validación rápida con patrón regex
        if (!CURRENCY_PATTERN.matcher(upperCurrencyCode).matches()) {
            CURRENCY_VALIDATION_CACHE.put(upperCurrencyCode, false);
            return false;
        }

        // Verificar en conjunto de monedas comunes primero (más rápido)
        if (COMMON_CURRENCY_CODES.contains(upperCurrencyCode)) {
            isValid = true;
        } else {
            // Validación completa usando Java Currency API
            try {
                Currency.getInstance(upperCurrencyCode);
                isValid = true;
                log.debug("Código de moneda {} validado usando Currency API", upperCurrencyCode);
            } catch (IllegalArgumentException e) {
                log.debug("Código de moneda inválido: {}", upperCurrencyCode);
                isValid = false;
            }
        }

        // Guardar en cache
        CURRENCY_VALIDATION_CACHE.put(upperCurrencyCode, isValid);
        return isValid;
    }

    /**
     * Redondea una cantidad monetaria a la precisión adecuada para la moneda
     *
     * @param amount       la cantidad a redondear
     * @param currencyCode el código de moneda
     * @return la cantidad redondeada
     */
    public static BigDecimal roundToMonetaryPrecision(BigDecimal amount, String currencyCode) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }

        int fractionDigits = getDefaultFractionDigits(currencyCode);
        return amount.setScale(fractionDigits, RoundingMode.HALF_UP);
    }

    /**
     * Redondea una cantidad monetaria a la precisión adecuada para la moneda
     *
     * @param amount       la cantidad a redondear
     * @param currencyCode el código de moneda
     * @return la cantidad redondeada como Double
     */
    public static Double roundToMonetaryPrecision(Double amount, String currencyCode) {
        if (amount == null) {
            return 0.0;
        }

        BigDecimal bigDecimalAmount = BigDecimal.valueOf(amount);
        return roundToMonetaryPrecision(bigDecimalAmount, currencyCode).doubleValue();
    }

    /**
     * Obtiene el número de dígitos decimales por defecto para una moneda
     *
     * @param currencyCode el código de moneda
     * @return el número de dígitos decimales
     */
    public static int getDefaultFractionDigits(String currencyCode) {
        if (!isValidCurrencyCode(currencyCode)) {
            log.warn("Código de moneda inválido: {}, usando 2 decimales por defecto", currencyCode);
            return 2;
        }

        try {
            Currency currency = Currency.getInstance(currencyCode.toUpperCase());
            return currency.getDefaultFractionDigits();
        } catch (Exception e) {
            log.warn("Error obteniendo dígitos decimales para moneda {}: {}", currencyCode, e.getMessage());
            return 2; // Default para la mayoría de monedas
        }
    }

    /**
     * Formatea una cantidad monetaria como String
     *
     * @param amount       la cantidad
     * @param currencyCode el código de moneda
     * @return la cantidad formateada
     */
    public static String formatAmount(Double amount, String currencyCode) {
        if (amount == null) {
            return "0.00";
        }

        BigDecimal roundedAmount = roundToMonetaryPrecision(BigDecimal.valueOf(amount), currencyCode);
        int fractionDigits = getDefaultFractionDigits(currencyCode);

        return String.format("%." + fractionDigits + "f", roundedAmount.doubleValue());
    }

    /**
     * Formatea una cantidad monetaria con símbolo de moneda
     *
     * @param amount       la cantidad
     * @param currencyCode el código de moneda
     * @return la cantidad formateada con símbolo
     */
    public static String formatAmountWithSymbol(Double amount, String currencyCode) {
        String formattedAmount = formatAmount(amount, currencyCode);
        return formattedAmount + " " + currencyCode;
    }

    /**
     * Convierte una cantidad usando una tasa de cambio
     *
     * @param amount             la cantidad original
     * @param exchangeRate       la tasa de cambio
     * @param targetCurrencyCode la moneda objetivo
     * @return la cantidad convertida y redondeada
     */
    public static Double convertAmount(Double amount, BigDecimal exchangeRate, String targetCurrencyCode) {
        if (amount == null || exchangeRate == null) {
            log.warn("Cantidad o tasa de cambio nula en conversión");
            return 0.0;
        }

        BigDecimal originalAmount = BigDecimal.valueOf(amount);
        BigDecimal convertedAmount = originalAmount.multiply(exchangeRate);

        return roundToMonetaryPrecision(convertedAmount, targetCurrencyCode).doubleValue();
    }

    /**
     * Verifica si dos códigos de moneda son iguales (ignorando mayúsculas/minúsculas)
     *
     * @param currency1 primer código de moneda
     * @param currency2 segundo código de moneda
     * @return true si son iguales, false en caso contrario
     */
    public static boolean areCurrenciesEqual(String currency1, String currency2) {
        if (!StringUtils.hasText(currency1) || !StringUtils.hasText(currency2)) {
            return false;
        }

        return currency1.trim().equalsIgnoreCase(currency2.trim());
    }

    /**
     * Normaliza un código de moneda (trim y mayúsculas)
     *
     * @param currencyCode el código de moneda
     * @return el código normalizado o null si es inválido
     */
    public static String normalizeCurrencyCode(String currencyCode) {
        if (!StringUtils.hasText(currencyCode)) {
            return null;
        }

        String normalized = currencyCode.trim().toUpperCase();
        return isValidCurrencyCode(normalized) ? normalized : null;
    }

    /**
     * Obtiene el símbolo de una moneda
     *
     * @param currencyCode el código de moneda
     * @return el símbolo de la moneda o el código si no se encuentra el símbolo
     */
    public static String getCurrencySymbol(String currencyCode) {
        if (!isValidCurrencyCode(currencyCode)) {
            return currencyCode;
        }

        try {
            Currency currency = Currency.getInstance(currencyCode.toUpperCase());
            return currency.getSymbol();
        } catch (Exception e) {
            log.debug("Error obteniendo símbolo para moneda {}: {}", currencyCode, e.getMessage());
            return currencyCode;
        }
    }

    /**
     * Verifica si una cantidad es válida para operaciones monetarias
     *
     * @param amount la cantidad a verificar
     * @return true si es válida, false en caso contrario
     */
    public static boolean isValidAmount(Double amount) {
        return amount != null &&
                !Double.isNaN(amount) &&
                !Double.isInfinite(amount) &&
                amount >= 0;
    }

    /**
     * Verifica si una cantidad es válida y mayor a cero
     *
     * @param amount la cantidad a verificar
     * @return true si es válida y positiva, false en caso contrario
     */
    public static boolean isValidPositiveAmount(Double amount) {
        return isValidAmount(amount) && amount > 0;
    }

    /**
     * Limpia el cache de validación de monedas
     * Útil para pruebas o si se necesita refrescar las validaciones
     */
    public static void clearValidationCache() {
        CURRENCY_VALIDATION_CACHE.clear();
        log.info("Cache de validación de monedas limpiado");
    }

    /**
     * Obtiene el tamaño actual del cache de validación
     *
     * @return el número de entradas en cache
     */
    public static int getValidationCacheSize() {
        return CURRENCY_VALIDATION_CACHE.size();
    }

    /**
     * Calcula el total de una lista de cantidades en la misma moneda
     *
     * @param amounts      las cantidades a sumar
     * @param currencyCode la moneda
     * @return el total calculado y redondeado
     */
    public static Double calculateTotal(Double[] amounts, String currencyCode) {
        if (amounts == null || amounts.length == 0) {
            return 0.0;
        }

        BigDecimal total = BigDecimal.ZERO;
        for (Double amount : amounts) {
            if (isValidAmount(amount)) {
                total = total.add(BigDecimal.valueOf(amount));
            }
        }

        return roundToMonetaryPrecision(total, currencyCode).doubleValue();
    }

    /**
     * Aplica un descuento a una cantidad
     *
     * @param originalAmount la cantidad original
     * @param discountAmount el descuento a aplicar
     * @param currencyCode   la moneda
     * @return la cantidad con descuento aplicado (mínimo 0)
     */
    public static Double applyDiscount(Double originalAmount, Double discountAmount, String currencyCode) {
        if (!isValidAmount(originalAmount)) {
            return 0.0;
        }

        if (!isValidAmount(discountAmount) || discountAmount == 0) {
            return roundToMonetaryPrecision(originalAmount, currencyCode);
        }

        BigDecimal original = BigDecimal.valueOf(originalAmount);
        BigDecimal discount = BigDecimal.valueOf(discountAmount);
        BigDecimal result = original.subtract(discount);

        // Asegurar que no sea negativo
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            result = BigDecimal.ZERO;
        }

        return roundToMonetaryPrecision(result, currencyCode).doubleValue();
    }
}
