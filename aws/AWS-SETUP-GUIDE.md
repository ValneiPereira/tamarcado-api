# Guia de Deploy AWS - Tá Marcado! API

---

## Arquitetura: AWS Free Tier ($0/mês por 12 meses)

EC2 (somente API) + RDS PostgreSQL (free tier) + Upstash Redis (grátis sempre).

```
┌──────────────────────────────────────────────────────┐
│              EC2 t3.micro (FREE TIER)                 │
│         2 vCPU | 1GB RAM | 750h/mês grátis           │
│                                                       │
│  ┌──────────────────────────────────────────────┐    │
│  │  Docker: tamarcado-api (Java 21)              │    │
│  │  ~700MB RAM disponível (só API, sem PG)       │    │
│  │  port 8080                                    │    │
│  └──────────────┬───────────────────────────────┘    │
│                 │   1GB Swap (segurança)              │
└─────────────────┼────────────────────────────────────┘
                  │
        ┌─────────┴─────────┐
        │                   │
        ▼                   ▼
┌───────────────┐   ┌──────────────────┐
│ RDS PostgreSQL│   │  Upstash Redis   │
│ (FREE TIER)   │   │  (Grátis sempre) │
│ db.t3.micro   │   └──────────────────┘
│ 20GB storage  │
└───────────────┘
```

### Custos (Free Tier - 12 meses)

| Serviço | Free Tier | Custo/mês |
|---------|-----------|-----------|
| **EC2 t3.micro** | 750h/mês | **$0** |
| **RDS PostgreSQL** | 750h/mês, 20GB, db.t3.micro | **$0** |
| **EBS** | 30GB gp2 | **$0** |
| **Upstash Redis** | 10K cmd/dia (grátis sempre) | **$0** |
| **Transferência** | 100GB/mês | **$0** |
| **Total** | | **$0/mês** |

### Distribuição de Memória (1GB)

| Componente | RAM | Observação |
|------------|-----|------------|
| Sistema + Docker | ~150MB | Amazon Linux 2023 |
| Java API | ~700MB | MaxRAMPercentage=70%, G1GC |
| Swap (disco) | 1GB | Segurança contra OOM |

---

## Opções de Deploy por Custo

| Opção | Custo | Quando usar |
|-------|-------|-------------|
| **Free Tier (ATUAL)** | $0/mês | Portfólio e estudos (12 meses) |
| EC2 t4g.micro + PG Docker | ~$7/mês | Após Free Tier expirar |
| ECS Fargate + RDS + ALB | ~$90/mês | Produção com clientes pagando |

---

## Dicas para Manter $0

1. **Configure Budget Alerts**: AWS > Billing > Budgets > Create Budget ($0 ou $1)
2. **Use apenas sa-east-1** (São Paulo)
3. **Sem NAT Gateway, sem ALB, sem ElastiCache**
4. **Pare EC2/RDS quando não usar por muito tempo**
5. **Monitore**: AWS > Billing > Free Tier (veja consumo)
6. **RDS parado reinicia após 7 dias** automaticamente - fique atento!

---

## Pré-requisitos

