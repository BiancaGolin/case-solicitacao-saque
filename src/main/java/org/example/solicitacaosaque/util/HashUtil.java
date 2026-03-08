package org.example.solicitacaosaque.util;

import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class HashUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String gerarHash(Object payload) {

        try {
            String json = objectMapper.writeValueAsString(payload);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(json.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
