# SonarCloud + GitHub Actions

O projeto usa **SonarCloud** para análise estática e cobertura de testes, via workflow em `.github/workflows/sonarcloud.yml`.

## O que o workflow faz

- **Triggers:** `push` e `pull_request` nas branches `main`, `master` e `develop`
- **Build:** `mvn verify` (compila, roda testes, gera relatório JaCoCo)
- **SonarCloud:** envia análise e cobertura para o SonarCloud (quando configurado)

O step do SonarCloud só roda se **SONAR_TOKEN**, **SONAR_ORGANIZATION** e **SONAR_PROJECT_KEY** estiverem configurados.

## Configuração inicial

### 1. Criar projeto no SonarCloud

1. Acesse [sonarcloud.io](https://sonarcloud.io) e faça login (ex.: com GitHub).
2. **Add new project** → escolha o repositório `tamarcado-api`.
3. Siga o assistente e anote:
   - **Organization key** (ex.: `minha-org`)
   - **Project key** (ex.: `minha-org_tamarcado-api`).

### 2. Gerar token

- **Plano Free:** [My Account → Security → Generate Tokens](https://sonarcloud.io/account/security/)
- **Team:** [Scoped Organization Tokens](https://docs.sonarsource.com/sonarcloud/advanced-setup/managing-organization-tokens/)

Crie um token com permissão de **Analysis** e copie o valor (não será mostrado de novo).

### 3. Configurar GitHub

No repositório: **Settings → Secrets and variables → Actions**.

**Secrets:**

| Nome          | Valor        | Obrigatório |
|---------------|--------------|-------------|
| `SONAR_TOKEN` | Token do passo 2 | Sim      |

**Variables (Repository variables):**

| Nome                   | Valor              | Obrigatório |
|------------------------|--------------------|-------------|
| `SONAR_ORGANIZATION`   | Organization key   | Sim         |
| `SONAR_PROJECT_KEY`    | Project key        | Sim         |

### 4. Rodar o workflow

Após salvar o secret e as variáveis, faça um `push` ou abra um **Pull Request** para `main`/`develop`. O workflow **Build, Test & SonarCloud** será executado e a análise aparecerá no SonarCloud.

## Rodar SonarCloud localmente

```bash
export SONAR_TOKEN=seu-token
mvn verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.organization=SUA_ORG \
  -Dsonar.projectKey=SUA_ORG_tamarcado-api
```

Substitua `SUA_ORG` e o project key pelos valores do seu projeto no SonarCloud.

## Cobertura (JaCoCo)

- O **JaCoCo** gera cobertura em `mvn verify`.
- Relatório XML: `target/site/jacoco/jacoco.xml`.
- O SonarCloud usa esse XML para exibir cobertura na UI.

## Quality Gate

Você pode [configurar um Quality Gate](https://docs.sonarsource.com/sonarcloud/enriching/quality-gates/) no SonarCloud e exigir que o check **SonarCloud** passe antes de merge (em **Branch protection rules**), para bloquear PRs que falharem na análise.

## "Project not found" / Erro na esteira

Se o workflow falhar com **Project not found**:

1. **Confirme Organization e Project Key** no SonarCloud:  
   Projeto → **Project Information** (canto superior direito).  
   O **Project key** costuma ser `{organization}_{repositório}`.  
   Ex.: se for `ValneiPereira_tamarcado-api`, a **Organization** costuma ser **`ValneiPereira`** (e não `tamarcado`).

2. **Ajuste as variáveis no GitHub:**
   - `SONAR_ORGANIZATION` = **Organization key** exatamente como no SonarCloud.
   - `SONAR_PROJECT_KEY` = **Project key** exatamente como no SonarCloud.

3. **Token:** O `SONAR_TOKEN` precisa ser de um usuário com acesso ao projeto.  
   Gere em [Account → Security → Generate Tokens](https://sonarcloud.io/account/security/) (plano Free) ou use *Scoped Organization Token* (Team).

4. **PR a partir de fork:** Em PRs vindos de **fork**, secrets e variáveis não são passados ao workflow.  
   O SonarCloud só roda em **push** ou em **PR da mesma base** (branch no próprio repositório).  
   Para depurar, veja no log do step "SonarCloud scan" a linha  
   `SonarCloud org=... project=...` e confira se os valores estão corretos.

## Referências

- [SonarCloud – GitHub Actions](https://docs.sonarsource.com/sonarcloud/advanced-setup/ci-based-analysis/github-actions-for-sonarcloud/)
- [SonarScanner for Maven](https://docs.sonarsource.com/sonarcloud/advanced-setup/ci-based-analysis/sonarscanner-for-maven/)
