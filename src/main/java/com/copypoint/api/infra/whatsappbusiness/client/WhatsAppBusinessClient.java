package com.copypoint.api.infra.whatsappbusiness.client;

import com.copypoint.api.infra.whatsappbusiness.dto.response.WhatsAppSendMessageRequestDTO;
import com.copypoint.api.infra.whatsappbusiness.dto.response.WhatsAppSendMessageResponseDTO;
import com.copypoint.api.infra.security.service.CredentialEncryptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

@Component
public class WhatsAppBusinessClient {
    private static final Logger logger = LoggerFactory.getLogger(WhatsAppBusinessClient.class);
    private static final String WHATSAPP_API_BASE_URL = "https://graph.facebook.com/v22.0";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CredentialEncryptionService encryptionService;

    @Autowired
    private ObjectMapper objectMapper;

    public WhatsAppSendMessageResponseDTO sendMessage(WhatsAppSendMessageRequestDTO request,
                                                      String phoneNumberId,
                                                      String encryptedAccessToken) {
        try {
            String accessToken = encryptionService.decryptCredential(encryptedAccessToken);
            String url = String.format("%s/%s/messages", WHATSAPP_API_BASE_URL, phoneNumberId);

            HttpHeaders headers = createHeaders(accessToken);
            HttpEntity<WhatsAppSendMessageRequestDTO> entity = new HttpEntity<>(request, headers);

            ResponseEntity<WhatsAppSendMessageResponseDTO> response = restTemplate.exchange(
                    URI.create(url),
                    HttpMethod.POST,
                    entity,
                    WhatsAppSendMessageResponseDTO.class
            );

            logger.info("Mensaje enviado exitosamente a WhatsApp: {}", response.getBody());
            return response.getBody();

        } catch (Exception e) {
            logger.error("Error enviando mensaje a WhatsApp: {}", e.getMessage(), e);
            throw new RuntimeException("Error enviando mensaje a WhatsApp", e);
        }
    }

    public byte[] downloadMedia(String mediaId, String encryptedAccessToken) {
        try {
            String accessToken = encryptionService.decryptCredential(encryptedAccessToken);

            // Primero obtenemos la URL del media
            String mediaInfoUrl = String.format("%s/%s", WHATSAPP_API_BASE_URL, mediaId);
            HttpHeaders headers = createHeaders(accessToken);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> mediaInfoResponse = restTemplate.exchange(
                    URI.create(mediaInfoUrl),
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            String mediaUrl = (String) mediaInfoResponse.getBody().get("url");

            // Ahora descargamos el archivo
            ResponseEntity<byte[]> mediaResponse = restTemplate.exchange(
                    URI.create(mediaUrl),
                    HttpMethod.GET,
                    entity,
                    byte[].class
            );

            return mediaResponse.getBody();

        } catch (Exception e) {
            logger.error("Error descargando media de WhatsApp: {}", e.getMessage(), e);
            throw new RuntimeException("Error descargando media de WhatsApp", e);
        }
    }

    private HttpHeaders createHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        return headers;
    }
}
