package com.tamarcado.domain.model.service;

public enum ServiceType {
    // BELEZA
    CABELEIREIRO(Category.BELEZA),
    BARBEIRO(Category.BELEZA),
    ESTETICISTA(Category.BELEZA),
    DESIGNER_SOBRANCELHA(Category.BELEZA),
    MANICURE(Category.BELEZA),
    
    // SAUDE
    PSICOLOGO(Category.SAUDE),
    FISIOTERAPEUTA(Category.SAUDE),
    NUTRICIONISTA(Category.SAUDE),
    PERSONAL_TRAINER(Category.SAUDE),
    
    // SERVICOS
    ELETRICISTA(Category.SERVICOS),
    ENCANADOR(Category.SERVICOS),
    TECNICO_INFORMATICA(Category.SERVICOS),
    MONTADOR_MOVEIS(Category.SERVICOS),
    PEDREIRO(Category.SERVICOS),
    DIARISTA(Category.SERVICOS),
    
    // EDUCACAO
    AULA_PARTICULAR(Category.EDUCACAO),
    PROFESSOR_IDIOMAS(Category.EDUCACAO),
    REFORCO_ESCOLAR(Category.EDUCACAO),
    MENTOR(Category.EDUCACAO),
    
    // OUTROS
    OUTROS(Category.OUTROS);
    
    private final Category category;
    
    ServiceType(Category category) {
        this.category = category;
    }
    
    public Category getCategory() {
        return category;
    }
}