- Conta AWS com Free Tier ativo (https://aws.amazon.com)
- AWS CLI instalado no seu PC
- Conta Upstash (https://upstash.com) - Redis gratuito

### Instalar AWS CLI (PowerShell)

```powershell
# Instalar via winget
winget install Amazon.AWSCLI

# Configurar credenciais
aws configure
# AWS Access Key ID: <sua-access-key>
# AWS Secret Access Key: <sua-secret-key>
# Default region name: sa-east-1
# Default output format: json
```

---

# PASSO A PASSO DO DEPLOY

---

## Passo 1: Criar Conta Upstash (Redis Gratuito)

1. Acesse https://console.upstash.com
2. Crie conta com GitHub
3. **Create Database**:
   - Name: `tamarcado-redis`
   - Region: `South America (São Paulo)`
4. Copie a URL: `rediss://default:xxx@xxx.upstash.io:6379`

---

## Passo 2: Configurar Alerta de Custos (PRIMEIRO!)

1. Vá para **Billing** > **Budgets** > **Create budget**
2. Escolha **Zero spend budget** (alerta se gastar qualquer coisa)
3. Configure email para alertas
4. Crie também um **Monthly cost budget** de $5 como segurança

---

## Passo 3: Criar Security Groups

### 3.1 Security Group para EC2

1. **EC2** > **Security Groups** > **Create**
2. Name: `tamarcado-ec2-sg`
3. Inbound rules:

| Tipo | Porta | Origem | Descrição |
|------|-------|--------|-----------|
| SSH | 22 | My IP | Acesso SSH |
| HTTP | 80 | 0.0.0.0/0 | Web (futuro Nginx) |
| HTTPS | 443 | 0.0.0.0/0 | Web SSL (futuro) |
| Custom TCP | 8080 | 0.0.0.0/0 | API Spring Boot |

### 3.2 Security Group para RDS

1. Name: `tamarcado-rds-sg`
2. Inbound rules:

| Tipo | Porta | Origem | Descrição |
|------|-------|--------|-----------|
| PostgreSQL | 5432 | tamarcado-ec2-sg | Acesso do EC2 ao banco |

---

## Passo 4: Criar RDS PostgreSQL (Free Tier)

1. Vá para **RDS** > **Create database**
2. Configurações:
   - **Engine:** PostgreSQL
   - **Templates:** Free tier (IMPORTANTE!)
   - **DB instance identifier:** tamarcado-db
   - **Master username:** tamarcado
   - **Master password:** (crie uma senha forte, ANOTE!)
   - **DB instance class:** db.t3.micro
   - **Storage:** 20 GB gp2, desmarque autoscaling
   - **VPC:** Default VPC
   - **Public access:** No
   - **Security group:** tamarcado-rds-sg
   - **Database name:** tamarcado
   - **Backup:** Desmarque automated backups (economiza storage)
3. **Create database**
4. Aguarde ~5 min
5. Anote o **Endpoint:** `tamarcado-db.xxxxx.sa-east-1.rds.amazonaws.com`

---

## Passo 5: Criar EC2 t3.micro (Free Tier)

1. Vá para **EC2** > **Launch instance**
2. Configurações:
   - **Name:** tamarcado-api
   - **AMI:** Amazon Linux 2023 (x86_64) - Free tier eligible
   - **Instance type:** t3.micro (Free tier)
   - **Key pair:** Create new > `tamarcado-key` > Download .pem
   - **Network:** Default VPC
   - **Auto-assign public IP:** Enable
   - **Security group:** tamarcado-ec2-sg
   - **Storage:** 20 GB gp2 (free tier até 30GB)
3. **Launch instance**
4. Anote o **Public IP**

---

## Passo 6: Conectar no EC2

### PowerShell (Windows):
```powershell
# Mover a chave para pasta segura
New-Item -ItemType Directory -Force -Path "$env:USERPROFILE\.ssh"
Move-Item "$env:USERPROFILE\Downloads\tamarcado-key.pem" "$env:USERPROFILE\.ssh\tamarcado-key.pem"

# Conectar via SSH
ssh -i "$env:USERPROFILE\.ssh\tamarcado-key.pem" ec2-user@SEU_IP_PUBLICO
```

### Mac/Linux:
```bash
chmod 400 ~/Downloads/tamarcado-key.pem
mv ~/Downloads/tamarcado-key.pem ~/.ssh/
ssh -i ~/.ssh/tamarcado-key.pem ec2-user@SEU_IP_PUBLICO
```

---

## Passo 7: Instalar Tudo no EC2

> Os comandos abaixo rodam **dentro do EC2** (Linux), não no seu PC.

### Opção A: Script automático
```bash
curl -sL https://raw.githubusercontent.com/ValneiPereira/tamarcado-api/main/aws/setup-ec2.sh | bash
```

### Opção B: Manual
```bash
# Atualizar sistema
sudo dnf update -y

# Instalar Docker e Git
sudo dnf install docker git -y
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker ec2-user

# Instalar Docker Compose
ARCH=$(uname -m)
sudo curl -sL "https://github.com/docker/compose/releases/latest/download/docker-compose-linux-${ARCH}" \
  -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Configurar 1GB de Swap (ESSENCIAL para 1GB RAM!)
sudo dd if=/dev/zero of=/swapfile bs=128M count=8
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile swap swap defaults 0 0' | sudo tee -a /etc/fstab
sudo sysctl vm.swappiness=60

# IMPORTANTE: Sair e reconectar para aplicar grupo docker
exit
```

Reconecte via SSH (mesmo comando do Passo 6).

---

## Passo 8: Clonar Repositório e Configurar

> Dentro do EC2 (Linux):

```bash
# Clonar
git clone https://github.com/ValneiPereira/tamarcado-api.git ~/tamarcado-api
cd ~/tamarcado-api

# Criar arquivo de ambiente a partir do template
cp .env.prod.example .env.prod
nano .env.prod
```

Preencha o `.env.prod` com seus dados reais:
```
DATABASE_HOST=tamarcado-db.xxxxx.sa-east-1.rds.amazonaws.com
DATABASE_PORT=5432
DATABASE_NAME=tamarcado
DATABASE_USERNAME=tamarcado
DATABASE_PASSWORD=SUA_SENHA_DO_RDS

REDIS_URL=rediss://default:SUA_SENHA@SEU_HOST.upstash.io:6379

JWT_SECRET=SUA_CHAVE_JWT_MINIMO_32_CARACTERES_AQUI

CORS_ALLOWED_ORIGINS=*
```

> Gerar JWT secret: `openssl rand -base64 48`

---

## Passo 9: Subir a Aplicação

> Dentro do EC2 (Linux):

```bash
cd ~/tamarcado-api

# Build e start (primeira vez demora mais)
docker-compose -f docker-compose.prod.yml --env-file .env.prod up -d --build

# Acompanhar logs (Java demora ~60-90s para iniciar em 1GB RAM)
docker logs -f tamarcado-api
```

---

## Passo 10: Testar

> Dentro do EC2 (Linux):
```bash
# Health check
curl http://localhost:8080/api/v1/actuator/health

# Ver uso de memória
free -m
docker stats --no-stream
```

> No navegador (PowerShell ou qualquer browser):
```
http://SEU_IP_PUBLICO:8080/api/v1/actuator/health
http://SEU_IP_PUBLICO:8080/api/v1/swagger-ui.html
```

---

## Passo 11: Configurar CI/CD no GitHub

1. **Settings** > **Secrets and variables** > **Actions**
2. Adicione os secrets:

| Secret | Valor | Como obter |
|--------|-------|------------|
| `EC2_HOST` | IP público do EC2 | Console AWS > EC2 |
| `EC2_USERNAME` | `ec2-user` | Padrão Amazon Linux |
| `EC2_SSH_KEY` | Conteúdo do arquivo .pem | Abrir o .pem no notepad |

### Ler conteúdo da chave (PowerShell):
```powershell
Get-Content "$env:USERPROFILE\.ssh\tamarcado-key.pem" | Set-Clipboard
# Agora a chave está no clipboard, cole no GitHub Secret
```

Agora, todo push para `main` faz deploy automático via `ec2-deploy.yml`.

---

## Passo 12: Configurar HTTPS (Opcional, Grátis)

Precisa de um domínio apontando para o IP do EC2.

> Dentro do EC2 (Linux):

```bash
# Instalar Nginx e Certbot
sudo dnf install nginx python3-certbot-nginx -y

# Configurar Nginx como proxy reverso
sudo tee /etc/nginx/conf.d/tamarcado.conf << 'EOF'
server {
    listen 80;
    server_name SEU_DOMINIO.com.br;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
EOF

sudo systemctl start nginx
sudo systemctl enable nginx

# Certificado SSL grátis (Let's Encrypt)
sudo certbot --nginx -d SEU_DOMINIO.com.br
```

---

# COMANDOS ÚTEIS

## No EC2 (Linux)

```bash
# Ver containers rodando
docker ps

# Ver logs da API
docker logs -f tamarcado-api

# Reiniciar
cd ~/tamarcado-api
docker-compose -f docker-compose.prod.yml --env-file .env.prod restart

# Atualizar aplicação (novo deploy manual)
cd ~/tamarcado-api
git pull
docker-compose -f docker-compose.prod.yml --env-file .env.prod up -d --build

# Ver uso de memória
free -m
docker stats --no-stream

# Backup do banco (via RDS)
pg_dump -h ENDPOINT_RDS -U tamarcado -d tamarcado > backup_$(date +%Y%m%d).sql
```

## No seu PC (PowerShell)

```powershell
# Conectar no EC2
ssh -i "$env:USERPROFILE\.ssh\tamarcado-key.pem" ec2-user@SEU_IP_PUBLICO

# Parar EC2 (para economizar)
aws ec2 stop-instances --instance-ids i-xxxxx

# Iniciar EC2
aws ec2 start-instances --instance-ids i-xxxxx

# Parar RDS (CUIDADO: reinicia sozinho após 7 dias!)
aws rds stop-db-instance --db-instance-identifier tamarcado-db

# Iniciar RDS
aws rds start-db-instance --db-instance-identifier tamarcado-db

# Ver custos do mês atual
aws ce get-cost-and-usage `
  --time-period Start=$(Get-Date -Format "yyyy-MM-01"),End=$(Get-Date -Format "yyyy-MM-dd") `
  --granularity MONTHLY `
  --metrics BlendedCost
```

---

# PARAR/ECONOMIZAR

```powershell
# Parar EC2 (PowerShell)
aws ec2 stop-instances --instance-ids i-xxxxx

# Parar RDS (PowerShell)
aws rds stop-db-instance --db-instance-identifier tamarcado-db
```

| Recurso | Rodando | Parado |
|---------|---------|--------|
| EC2 | $0 (free tier) | $0 |
| RDS | $0 (free tier) | $0 (reinicia após 7 dias!) |
| EBS 20GB | $0 (free tier) | $0 (free tier) |
| Upstash | $0 | $0 |

---

# APÓS FREE TIER EXPIRAR (12 meses)

Duas opções:

1. **Manter na AWS (~$7/mês):** EC2 t4g.micro + PostgreSQL no Docker (sem RDS)
2. **Migrar para profissional (~$90/mês):** ECS Fargate + RDS Multi-AZ + ALB

O código já está preparado para ambas as opções!

---

# CHECKLIST FINAL

- [ ] Conta Upstash criada (Redis grátis)
- [ ] Budget alert configurado ($0)
- [ ] Security Groups criados (EC2 + RDS)
- [ ] RDS PostgreSQL criado (free tier)
- [ ] EC2 t3.micro criado (free tier)
- [ ] Docker + Docker Compose instalados
- [ ] Swap de 1GB configurado
- [ ] `.env.prod` configurado com endpoint RDS
- [ ] `docker-compose up` rodando
- [ ] Health check funcionando
- [ ] GitHub Secrets configurados (CI/CD)

---

# PORTFÓLIO

1. Compre um domínio (~$12/ano no Registro.br ou Namecheap)
2. Configure HTTPS com Let's Encrypt (grátis) - Passo 12
3. Aponte o domínio para o IP do EC2
4. Documente o projeto no GitHub
5. Adicione no LinkedIn como projeto pessoal

```
https://api.tamarcado.seudominio.com.br/api/v1/swagger-ui.html
```

---
---

# FUTURO: Upload de Imagens com S3

Quando quiser adicionar upload de fotos de perfil, portfólio, etc.

## Arquitetura com S3

```
App Mobile --> API (EC2) --> S3 Bucket --> CloudFront (CDN)
                 |                              |
                 v                              v
            RDS (salva URL) <------------- URL pública
```

## Custos S3

| Recurso | Free Tier | Observação |
|---------|-----------|------------|
| Armazenamento | 5GB (sempre grátis) | ~2.500 fotos de 2MB |
| PUT/POST | 2.000/mês | Upload de imagens |
| GET | 20.000/mês | Download de imagens |
| Transferência | 100GB/mês (12 meses) | Depois $0.09/GB |

## Criar Bucket S3 (PowerShell)

```powershell
aws s3api create-bucket `
  --bucket tamarcado-images `
  --region sa-east-1 `
  --create-bucket-configuration LocationConstraint=sa-east-1
```

## Dependência (pom.xml)

```xml
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>
    <version>2.25.0</version>
</dependency>
```

## Configuração (application-prod.yml)

```yaml
aws:
  s3:
    bucket: ${AWS_S3_BUCKET:tamarcado-images}
    region: ${AWS_REGION:sa-east-1}
```

## S3Service (exemplo)

```java
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public String uploadImage(MultipartFile file, String folder) {
        String fileName = folder + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

        PutObjectRequest request = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(fileName)
            .contentType(file.getContentType())
            .acl(ObjectCannedACL.PUBLIC_READ)
            .build();

        s3Client.putObject(request, RequestBody.fromInputStream(
            file.getInputStream(), file.getSize()));

        return String.format("https://%s.s3.sa-east-1.amazonaws.com/%s",
            bucketName, fileName);
    }

    public void deleteImage(String imageUrl) {
        String key = imageUrl.substring(imageUrl.lastIndexOf(".com/") + 5);
        s3Client.deleteObject(DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .build());
    }
}
```

## Pastas S3 por Entidade

| Entidade | Pasta S3 |
|----------|----------|
| User (perfil) | `profiles/{userId}/` |
| Professional (perfil) | `professionals/{id}/profile/` |
| Professional (portfólio) | `professionals/{id}/portfolio/` |
| Service | `services/{id}/` |

## Variáveis Adicionais no `.env.prod`

```
AWS_S3_BUCKET=tamarcado-images
AWS_ACCESS_KEY_ID=AKIA...
AWS_SECRET_ACCESS_KEY=...
AWS_REGION=sa-east-1
```

---
---

# SYSTEM DESIGN - Tá Marcado! API

## Visão Geral

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        TÁ MARCADO! - SYSTEM DESIGN                         │
└─────────────────────────────────────────────────────────────────────────────┘

                             ┌─────────────┐
                             │   Clients   │
                             │ Mobile/Web  │
                             └──────┬──────┘
                                    │
                                    ▼
                          ┌─────────────────┐
                          │   CloudFront    │ (CDN - futuro)
                          └────────┬────────┘
                                   │
                ┌──────────────────┼──────────────────┐
                │                  │                   │
                ▼                  ▼                   ▼
       ┌───────────────┐  ┌───────────────┐  ┌───────────────┐
       │  Static Files │  │   API REST    │  │    Images     │
       │  (Frontend)   │  │   (Backend)   │  │   (S3)        │
       │   Vercel/S3   │  │   EC2/ECS     │  │               │
       └───────────────┘  └───────┬───────┘  └───────────────┘
                                  │
                ┌─────────────────┼─────────────────┐
                │                 │                  │
                ▼                 ▼                  ▼
       ┌───────────────┐  ┌───────────────┐  ┌───────────────┐
       │  PostgreSQL   │  │    Redis      │  │   External    │
       │  (RDS)        │  │  (Upstash)    │  │   Nominatim   │
       └───────────────┘  └───────────────┘  │   ViaCEP      │
                                             └───────────────┘
```

## Arquitetura Hexagonal

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          ADAPTER LAYER (IN)                              │
│  ┌──────────┐ ┌──────────┐ ┌──────────────┐ ┌─────────────┐           │
│  │   Auth   │ │   User   │ │ Professional │ │ Appointment │ ...       │
│  │Controller│ │Controller│ │  Controller  │ │ Controller  │           │
│  └────┬─────┘ └────┬─────┘ └──────┬───────┘ └──────┬──────┘           │
└───────┼─────────────┼──────────────┼────────────────┼──────────────────┘
        ▼             ▼              ▼                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        APPLICATION LAYER                                 │
│  ┌──────────┐ ┌──────────┐ ┌──────────────┐ ┌─────────────┐           │
│  │   Auth   │ │   User   │ │ Professional │ │ Appointment │ ...       │
│  │ Service  │ │ Service  │ │   Service    │ │   Service   │           │
│  └────┬─────┘ └────┬─────┘ └──────┬───────┘ └──────┬──────┘           │
│       └─────────────┴──────┬──────┴─────────────────┘                   │
│                            ▼                                             │
│                     ┌──────────┐                                         │
│                     │  Ports   │ (Interfaces)                           │
│                     └────┬─────┘                                         │
└──────────────────────────┼───────────────────────────────────────────────┘
                           ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                          DOMAIN LAYER                                     │
│  ┌──────────┐ ┌──────────────┐ ┌─────────────┐ ┌──────────┐           │
│  │   User   │ │ Professional │ │ Appointment │ │  Review  │ ...       │
│  │  Entity  │ │    Entity    │ │   Entity    │ │  Entity  │           │
│  └──────────┘ └──────────────┘ └─────────────┘ └──────────┘           │
└──────────────────────────────────────────────────────────────────────────┘
                           ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                       ADAPTER LAYER (OUT)                                 │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐                    │
│  │     JPA      │ │    Redis     │ │  Geocoding   │                    │
│  │  Repository  │ │    Cache     │ │   Client     │                    │
│  └──────┬───────┘ └──────┬───────┘ └──────┬───────┘                    │
└─────────┼────────────────┼────────────────┼────────────────────────────┘
          ▼                ▼                ▼
    ┌───────────┐    ┌───────────┐    ┌───────────┐
    │ PostgreSQL│    │   Redis   │    │ Nominatim │
    │   (RDS)   │    │ (Upstash) │    │  ViaCEP   │
    └───────────┘    └───────────┘    └───────────┘
```

## Modelo de Dados (ERD)

```
┌──────────────────┐       ┌──────────────────┐       ┌──────────────────┐
│      users       │       │   professionals  │       │     services     │
├──────────────────┤       ├──────────────────┤       ├──────────────────┤
│ id (PK)          │       │ id (PK)          │       │ id (PK)          │
│ email            │◄──────│ user_id (FK)     │──────►│ professional_id  │
│ password_hash    │  1:1  │ bio              │  1:N  │ name             │
│ name             │       │ specialties      │       │ description      │
│ phone            │       │ rating           │       │ price            │
│ role             │       │ total_reviews    │       │ duration_minutes │
│ active           │       │ verified         │       │ active           │
│ created_at       │       │ created_at       │       │ created_at       │
└────────┬─────────┘       └────────┬─────────┘       └──────────────────┘
         │                          │
         │ 1:1                      │ 1:N
         ▼                          ▼
┌──────────────────┐       ┌──────────────────┐       ┌──────────────────┐
│    addresses     │       │   appointments   │       │     reviews      │
├──────────────────┤       ├──────────────────┤       ├──────────────────┤
│ id (PK)          │       │ id (PK)          │       │ id (PK)          │
│ user_id (FK)     │       │ client_id (FK)   │◄──────│ appointment_id   │
│ street           │       │ professional_id  │  1:1  │ client_id (FK)   │
│ number           │       │ service_id (FK)  │       │ professional_id  │
│ complement       │       │ date_time        │       │ rating           │
│ neighborhood     │       │ status           │       │ comment          │
│ city             │       │ notes            │       │ created_at       │
│ state            │       │ created_at       │       └──────────────────┘
│ zip_code         │       └──────────────────┘
│ latitude         │
│ longitude        │       ┌──────────────────┐
└──────────────────┘       │  notifications   │
                           ├──────────────────┤
                           │ id (PK)          │
                           │ user_id (FK)     │
                           │ title            │
                           │ message          │
                           │ type             │
                           │ read             │
                           │ created_at       │
                           └──────────────────┘
```

## Fluxo de Autenticação

```
    Client                    API                     Database
      │                        │                          │
      │  POST /auth/login      │                          │
      │  {email, password}     │                          │
      │───────────────────────►│                          │
      │                        │  SELECT user BY email    │
      │                        │─────────────────────────►│
      │                        │◄─────────────────────────│
      │                        │                          │
      │                        │  Verify BCrypt password  │
      │                        │  Generate JWT token      │
      │                        │                          │
      │  {accessToken,         │                          │
      │   refreshToken,        │                          │
      │   expiresIn}           │                          │
      │◄───────────────────────│                          │
      │                        │                          │
      │  GET /api/resource     │                          │
      │  Header: Bearer <JWT>  │                          │
      │───────────────────────►│                          │
      │                        │  Validate JWT            │
      │                        │  Extract user claims     │
      │                        │─────────────────────────►│
      │                        │◄─────────────────────────│
      │  {resource data}       │                          │
      │◄───────────────────────│                          │
```

## Fluxo de Agendamento

```
  Client App           API              Cache           Database
     │                  │                 │                 │
     │ Search services  │                 │                 │
     │─────────────────►│ Check cache     │                 │
     │                  │────────────────►│                 │
     │                  │ Cache miss      │                 │
     │                  │◄────────────────│                 │
     │                  │ Query DB ───────────────────────►│
     │                  │◄────────────────────────────────│
     │                  │ Store in cache ►│                 │
     │  Services list   │                 │                 │
     │◄─────────────────│                 │                 │
     │                  │                 │                 │
     │ Book appointment │                 │                 │
     │─────────────────►│ Create ─────────────────────────►│
     │                  │ Invalidate ────►│                 │
     │  Confirmation    │                 │                 │
     │◄─────────────────│                 │                 │
```

## Estratégia de Cache

| Cache Key | TTL | Invalidação |
|-----------|-----|-------------|
| `geocoding:coords:{address}` | 30 dias | Nunca |
| `geocoding:address:{cep}` | 30 dias | Nunca |
| `search:services:{query}:{page}` | 1 hora | On service create/update |
| `professional:detail:{id}` | 30 min | On profile update |
| `professional:dashboard:{id}` | 5 min | On new appointment/review |
| `client:dashboard:{id}` | 5 min | On new appointment |
| `services:professional:{id}` | 30 min | On service CRUD |

**Pattern:** Cache-Aside (check cache > miss > query DB > store in cache > return)

## Segurança

| Camada | Mecanismos |
|--------|------------|
| **Rede** | VPC, Security Groups, HTTPS (TLS 1.2+) |
| **Aplicação** | JWT (HMAC-SHA256), BCrypt, Rate Limiting, CORS, Bean Validation |
| **Dados** | SSL para RDS, TLS para Redis, Secrets em .env.prod (nunca no Git) |

## Escalabilidade (Futuro)

```
Current (Free Tier)              Future (Paid)

  EC2 t3.micro ──► RDS            Route 53 > CloudFront > ALB
    (1 inst)                        │
                                    ├── ECS Fargate Task 1
                                    ├── ECS Fargate Task 2
                                    └── ECS Fargate Task N
                                          │
                                    ├── RDS Primary (Multi-AZ)
                                    ├── RDS Replica (Read)
                                    └── ElastiCache Redis
```

## Tech Stack

| Categoria | Tecnologias |
|-----------|-------------|
| **Backend** | Java 21, Spring Boot 3.2, Spring Security, Spring Data JPA, Flyway, MapStruct, Lombok |
| **Banco** | PostgreSQL 16 (RDS), Redis 7 (Upstash) |
| **Infra** | AWS EC2, RDS, S3 (futuro), GitHub Actions |
| **Segurança** | JWT (JJWT), BCrypt, HTTPS/TLS, CORS |
| **Testes** | JUnit 5, Mockito, Testcontainers, Rest Assured |
| **Docs** | OpenAPI 3.0, Swagger UI |
| **Frontend** | React Native, Expo 51, Redux Toolkit, TypeScript |
