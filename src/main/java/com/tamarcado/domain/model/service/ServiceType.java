package com.tamarcado.domain.model.service;

import static com.tamarcado.domain.model.service.Category.*;

public enum ServiceType {
    // BELEZA
    CABELEIREIRO(BELEZA),
    BARBEIRO(BELEZA),
    ESTETICISTA(BELEZA),
    DESIGNER_SOBRANCELHA(BELEZA),
    MANICURE(BELEZA),
    
    // SAUDE
    PSICOLOGO(SAUDE),
    FISIOTERAPEUTA(SAUDE),
    NUTRICIONISTA(SAUDE),
    PERSONAL_TRAINER(SAUDE),
    
    // SERVICOS
    ELETRICISTA(SERVICOS),
    ENCANADOR(SERVICOS),
    TECNICO_INFORMATICA(SERVICOS),
    MONTADOR_MOVEIS(SERVICOS),
    PEDREIRO(SERVICOS),
    DIARISTA(SERVICOS),
    
    // EDUCACAO
    AULA_PARTICULAR(EDUCACAO),
    PROFESSOR_IDIOMAS(EDUCACAO),
    REFORCO_ESCOLAR(EDUCACAO),
    MENTOR(EDUCACAO),
    
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