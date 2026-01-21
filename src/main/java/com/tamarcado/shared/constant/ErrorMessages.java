package com.tamarcado.shared.constant;

public final class ErrorMessages {
    
    // User
    public static final String USER_NOT_FOUND = "Usuário não encontrado";
    public static final String USER_ALREADY_EXISTS = "Usuário já cadastrado com este email";
    public static final String INVALID_CREDENTIALS = "Email ou senha inválidos";
    
    // Professional
    public static final String PROFESSIONAL_NOT_FOUND = "Profissional não encontrado";
    
    // Service
    public static final String SERVICE_NOT_FOUND = "Serviço não encontrado";
    public static final String SERVICE_IN_USE = "Serviço não pode ser excluído pois possui agendamentos ativos";
    
    // Appointment
    public static final String APPOINTMENT_NOT_FOUND = "Agendamento não encontrado";
    public static final String APPOINTMENT_INVALID_DATE = "Data do agendamento deve ser futura";
    public static final String APPOINTMENT_INVALID_STATUS = "Agendamento não pode ser alterado neste status";
    public static final String APPOINTMENT_NOT_OWNER = "Você não tem permissão para acessar este agendamento";
    
    // Review
    public static final String REVIEW_NOT_FOUND = "Avaliação não encontrada";
    public static final String REVIEW_ALREADY_EXISTS = "Agendamento já foi avaliado";
    public static final String REVIEW_INVALID_APPOINTMENT = "Apenas agendamentos concluídos podem ser avaliados";
    
    // Validation
    public static final String VALIDATION_ERROR = "Erro de validação";
    public static final String INVALID_EMAIL = "Email inválido";
    public static final String INVALID_PHONE = "Telefone inválido";
    public static final String INVALID_CEP = "CEP inválido";
    
    // Security
    public static final String UNAUTHORIZED = "Não autorizado";
    public static final String FORBIDDEN = "Acesso negado";
    public static final String INVALID_TOKEN = "Token inválido ou expirado";
    
    // General
    public static final String INTERNAL_SERVER_ERROR = "Erro interno do servidor";
    public static final String RESOURCE_NOT_FOUND = "Recurso não encontrado";
    
    private ErrorMessages() {
        throw new UnsupportedOperationException("Utility class");
    }
}