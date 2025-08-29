package com.example.servicea.service;

import com.example.servicea.model.CbarResponse;
import com.example.servicea.model.Currency;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class CbarService {
    
    private static final Logger log = LoggerFactory.getLogger(CbarService.class);
    
    private final RestTemplate restTemplate;
    private final XmlMapper xmlMapper;
    private static final String CBAR_BASE_URL = "https://cbar.az/currencies";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    
    public CbarService() {
        this.restTemplate = new RestTemplate();
        this.xmlMapper = new XmlMapper();
        // Configure XML mapper to use UTF-8
        this.xmlMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        this.xmlMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_COMMENTS, true);
        // Configure for proper UTF-8 handling
        this.xmlMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        this.xmlMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
    }
    
    public List<Currency> getCurrencies(String date) {
        try {
            log.info("Fetching currencies for date: {}", date);
            
            // Format date for CBAR API (dd.MM.yyyy)
            LocalDate localDate = LocalDate.parse(date);
            String formattedDate = localDate.format(DATE_FORMATTER);
            
            String url = CBAR_BASE_URL + "/" + formattedDate + ".xml";
            log.info("Fetching from URL: {}", url);
            
            // Create headers with UTF-8 encoding
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/xml;charset=UTF-8");
            headers.set("Accept-Charset", "UTF-8");
            headers.set("Content-Type", "application/xml;charset=UTF-8");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // Make request with proper encoding
            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                String.class
            );
            
            String xmlResponse = response.getBody();
            
            if (xmlResponse == null) {
                throw new RuntimeException("Empty response from CBAR API");
            }
            
            log.info("Raw XML response length: {}", xmlResponse.length());
            
            CbarResponse cbarResponse = xmlMapper.readValue(xmlResponse, CbarResponse.class);
            
            List<Currency> currencies = new ArrayList<>();
            
            if (cbarResponse.getValTypes() != null) {
                log.info("Found {} ValType sections", cbarResponse.getValTypes().size());
                for (CbarResponse.ValType valType : cbarResponse.getValTypes()) {
                    log.info("Processing ValType: {}", valType.getType());
                    if (valType.getValutes() != null) {
                        log.info("Found {} valutes in ValType: {}", valType.getValutes().size(), valType.getType());
                        for (CbarResponse.Valute valute : valType.getValutes()) {
                            String currencyName = valute.getName();
                            String currencyCode = valute.getCode();
                            BigDecimal exchangeRate = parseExchangeRate(valute.getValue(), valute.getNominal());
                            
                            // Log the currency name and rate for debugging
                            log.info("Processing currency: {} (Code: {}) - Raw Value: {}, Nominal: {}, Calculated Rate: {}", 
                                    currencyName, currencyCode, valute.getValue(), valute.getNominal(), exchangeRate);
                            
                            // Fix encoding issues in currency name
                            String fixedCurrencyName = fixEncoding(currencyName);
                            
                            Currency currency = new Currency(currencyCode, fixedCurrencyName, exchangeRate);
                            log.info("Created Currency object: code={}, name={}, rate={}", 
                                    currency.getCode(), currency.getName(), currency.getRate());
                            currencies.add(currency);
                        }
                    }
                }
            }
            
            log.info("Successfully parsed {} currencies with exchange rates", currencies.size());
            return currencies;
            
        } catch (Exception e) {
            log.error("Error fetching currencies for date {}: {}", date, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch currencies from CBAR: " + e.getMessage(), e);
        }
    }
    
    /**
     * Parse exchange rate from CBAR value and nominal
     * @param value The exchange rate value from CBAR
     * @param nominal The nominal amount (usually 1, but can be "1 t.u." for precious metals)
     * @return The parsed exchange rate
     */
    private BigDecimal parseExchangeRate(String value, String nominal) {
        try {
            log.info("parseExchangeRate called with value='{}', nominal='{}'", value, nominal);
            
            // Remove any whitespace and replace comma with dot for decimal
            String cleanValue = value.trim().replace(",", ".");
            String cleanNominal = nominal.trim().replace(",", ".");
            
            log.info("After cleaning - cleanValue='{}', cleanNominal='{}'", cleanValue, cleanNominal);
            
            // Extract numeric part from nominal (e.g., "1 t.u." -> "1")
            String numericNominal = extractNumericPart(cleanNominal);
            
            log.info("Extracted numeric nominal: '{}'", numericNominal);
            
            BigDecimal rateValue = new BigDecimal(cleanValue);
            BigDecimal nominalValue = new BigDecimal(numericNominal);
            
            log.info("Parsed values - rateValue={}, nominalValue={}", rateValue, nominalValue);
            
            // Calculate the rate per unit (divide by nominal)
            log.info("About to perform division: {} / {}", rateValue, nominalValue);
            BigDecimal result = rateValue.divide(nominalValue, 6, BigDecimal.ROUND_HALF_UP);
            
            log.info("Calculated rate: {}", result);
            log.info("Rate as double: {}", result.doubleValue());
            return result;
            
        } catch (NumberFormatException e) {
            log.error("NumberFormatException parsing exchange rate: value={}, nominal={}", value, nominal, e);
            return BigDecimal.ZERO;
        } catch (ArithmeticException e) {
            log.error("ArithmeticException parsing exchange rate: value={}, nominal={}", value, nominal, e);
            return BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Unexpected error parsing exchange rate: value={}, nominal={}", value, nominal, e);
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * Extract numeric part from a string that may contain text
     * @param input String like "1 t.u." or "100"
     * @return Numeric part as string
     */
    private String extractNumericPart(String input) {
        log.info("extractNumericPart called with input: '{}'", input);
        
        // Handle special case for troy ounce notation
        if (input != null && input.trim().toLowerCase().contains("t.u.")) {
            log.info("Detected troy ounce notation, extracting numeric part");
            // Extract the number before "t.u."
            String[] parts = input.trim().split("\\s+");
            for (String part : parts) {
                try {
                    Double.parseDouble(part);
                    log.info("Found numeric part for troy ounce: '{}'", part);
                    return part;
                } catch (NumberFormatException e) {
                    // Continue searching
                }
            }
            log.info("No numeric part found in troy ounce notation, defaulting to 1");
            return "1";
        }
        
        // Remove any non-numeric characters except dots and commas
        String numericPart = input.replaceAll("[^0-9.,]", "");
        
        log.info("After regex replacement: '{}'", numericPart);
        
        // If no numeric part found, default to "1"
        if (numericPart.isEmpty()) {
            log.warn("No numeric part found in nominal '{}', defaulting to 1", input);
            return "1";
        }
        
        log.info("Returning numeric part: '{}'", numericPart);
        return numericPart;
    }

    /**
     * Extracts the proper currency code from the currency name if it's not directly available.
     * This handles cases where the code is embedded in the name (e.g., "USD (US Dollar)").
     * @param currencyName The full name of the currency.
     * @param currencyCode The code provided in the XML.
     * @return The proper currency code.
     */
    private String extractCurrencyCode(String currencyName, String currencyCode) {
        // If we have a valid code, use it
        if (currencyCode != null && !currencyCode.trim().isEmpty()) {
            return currencyCode.trim();
        }
        
        // Map common currency names to codes
        String upperName = currencyName.toUpperCase();
        if (upperName.contains("AVRO") || upperName.contains("EURO")) {
            return "EUR";
        } else if (upperName.contains("DOLLAR") && upperName.contains("ABŞ")) {
            return "USD";
        } else if (upperName.contains("FUNT") || upperName.contains("STERLİN")) {
            return "GBP";
        } else if (upperName.contains("RUBLU") || upperName.contains("RUSİYA")) {
            return "RUB";
        } else if (upperName.contains("FRANK")) {
            return "CHF";
        } else if (upperName.contains("KRONU") && upperName.contains("İSVEÇ")) {
            return "SEK";
        } else if (upperName.contains("KRONU") && upperName.contains("NORVEÇ")) {
            return "NOK";
        } else if (upperName.contains("KRONU") && upperName.contains("DANİMARKA")) {
            return "DKK";
        } else if (upperName.contains("KRONU") && upperName.contains("ÇEX")) {
            return "CZK";
        } else if (upperName.contains("YUAN")) {
            return "CNY";
        } else if (upperName.contains("YENİ")) {
            return "JPY";
        } else if (upperName.contains("DİRHƏMİ")) {
            return "AED";
        } else if (upperName.contains("DİNAR")) {
            return "KWD";
        } else if (upperName.contains("RİALI")) {
            return "QAR";
        } else if (upperName.contains("LARİ")) {
            return "GEL";
        } else if (upperName.contains("LEVİ")) {
            return "BGN";
        } else if (upperName.contains("LEVİ") && upperName.contains("BOLQARİSTAN")) {
            return "BGN";
        } else if (upperName.contains("LEVİ") && upperName.contains("MOLDOVA")) {
            return "MDL";
        } else if (upperName.contains("LEVİ") && upperName.contains("RUMINİYA")) {
            return "RON";
        } else if (upperName.contains("ZLO") || upperName.contains("POLŞA")) {
            return "PLN";
        } else if (upperName.contains("FORİNT")) {
            return "HUF";
        } else if (upperName.contains("SOMU") && upperName.contains("QIRĞIZ")) {
            return "KGS";
        } else if (upperName.contains("SOMU") && upperName.contains("ÖZBƏK")) {
            return "UZS";
        } else if (upperName.contains("TENGƏSİ")) {
            return "KZT";
        } else if (upperName.contains("QRİVNASI")) {
            return "UAH";
        } else if (upperName.contains("MANATI") && upperName.contains("TÜRKMƏNİSTAN")) {
            return "TMT";
        } else if (upperName.contains("LİRƏSİ")) {
            return "TRY";
        } else if (upperName.contains("RUPİSİ")) {
            return "INR";
        } else if (upperName.contains("RUPİSİ") && upperName.contains("PAKİSTAN")) {
            return "PKR";
        } else if (upperName.contains("VONU")) {
            return "KRW";
        } else if (upperName.contains("DİNAR") && upperName.contains("SERBİYA")) {
            return "RSD";
        } else if (upperName.contains("ŞEKEL")) {
            return "ILS";
        } else if (upperName.contains("DİNAR") && upperName.contains("KÜVEYT")) {
            return "KWD";
        } else if (upperName.contains("RİALI") && upperName.contains("QƏTƏR")) {
            return "QAR";
        } else if (upperName.contains("RİALI") && upperName.contains("SƏUDİYYƏ")) {
            return "SAR";
        } else if (upperName.contains("DİRHƏMİ") && upperName.contains("BƏƏ")) {
            return "AED";
        } else if (upperName.contains("DİNAR") && upperName.contains("BELARUS")) {
            return "BYN";
        } else if (upperName.contains("DİNAR") && upperName.contains("SERBİYA")) {
            return "RSD";
        } else if (upperName.contains("DİNAR") && upperName.contains("SİNQAPUR")) {
            return "SGD";
        } else if (upperName.contains("DİNAR") && upperName.contains("KANADA")) {
            return "CAD";
        } else if (upperName.contains("DİNAR") && upperName.contains("AVSTRALİYA")) {
            return "AUD";
        } else if (upperName.contains("DİNAR") && upperName.contains("YENİ ZELLANDİYA")) {
            return "NZD";
        } else if (upperName.contains("DİNAR") && upperName.contains("HONQ KONQ")) {
            return "HKD";
        } else if (upperName.contains("DİNAR") && upperName.contains("İRAN")) {
            return "IRR";
        }
        
        // If no mapping found, try to extract from parentheses
        int openParenIndex = currencyName.indexOf('(');
        int closeParenIndex = currencyName.indexOf(')');
        if (openParenIndex != -1 && closeParenIndex != -1 && closeParenIndex > openParenIndex) {
            return currencyName.substring(openParenIndex + 1, closeParenIndex).trim();
        }
        
        // Return the original code if nothing else works
        return currencyCode != null ? currencyCode : "UNKNOWN";
    }
    
    /**
     * Map known problematic currency names to their correct forms
     * @param text The text to map
     * @return The mapped text or null if no mapping found
     */
    private String mapCurrencyName(String text) {
        if (text == null) {
            return null;
        }
        
        // Map known problematic currency names
        switch (text.trim()) {
            case "QÄ±zÄ±l":
                return "Qızıl";
            case "GÃ¼mÃ¼Å":
                return "Gümüş";
            case "Palladium":
                return "Palladium";
            case "Platin":
                return "Platin";
            case "1 Serbiya dinarA+ (RSD)":
                return "1 Serbiya dinarı (RSD)";
            case "1 Sinqapur dollarÄ± (SGD)":
                return "1 Sinqapur dolları (SGD)";
            case "1 SÉQudiyyÉO ÆrÉ bistanÄ± rialÄ± (SAR)":
                return "1 Səudiyyə Ərəbistanı rialı (SAR)";
            case "1 TÃ¼rk lirÉOsi (TRY)":
                return "1 Türk lirəsi (TRY)";
            case "1 TÃ¼rkmÉOnistan manatÄ± (TMT)":
                return "1 Türkmənistan manatı (TMT)";
            case "1 Ukrayna qrivnasÄ± (UAH)":
                return "1 Ukrayna qrivnası (UAH)";
            case "1 Yeni Zelandiya dollarÄ± (NZD)":
                return "1 Yeni Zelandiya dolları (NZD)";
            case "1 BÆÆ dirhÉmi (AED)":
                return "1 BƏƏ dirhəmi (AED)";
            case "100 QazaxÄ±stan tengÉsi (KZT)":
                return "100 Qazaxıstan tengəsi (KZT)";
            case "1 QÉtÉr rialÄ± (QAR)":
                return "1 Qətər rialı (QAR)";
            case "1 QÄ±rÄÄ±z somu (KGS)":
                return "1 Qırğız somu (KGS)";
            case "100 MacarÄ±stan forinti (HUF)":
                return "100 Macarıstan forinti (HUF)";
            case "1 Moldova leyi (MDL)":
                return "1 Moldova leyi (MDL)";
            case "1 NorveÃ§ kronu (NOK)":
                return "1 Norveç kronu (NOK)";
            case "100 ÃzbÉk somu (UZS)":
                return "100 Özbək somu (UZS)";
            case "100 Pakistan rupisi (PKR)":
                return "100 Pakistan rupisi (PKR)";
            case "1 PolÅa zlotÄ±sÄ± (PLN)":
                return "1 Polşa zlotısı (PLN)";
            case "1 RumÄ±niya leyi (RON)":
                return "1 Rumıniya leyi (RON)";
            case "100 Rusiya rublu (RUB)":
                return "100 Rusiya rublu (RUB)";
            case "1 Serbiya dinarÄ± (RSD)":
                return "1 Serbiya dinarı (RSD)";
            case "1 SÉudiyyÉ ÆrÉbistanÄ± rialÄ± (SAR)":
                return "1 Səudiyyə Ərəbistanı rialı (SAR)";
            case "1 SDR (BVF-nin xÃ¼susi borcalma hÃ¼quqlarÄ±) (SDR)":
                return "1 SDR (BVF-nin xüsusi borcalma hüquqları) (SDR)";
            case "1 Ä°ngilis funt sterlinqi (GBP)":
                return "1 İngilis funt sterlinqi (GBP)";
            case "1 Ä°sveÃ§ kronu (SEK)":
                return "1 İsveç kronu (SEK)";
            case "1 Ä°sveÃ§rÉ frankÄ± (CHF)":
                return "1 İsveçrə frankı (CHF)";
            case "1 Ä°srail Åekeli (ILS)":
                return "1 İsrail Şekeli (ILS)";
            case "1 KÃ¼veyt dinarÄ± (KWD)":
                return "1 Küveyt dinarı (KWD)";
            case "100 Yapon yeni (JPY)":
                return "100 Yapon yeni (JPY)";
            default:
                return null;
        }
    }
    
    /**
     * Fix encoding issues in currency names
     * @param text The text with encoding issues
     * @return The fixed text
     */
    private String fixEncoding(String text) {
        if (text == null) {
            return null;
        }
        
        // First, try to map known problematic currency names
        String mapped = mapCurrencyName(text);
        if (mapped != null) {
            return mapped;
        }
        
        // If no mapping found, apply general encoding fixes
        
        // Common encoding fixes for Azerbaijani characters
        String fixed = text
            .replace("Ä±", "ı")
            .replace("É", "ə")
            .replace("Å", "ş")
            .replace("Ä", "ı")
            .replace("ÉO", "ə")
            .replace("A+", "ı")
            .replace("Ã¹¼", "ü")
            .replace("Ã¼", "ü")
            .replace("Ã", "ü")
            .replace("¹¼", "ü")
            .replace("Æ", "ə")
            .replace("O", "ə")
            .replace("bistanÄ±", "bistanı")
            .replace("dinarA+", "dinarı")
            .replace("dollarÄ±", "dolları")
            .replace("rialÄ±", "rialı")
            .replace("lirÉOsi", "lirəsi")
            .replace("manatÄ±", "manatı")
            .replace("qrivnasÄ±", "qrivnası")
            .replace("yeni", "yeni")
            .replace("dollarÄ±", "dolları")
            .replace("larÄ±", "ları")
            .replace("dirhÉmi", "dirhəmi")
            .replace("tengÉsi", "tengəsi")
            .replace("somu", "somu")
            .replace("forinti", "forinti")
            .replace("levi", "levi")
            .replace("kronu", "kronu")
            .replace("rupisi", "rupisi")
            .replace("dinarÄ±", "dinarı")
            .replace("rialÄ±", "rialı")
            .replace("vonu", "vonu")
            .replace("sekeli", "şekeli")
            .replace("larÄ±", "ları")
            .replace("borcalma hÃ¼quqlarÄ±", "borcalma hüquqları")
            .replace("xÃ¼susi", "xüsusi")
            // Additional fixes for specific currency names
            .replace("SÉQudiyyÉO ÆrÉ bistanÄ±", "Səudiyyə Ərəbistanı")
            .replace("Serbiya dinarA+", "Serbiya dinarı")
            .replace("Sinqapur dollarÄ±", "Sinqapur dolları")
            .replace("TÃ¼rk lirÉOsi", "Türk lirəsi")
            .replace("TÃ¼rkmÉOnistan manatÄ±", "Türkmənistan manatı")
            .replace("Ukrayna qrivnasÄ±", "Ukrayna qrivnası")
            .replace("Yapon yeni", "Yapon yeni")
            .replace("Yeni Zelandiya dollarÄ±", "Yeni Zelandiya dolları")
            .replace("BÆÆ dirhÉmi", "BƏƏ dirhəmi")
            .replace("QazaxÄ±stan tengÉsi", "Qazaxıstan tengəsi")
            .replace("QÄ±rÄÄ±z somu", "Qırğız somu")
            .replace("ÃzbÉk somu", "Özbək somu")
            .replace("Pakistan rupisi", "Pakistan rupisi")
            .replace("PolÅa zlotÄ±sÄ±", "Polşa zlotısı")
            .replace("RumÄ±niya leyi", "Rumıniya leyi")
            .replace("Rusiya rublu", "Rusiya rublu")
            .replace("Serbiya dinarÄ±", "Serbiya dinarı")
            .replace("Sinqapur dollarÄ±", "Sinqapur dolları")
            .replace("SÉudiyyÉ ÆrÉbistanÄ± rialÄ±", "Səudiyyə Ərəbistanı rialı")
            .replace("SDR (BVF-nin xÃ¼susi borcalma hÃ¼quqlarÄ±)", "SDR (BVF-nin xüsusi borcalma hüquqları)")
            .replace("Ä°ngilis funt sterlinqi", "İngilis funt sterlinqi")
            .replace("Ä°sveÃ§ kronu", "İsveç kronu")
            .replace("Ä°sveÃ§rÉ frankÄ±", "İsveçrə frankı")
            .replace("Ä°srail Åekeli", "İsrail Şekeli")
            .replace("KÃ¼veyt dinarÄ±", "Küveyt dinarı")
            .replace("QÉtÉr rialÄ±", "Qətər rialı")
            .replace("QÄ±rÄÄ±z somu", "Qırğız somu")
            .replace("MacarÄ±stan forinti", "Macarıstan forinti")
            .replace("Moldova leyi", "Moldova leyi")
            .replace("NorveÃ§ kronu", "Norveç kronu")
            .replace("ÃzbÉk somu", "Özbək somu")
            .replace("PolÅa zlotÄ±sÄ±", "Polşa zlotısı")
            .replace("RumÄ±niya leyi", "Rumıniya leyi")
            .replace("Rusiya rublu", "Rusiya rublu")
            .replace("Serbiya dinarÄ±", "Serbiya dinarı")
            .replace("Sinqapur dollarÄ±", "Sinqapur dolları")
            .replace("SÉudiyyÉ ÆrÉbistanÄ± rialÄ±", "Səudiyyə Ərəbistanı rialı")
            .replace("TÃ¼rk lirÉOsi", "Türk lirəsi")
            .replace("TÃ¼rkmÉOnistan manatÄ±", "Türkmənistan manatı")
            .replace("Ukrayna qrivnasÄ±", "Ukrayna qrivnası")
            .replace("Yapon yeni", "Yapon yeni")
            .replace("Yeni Zelandiya dollarÄ±", "Yeni Zelandiya dolları");
        
        return fixed;
    }
}
