package com.tamarcado;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Carrega dados de teste a partir de arquivos JSON.
 * Padrão: arquivos em {@code test-data/}, objetos indexados por {@code key}.
 * <p>
 * Exemplo: {@code loadRegisterClientRequest("joaoSilva")} retorna o objeto
 * sob a chave "joaoSilva" em {@code test-data/auth/register-client.json}.
 */
public final class TestDataLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private TestDataLoader() {}

    /**
     * Carrega um objeto JSON por chave a partir do arquivo informado.
     *
     * @param filePath caminho no classpath (ex: "test-data/auth/register-client.json")
     * @param key      chave do objeto desejado (ex: "joaoSilva")
     * @return mapa com os campos do objeto (incluindo aninhados)
     * @throws IllegalArgumentException se o arquivo ou a chave não existir
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> load(String filePath, String key) {
        Map<String, Object> root = readJson(filePath);
        if (!root.containsKey(key)) {
            throw new IllegalArgumentException("Chave '%s' não encontrada em %s. Chaves disponíveis: %s"
                    .formatted(key, filePath, root.keySet()));
        }
        Object value = root.get(key);
        if (!(value instanceof Map)) {
            throw new IllegalArgumentException("Valor da chave '%s' em %s não é um objeto JSON.".formatted(key, filePath));
        }
        return (Map<String, Object>) value;
    }

    /**
     * Carrega o JSON completo como mapa (todas as chaves).
     */
    public static Map<String, Object> loadAll(String filePath) {
        return readJson(filePath);
    }

    private static Map<String, Object> readJson(String filePath) {
        try (InputStream is = TestDataLoader.class.getClassLoader().getResourceAsStream(filePath)) {
            if (is == null) {
                throw new IllegalArgumentException("Arquivo não encontrado no classpath: " + filePath);
            }
            return MAPPER.readValue(is, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Erro ao ler " + filePath + ": " + e.getMessage(), e);
        }
    }

    // --- Auth ---

    public static final String AUTH_REGISTER_CLIENT = "test-data/auth/register-client.json";
    public static final String AUTH_REGISTER_PROFESSIONAL = "test-data/auth/register-professional.json";
    public static final String AUTH_LOGIN = "test-data/auth/login.json";
    public static final String CLIENTS_VALID = "test-data/clients/valid-clients.json";

    public static Map<String, Object> loadRegisterClientRequest(String key) {
        return load(AUTH_REGISTER_CLIENT, key);
    }

    public static Map<String, Object> loadRegisterProfessionalRequest(String key) {
        return load(AUTH_REGISTER_PROFESSIONAL, key);
    }

    public static Map<String, Object> loadLoginRequest(String key) {
        return load(AUTH_LOGIN, key);
    }

    public static Map<String, Object> loadValidClient(String key) {
        return load(CLIENTS_VALID, key);
    }

    // --- Appointments ---

    public static final String APPOINTMENTS_SETUP = "test-data/appointments/setup.json";
    public static final String APPOINTMENTS_CREATE = "test-data/appointments/create-requests.json";

    /** Fixture para setUp: service (name, price), client (name), professional (name). */
    public static Map<String, Object> loadAppointmentSetup(String key) {
        return load(APPOINTMENTS_SETUP, key);
    }

    public static Map<String, Object> loadCreateAppointmentTemplate(String key) {
        return load(APPOINTMENTS_CREATE, key);
    }

    /**
     * Monta o body de CreateAppointmentRequest a partir do template JSON,
     * injetando professionalId, serviceId e data computada (now + dateOffsetDays).
     */
    public static Map<String, Object> buildCreateAppointmentRequest(
            UUID professionalId, UUID serviceId, String templateKey) {
        Map<String, Object> template = load(APPOINTMENTS_CREATE, templateKey);
        int offset = ((Number) template.getOrDefault("dateOffsetDays", 1)).intValue();
        Map<String, Object> body = new HashMap<>(template);
        body.remove("dateOffsetDays");
        body.put("professionalId", professionalId.toString());
        body.put("serviceId", serviceId.toString());
        body.put("date", LocalDate.now().plusDays(offset).toString());
        return body;
    }
}
