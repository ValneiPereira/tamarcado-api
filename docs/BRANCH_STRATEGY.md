# 🌿 Estratégia de Branches

## 📋 Branches Principais

### `main` / `master`
- Branch de produção
- Sempre estável e testado
- Apenas merges de `develop` após releases

### `develop`
- Branch de desenvolvimento principal
- Sempre estável para testes de integração
- Recebe merges das feature branches

---

## 🌿 Branches de Features/Tasks

### Padrão de Nomenclatura

```
task/{numero-task}-{descricao-curta}
```

### Exemplos:

- `task/be-002-security-jwt`
- `task/be-003-database-modeling`
- `task/be-004-repositories`
- `task/be-005-authentication`

### Regras:

1. **Sempre criar a partir de `develop`**
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b task/be-002-security-jwt
   ```

2. **Commits descritivos**
   ```bash
   git commit -m "feat: implementa JWT token provider"
   git commit -m "fix: corrige validação de token expirado"
   ```

3. **Push frequente**
   ```bash
   git push origin task/be-002-security-jwt
   ```

4. **Criar Pull Request para `develop`**
   - Após task concluída
   - Aguardar code review
   - Merge apenas após aprovação

---

## 🔀 Fluxo de Trabalho

```
main/master (produção)
    ↑
develop (desenvolvimento)
    ↑
task/be-002-security-jwt (feature)
    ↑
task/be-003-database-modeling (feature)
```

---

## 📝 Convenções de Commit

### Formato

```
tipo(escopo): descrição curta

Descrição detalhada (opcional)

[corpo opcional]

[rodapé opcional]
```

### Tipos

- `feat`: Nova funcionalidade
- `fix`: Correção de bug
- `docs`: Documentação
- `style`: Formatação (não afeta código)
- `refactor`: Refatoração
- `test`: Testes
- `chore`: Tarefas de manutenção

### Exemplos

```bash
feat(security): implementa JWT token provider

Adiciona classe JwtTokenProvider com geração e validação
de tokens JWT usando jjwt 0.12.3

fix(auth): corrige validação de token expirado

O token estava sendo considerado válido mesmo após expiração

docs(readme): atualiza instruções de instalação

test(repository): adiciona testes para UserRepository
```

---

## 🚀 Processo de Merge

1. **Terminar a task**
   - Completar todos os itens do checklist
   - Executar testes
   - Atualizar documentação

2. **Criar Pull Request**
   - Base: `develop`
   - Compare: `task/be-xxx-description`
   - Preencher template do PR

3. **Code Review**
   - Aguardar aprovação
   - Incorporar feedback

4. **Merge**
   - Merge apenas após aprovação
   - Deletar branch após merge

---

## 🔍 Branches Temporários

### `hotfix/`
Para correções urgentes em produção:
```bash
git checkout -b hotfix/critical-bug-fix main
```

### `bugfix/`
Para bugs não críticos:
```bash
git checkout -b bugfix/user-validation-error develop
```

---

## 📚 Comandos Úteis

### Criar nova task branch
```bash
git checkout develop
git pull origin develop
git checkout -b task/be-002-security-jwt
```

### Sincronizar com develop
```bash
git checkout task/be-002-security-jwt
git fetch origin
git rebase origin/develop
```

### Ver branches locais
```bash
git branch
```

### Ver branches remotas
```bash
git branch -r
```

### Deletar branch local
```bash
git branch -d task/be-002-security-jwt
```

### Deletar branch remota
```bash
git push origin --delete task/be-002-security-jwt
```

---

**Última atualização:** 2026-01-21