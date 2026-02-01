# Guia de Deploy AWS - Tá Marcado! API

Este guia explica passo a passo como fazer deploy da API na AWS.

---

## Arquitetura Atual: AWS Free Tier ($0/mês)

EC2 (somente API) + RDS PostgreSQL (free tier) + Upstash Redis (grátis).

```
┌──────────────────────────────────────────────────────┐
│              EC2 t3.micro (FREE TIER)                 │
│         2 vCPU | 1GB RAM | 750h/mês grátis           │
│                                                       │
│  ┌──────────────────────────────────────────────┐    │
│  │  Docker:                                      │    │
│  │  tamarcado-api (Java 21)                      │    │
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

### Distribuição de Memória (1GB) - Mais Folga!

| Componente | RAM | Observação |
|------------|-----|------------|
| Sistema + Docker | ~150MB | Amazon Linux 2023 |
| Java API | ~700MB | MaxRAMPercentage=70%, G1GC |
| Swap (disco) | 1GB | Segurança contra OOM |

**Vantagem vs versão anterior:** Sem PostgreSQL local, a API Java tem ~700MB em vez de ~400MB.

---

## Opções de Deploy por Custo

### Opção 1 (ATUAL): Free Tier - $0/mês
EC2 + RDS grátis por 12 meses. Portfólio e estudos.

### Opção 2: Pós Free Tier - EC2 t4g.micro - ~$7/mês
API + PostgreSQL no mesmo EC2 via Docker Compose (sem RDS).

### Opção 3: ECS Fargate + RDS - ~$90/mês
Arquitetura profissional. Quando tiver clientes pagando.

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

- Conta AWS (criar em https://aws.amazon.com)
- AWS CLI instalado
- Conta Upstash (https://upstash.com) - Redis gratuito

---

## Passo 1: Criar Conta Upstash (Redis Gratuito)

1. Acesse https://console.upstash.com
2. Crie uma conta (pode usar GitHub)
3. Clique em **Create Database**
4. Escolha:
   - **Name:** tamarcado-redis
   - **Region:** South America (São Paulo)
   - **Type:** Regional
5. Copie a **UPSTASH_REDIS_REST_URL** (formato: `rediss://default:xxx@xxx.upstash.io:6379`)

---

## Passo 2: Criar Conta AWS

1. Acesse https://aws.amazon.com
2. Clique em **Create an AWS Account**
3. Complete o cadastro (precisa de cartão de crédito)
4. Escolha o plano **Free Tier**

---

## Passo 3: Configurar AWS CLI

```bash
# Instalar AWS CLI (Windows)
winget install Amazon.AWSCLI

# Configurar credenciais
aws configure
# AWS Access Key ID: <sua-access-key>
# AWS Secret Access Key: <sua-secret-key>
# Default region name: sa-east-1
# Default output format: json
```

---

## Passo 4: Criar VPC e Subnets

```bash
# Criar VPC
aws ec2 create-vpc \
  --cidr-block 10.0.0.0/16 \
  --tag-specifications 'ResourceType=vpc,Tags=[{Key=Name,Value=tamarcado-vpc}]'

# Anotar o VPC_ID retornado
```

Ou use o Console AWS:
1. Vá para **VPC** > **Your VPCs** > **Create VPC**
2. Escolha **VPC and more** para criar tudo automaticamente
3. Nomeie como `tamarcado`

---

## Passo 5: Criar RDS PostgreSQL

### Via Console:
1. Vá para **RDS** > **Create database**
2. Configurações:
   - **Engine:** PostgreSQL 16
   - **Template:** Free tier
   - **DB instance identifier:** tamarcado-db
   - **Master username:** tamarcado
   - **Master password:** (crie uma senha forte)
   - **DB instance class:** db.t3.micro
   - **Storage:** 20 GB
   - **VPC:** tamarcado-vpc
   - **Public access:** No
3. Clique **Create database**

### Anotar o Endpoint:
```
tamarcado-db.xxxxx.sa-east-1.rds.amazonaws.com
```

---

## Passo 6: Criar Secrets no Secrets Manager

```bash
# Secret do banco de dados
aws secretsmanager create-secret \
  --name tamarcado/prod/database \
  --secret-string '{
    "host": "tamarcado-db.xxxxx.sa-east-1.rds.amazonaws.com",
    "dbname": "tamarcado",
    "username": "tamarcado",
    "password": "SUA_SENHA_AQUI"
  }'

# Secret do Redis (Upstash)
aws secretsmanager create-secret \
  --name tamarcado/prod/redis \
  --secret-string '{
    "url": "rediss://default:xxx@xxx.upstash.io:6379"
  }'

# Secret do JWT
aws secretsmanager create-secret \
  --name tamarcado/prod/jwt \
  --secret-string '{
    "secret": "sua-chave-jwt-minimo-256-bits-muito-segura-aqui-12345678901234567890"
  }'

# Secret do App
aws secretsmanager create-secret \
  --name tamarcado/prod/app \
  --secret-string '{
    "cors_origins": "https://app.tamarcado.com.br"
  }'
```

---

## Passo 7: Criar ECR Repository

```bash
aws ecr create-repository \
  --repository-name tamarcado-api \
  --image-scanning-configuration scanOnPush=true
```

Anotar o URI:
```
123456789012.dkr.ecr.sa-east-1.amazonaws.com/tamarcado-api
```

---

## Passo 8: Criar ECS Cluster

```bash
aws ecs create-cluster \
  --cluster-name tamarcado-cluster \
  --capacity-providers FARGATE FARGATE_SPOT \
  --default-capacity-provider-strategy capacityProvider=FARGATE,weight=1
```

---

## Passo 9: Criar IAM Roles

### 9.1 ECS Task Execution Role

```bash
# Criar role
aws iam create-role \
  --role-name ecsTaskExecutionRole \
  --assume-role-policy-document '{
    "Version": "2012-10-17",
    "Statement": [{
      "Effect": "Allow",
      "Principal": {"Service": "ecs-tasks.amazonaws.com"},
      "Action": "sts:AssumeRole"
    }]
  }'

# Anexar políticas
aws iam attach-role-policy \
  --role-name ecsTaskExecutionRole \
  --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy

# Permitir acesso ao Secrets Manager
aws iam put-role-policy \
  --role-name ecsTaskExecutionRole \
  --policy-name SecretsManagerAccess \
  --policy-document '{
    "Version": "2012-10-17",
    "Statement": [{
      "Effect": "Allow",
      "Action": [
        "secretsmanager:GetSecretValue"
      ],
      "Resource": "arn:aws:secretsmanager:sa-east-1:*:secret:tamarcado/*"
    }]
  }'
```

---

## Passo 10: Criar Application Load Balancer

### Via Console:
1. Vá para **EC2** > **Load Balancers** > **Create Load Balancer**
2. Escolha **Application Load Balancer**
3. Configurações:
   - **Name:** tamarcado-alb
   - **Scheme:** Internet-facing
   - **VPC:** tamarcado-vpc
   - **Subnets:** Selecione as subnets públicas
4. **Listeners:**
   - HTTP:80 → Redirect to HTTPS:443
   - HTTPS:443 → Target Group
5. Crie um **Target Group:**
   - **Name:** tamarcado-tg
   - **Target type:** IP
   - **Protocol:** HTTP
   - **Port:** 8080
   - **Health check:** /api/v1/actuator/health

---

## Passo 11: Criar ECS Service

### 11.1 Registrar Task Definition

Primeiro, edite o arquivo `aws/task-definition.json`:
- Substitua `${AWS_ACCOUNT_ID}` pelo seu ID de conta AWS
- Substitua `${AWS_REGION}` por `sa-east-1`

```bash
aws ecs register-task-definition \
  --cli-input-json file://aws/task-definition.json
```

### 11.2 Criar Service

```bash
aws ecs create-service \
  --cluster tamarcado-cluster \
  --service-name tamarcado-api-service \
  --task-definition tamarcado-api \
  --desired-count 1 \
  --launch-type FARGATE \
  --network-configuration '{
    "awsvpcConfiguration": {
      "subnets": ["subnet-xxx", "subnet-yyy"],
      "securityGroups": ["sg-xxx"],
      "assignPublicIp": "DISABLED"
    }
  }' \
  --load-balancers '[{
    "targetGroupArn": "arn:aws:elasticloadbalancing:sa-east-1:xxx:targetgroup/tamarcado-tg/xxx",
    "containerName": "tamarcado-api",
    "containerPort": 8080
  }]'
```

---

## Passo 12: Configurar GitHub Actions

Adicione estes secrets no seu repositório GitHub:

1. Vá para **Settings** > **Secrets and variables** > **Actions**
2. Adicione os seguintes secrets:

| Secret | Valor |
|--------|-------|
| `AWS_ACCESS_KEY_ID` | Sua access key do IAM |
| `AWS_SECRET_ACCESS_KEY` | Sua secret key do IAM |
| `AWS_ACCOUNT_ID` | Seu ID de conta (12 dígitos) |
| `ECR_REPOSITORY` | tamarcado-api |
| `ECS_CLUSTER` | tamarcado-cluster |
| `ECS_SERVICE` | tamarcado-api-service |

3. Adicione a variável:

| Variable | Valor |
|----------|-------|
| `AWS_REGION` | sa-east-1 |

---

## Passo 13: Fazer Deploy

```bash
# Commit e push para main
git add .
git commit -m "feat: add AWS deployment configuration"
git push origin main
```

O GitHub Actions vai:
1. Rodar os testes
2. Buildar a imagem Docker
3. Fazer push para ECR
4. Atualizar o ECS Service

---

## Verificar Deploy

```bash
# Ver status do service
aws ecs describe-services \
  --cluster tamarcado-cluster \
  --services tamarcado-api-service

# Ver logs
aws logs tail /ecs/tamarcado-api --follow
```

---

## Troubleshooting

### API não inicia
```bash
# Ver eventos do ECS
aws ecs describe-services --cluster tamarcado-cluster --services tamarcado-api-service

# Ver logs do container
aws logs get-log-events --log-group-name /ecs/tamarcado-api --log-stream-name <stream>
```

### Erro de conexão com RDS
- Verifique se o Security Group do RDS permite conexão do Security Group do ECS
- Verifique se estão na mesma VPC

### Erro de Secrets
- Verifique se a role tem permissão para acessar o Secrets Manager
- Verifique se os nomes dos secrets estão corretos

---

## Custos Adicionais Opcionais

| Serviço | Uso | Custo |
|---------|-----|-------|
| Route 53 | Domínio próprio | ~$0.50/mês |
| ACM | Certificado SSL | Grátis |
| CloudWatch Alarms | Monitoramento | ~$0.10/alarme |
| WAF | Firewall | ~$5/mês |

---

## Comandos Úteis

```bash
# Atualizar service (force new deployment)
aws ecs update-service \
  --cluster tamarcado-cluster \
  --service tamarcado-api-service \
  --force-new-deployment

# Escalar para 2 instâncias
aws ecs update-service \
  --cluster tamarcado-cluster \
  --service tamarcado-api-service \
  --desired-count 2

# Parar service (custo zero)
aws ecs update-service \
  --cluster tamarcado-cluster \
  --service tamarcado-api-service \
  --desired-count 0
```

---
---

# DEPLOY: AWS Free Tier ($0/mês)

EC2 t3.micro (API) + RDS PostgreSQL + Upstash Redis. Tudo grátis por 12 meses.

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
   - **Templates:** Free tier ✅ (IMPORTANTE!)
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
   - **Instance type:** t3.micro ✅ (Free tier)
   - **Key pair:** Create new → `tamarcado-key` → Download .pem
   - **Network:** Default VPC
   - **Auto-assign public IP:** Enable
   - **Security group:** tamarcado-ec2-sg
   - **Storage:** 20 GB gp2 (free tier até 30GB)
3. **Launch instance**
4. Anote o **Public IP**

---

## Passo 6: Conectar no EC2

### Windows (PowerShell):
```powershell
mkdir ~\.ssh -Force
mv ~/Downloads/tamarcado-key.pem ~/.ssh/
ssh -i ~/.ssh/tamarcado-key.pem ec2-user@SEU_IP_PUBLICO
```

### Mac/Linux:
```bash
chmod 400 ~/Downloads/tamarcado-key.pem
ssh -i ~/Downloads/tamarcado-key.pem ec2-user@SEU_IP_PUBLICO
```

---

## Passo 7: Instalar Tudo com o Script de Setup

```bash
# Opção 1: Download e executar o script
curl -sL https://raw.githubusercontent.com/ValneiPereira/tamarcado-api/main/aws/setup-ec2.sh | bash
```

Ou manualmente:
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
sudo curl -sL "https://github.com/docker/compose/releases/latest/download/docker-compose-linux-${ARCH}" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Configurar 1GB de Swap (ESSENCIAL para 1GB RAM!)
sudo dd if=/dev/zero of=/swapfile bs=128M count=8
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile swap swap defaults 0 0' | sudo tee -a /etc/fstab
sudo sysctl vm.swappiness=60

# Sair e reconectar para aplicar grupo docker
exit
```

Reconecte via SSH.

---

## Passo 8: Clonar Repositório e Configurar

```bash
# Clonar
git clone https://github.com/ValneiPereira/tamarcado-api.git ~/tamarcado-api
cd ~/tamarcado-api

# Criar arquivo de ambiente
cp .env.prod.example .env.prod
nano .env.prod
```

Preencha o `.env.prod` com os dados do RDS:
```bash
# RDS endpoint (do passo 4)
DATABASE_HOST=tamarcado-db.xxxxx.sa-east-1.rds.amazonaws.com
DATABASE_PORT=5432
DATABASE_NAME=tamarcado
DATABASE_USERNAME=tamarcado
DATABASE_PASSWORD=SUA_SENHA_DO_RDS

# Upstash (do passo 1)
REDIS_URL=rediss://default:SUA_SENHA@SEU_HOST.upstash.io:6379

# JWT (gere com: openssl rand -base64 48)
JWT_SECRET=SUA_CHAVE_JWT_MINIMO_32_CARACTERES_AQUI

# CORS
CORS_ALLOWED_ORIGINS=*
```

---

## Passo 9: Subir a Aplicação

```bash
cd ~/tamarcado-api

# Build e start (primeira vez demora mais)
docker-compose -f docker-compose.prod.yml --env-file .env.prod up -d --build

# Acompanhar logs (Java demora ~60-90s para iniciar em 1GB RAM)
docker logs -f tamarcado-api
```

---

## Passo 10: Testar

```bash
# Health check local
curl http://localhost:8080/api/v1/actuator/health

# No navegador (use o IP público):
# http://SEU_IP_PUBLICO:8080/api/v1/actuator/health
# http://SEU_IP_PUBLICO:8080/api/v1/swagger-ui.html

# Ver uso de memória
free -m
docker stats --no-stream
```

---

## Passo 11: Configurar HTTPS (Opcional, Grátis)

Precisa de um domínio apontando para o IP do EC2.

```bash
# Instalar Nginx e Certbot
sudo dnf install nginx python3-certbot-nginx -y

# Configurar Nginx como proxy
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

# Certificado SSL grátis
sudo certbot --nginx -d SEU_DOMINIO.com.br
```

---

## Passo 12: Configurar CI/CD no GitHub

Adicione estes secrets no repositório GitHub:

1. **Settings** > **Secrets and variables** > **Actions**

| Secret | Valor |
|--------|-------|
| `EC2_HOST` | IP público do EC2 |
| `EC2_USERNAME` | `ec2-user` |
| `EC2_SSH_KEY` | Conteúdo do arquivo .pem |

Agora, todo push para `main` faz deploy automático!

---

## Comandos Úteis

```bash
# Ver containers rodando
docker ps

# Ver logs da API
docker logs -f tamarcado-api

# Ver logs do PostgreSQL
docker logs -f tamarcado-postgres

# Reiniciar tudo
cd ~/tamarcado-api
docker-compose -f docker-compose.prod.yml --env-file .env.prod restart

# Atualizar aplicação manualmente
cd ~/tamarcado-api
git pull
docker-compose -f docker-compose.prod.yml --env-file .env.prod up -d --build

# Ver uso de memória
free -m
docker stats --no-stream

# Backup do banco
docker exec tamarcado-postgres pg_dump -U tamarcado tamarcado > backup_$(date +%Y%m%d).sql
```

---

## Parar Tudo (Economizar)

```bash
# Parar containers (EC2 continua rodando)
cd ~/tamarcado-api
docker-compose -f docker-compose.prod.yml --env-file .env.prod down

# Parar EC2 inteiro
aws ec2 stop-instances --instance-ids i-xxxxx

# Parar RDS (CUIDADO: reinicia sozinho após 7 dias!)
aws rds stop-db-instance --db-instance-identifier tamarcado-db
```

**Custos quando parado (Free Tier):**
- EC2 parado: $0
- RDS parado: $0 (mas reinicia após 7 dias!)
- EBS: $0 (free tier até 30GB)
- Upstash: $0

---

## Monitorar Custos

Via Console: **AWS** > **Billing** > **Bills**

```bash
# Via CLI
aws ce get-cost-and-usage \
  --time-period Start=$(date +%Y-%m-01),End=$(date +%Y-%m-%d) \
  --granularity MONTHLY \
  --metrics BlendedCost
```

---

## Após Free Tier Expirar (12 meses)

Duas opções:
1. **Manter na AWS (~$7/mês):** Use EC2 t4g.micro + PostgreSQL no Docker (sem RDS)
2. **Migrar para profissional (~$90/mês):** ECS Fargate + RDS Multi-AZ + ALB

O código já está preparado para ambas as opções!

---

## Checklist Final

- [ ] Conta Upstash criada (Redis grátis)
- [ ] Budget alert configurado ($0)
- [ ] Security Groups criados (EC2 + RDS)
- [ ] RDS PostgreSQL criado (free tier)
- [ ] EC2 t3.micro criado (free tier)
- [ ] Docker + Docker Compose instalados
- [ ] Swap de 1GB configurado
- [ ] .env.prod configurado com endpoint RDS
- [ ] `docker-compose up` rodando
- [ ] Health check funcionando
- [ ] GitHub Secrets configurados (CI/CD)

---

## Próximos Passos para Portfólio

1. **Compre um domínio** (~$12/ano no Registro.br ou Namecheap)
2. **Configure HTTPS** com Let's Encrypt (grátis)
3. **Aponte o domínio** para o IP do EC2
4. **Documente o projeto** no GitHub
5. **Adicione no LinkedIn** como projeto pessoal

Exemplo de URL para portfólio:
```
https://api.tamarcado.seudominio.com.br/api/v1/swagger-ui.html
```

---
---

# PRÓXIMOS PASSOS: Upload de Imagens com S3

Quando quiser adicionar upload de fotos de perfil, portfólio, etc.

## Arquitetura com S3

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Upload de Imagem                             │
│                                                                      │
│  App Mobile ──▶ API (EC2) ──▶ S3 Bucket ──▶ CloudFront (CDN)        │
│       │              │              │              │                 │
│       │              │              │              ▼                 │
│       │              │              │         URL pública            │
│       │              │              │              │                 │
│       │              ▼              │              │                 │
│       │         RDS (salva URL) ◀──┴──────────────┘                 │
│       │                                                              │
│       └─────────── Exibe imagem via URL do S3/CloudFront            │
└─────────────────────────────────────────────────────────────────────┘
```

## Custos S3 (Free Tier - Sempre Grátis)

| Recurso | Free Tier | Observação |
|---------|-----------|------------|
| Armazenamento | 5GB | ~2.500 fotos de 2MB |
| PUT/POST | 2.000/mês | Upload de imagens |
| GET | 20.000/mês | Download de imagens |
| Transferência | 100GB/mês (12 meses) | Depois $0.09/GB |

## Passo 1: Criar Bucket S3

```bash
# Via AWS CLI
aws s3api create-bucket \
  --bucket tamarcado-images \
  --region sa-east-1 \
  --create-bucket-configuration LocationConstraint=sa-east-1

# Configurar para acesso público de leitura (imagens)
aws s3api put-public-access-block \
  --bucket tamarcado-images \
  --public-access-block-configuration \
  "BlockPublicAcls=false,IgnorePublicAcls=false,BlockPublicPolicy=false,RestrictPublicBuckets=false"
```

## Passo 2: Adicionar Dependência no pom.xml

```xml
<!-- AWS SDK for S3 -->
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>
    <version>2.25.0</version>
</dependency>
```

## Passo 3: Configurar application-prod.yml

```yaml
aws:
  s3:
    bucket: ${AWS_S3_BUCKET:tamarcado-images}
    region: ${AWS_REGION:sa-east-1}
  credentials:
    access-key: ${AWS_ACCESS_KEY_ID}
    secret-key: ${AWS_SECRET_ACCESS_KEY}
```

## Passo 4: Criar S3Service

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

## Passo 5: Endpoint de Upload

```java
@PostMapping("/upload/profile-image")
public ResponseEntity<Map<String, String>> uploadProfileImage(
        @RequestParam("file") MultipartFile file,
        @AuthenticationPrincipal UserDetails user) {

    String imageUrl = s3Service.uploadImage(file, "profiles/" + user.getUsername());

    // Atualizar URL no banco
    userService.updateProfileImage(user.getUsername(), imageUrl);

    return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
}
```

## Casos de Uso no Tá Marcado

| Entidade | Pasta S3 | Exemplo |
|----------|----------|---------|
| User (perfil) | `profiles/{userId}/` | Foto de perfil do cliente |
| Professional (perfil) | `professionals/{id}/profile/` | Foto do profissional |
| Professional (portfólio) | `professionals/{id}/portfolio/` | Trabalhos realizados |
| Service | `services/{id}/` | Imagens do serviço |

## Variáveis de Ambiente Adicionais

```bash
# Adicionar ao ~/.env no EC2
AWS_S3_BUCKET=tamarcado-images
AWS_ACCESS_KEY_ID=AKIA...
AWS_SECRET_ACCESS_KEY=...
AWS_REGION=sa-east-1
```

---
---

# SYSTEM DESIGN - Tá Marcado! API

Visão completa da arquitetura do sistema.

## Visão Geral

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                            TÁ MARCADO! - SYSTEM DESIGN                           │
└─────────────────────────────────────────────────────────────────────────────────┘

                                 ┌─────────────┐
                                 │   Clients   │
                                 │ Mobile/Web  │
                                 └──────┬──────┘
                                        │
                                        ▼
                              ┌─────────────────┐
                              │   CloudFront    │ (CDN - futuro)
                              │   (opcional)    │
                              └────────┬────────┘
                                       │
                    ┌──────────────────┼──────────────────┐
                    │                  │                  │
                    ▼                  ▼                  ▼
           ┌───────────────┐  ┌───────────────┐  ┌───────────────┐
           │  Static Files │  │   API REST    │  │    Images     │
           │  (Frontend)   │  │   (Backend)   │  │   (S3)        │
           │   Vercel/S3   │  │   EC2/ECS     │  │               │
           └───────────────┘  └───────┬───────┘  └───────────────┘
                                      │
                    ┌─────────────────┼─────────────────┐
                    │                 │                 │
                    ▼                 ▼                 ▼
           ┌───────────────┐  ┌───────────────┐  ┌───────────────┐
           │   PostgreSQL  │  │    Redis      │  │   External    │
           │   (RDS)       │  │  (Upstash)    │  │   Services    │
           │               │  │   Cache       │  │  Nominatim    │
           └───────────────┘  └───────────────┘  │  ViaCEP       │
                                                 └───────────────┘
```

## Arquitetura de Camadas (Hexagonal)

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              ADAPTER LAYER (IN)                                  │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐               │
│  │    Auth     │ │    User     │ │Professional │ │ Appointment │ ...           │
│  │ Controller  │ │ Controller  │ │ Controller  │ │ Controller  │               │
│  └──────┬──────┘ └──────┬──────┘ └──────┬──────┘ └──────┬──────┘               │
└─────────┼───────────────┼───────────────┼───────────────┼───────────────────────┘
          │               │               │               │
          ▼               ▼               ▼               ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                            APPLICATION LAYER                                     │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐               │
│  │    Auth     │ │    User     │ │Professional │ │ Appointment │ ...           │
│  │   Service   │ │   Service   │ │   Service   │ │   Service   │               │
│  └──────┬──────┘ └──────┬──────┘ └──────┬──────┘ └──────┬──────┘               │
│         │               │               │               │                        │
│         └───────────────┴───────┬───────┴───────────────┘                        │
│                                 │                                                │
│                          ┌──────▼──────┐                                         │
│                          │    Ports    │ (Interfaces)                            │
│                          │   (out)     │                                         │
│                          └──────┬──────┘                                         │
└─────────────────────────────────┼────────────────────────────────────────────────┘
                                  │
┌─────────────────────────────────┼────────────────────────────────────────────────┐
│                          DOMAIN LAYER                                            │
│                                 │                                                │
│  ┌─────────────┐ ┌─────────────┐│┌─────────────┐ ┌─────────────┐                │
│  │    User     │ │Professional │││ Appointment │ │   Review    │ ...            │
│  │   Entity    │ │   Entity    │││   Entity    │ │   Entity    │                │
│  └─────────────┘ └─────────────┘│└─────────────┘ └─────────────┘                │
│                                 │                                                │
└─────────────────────────────────┼────────────────────────────────────────────────┘
                                  │
┌─────────────────────────────────┼────────────────────────────────────────────────┐
│                         ADAPTER LAYER (OUT)                                      │
│                                 │                                                │
│  ┌─────────────┐ ┌─────────────┴─────────────┐ ┌─────────────┐                  │
│  │    JPA      │ │        Redis              │ │  Geocoding  │                  │
│  │ Repository  │ │        Cache              │ │   Client    │                  │
│  └──────┬──────┘ └───────────┬───────────────┘ └──────┬──────┘                  │
└─────────┼────────────────────┼────────────────────────┼──────────────────────────┘
          │                    │                        │
          ▼                    ▼                        ▼
    ┌───────────┐        ┌───────────┐           ┌───────────┐
    │ PostgreSQL│        │   Redis   │           │ Nominatim │
    │   (RDS)   │        │ (Upstash) │           │  ViaCEP   │
    └───────────┘        └───────────┘           └───────────┘
```

## Modelo de Dados (ERD)

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           ENTITY RELATIONSHIP DIAGRAM                            │
└─────────────────────────────────────────────────────────────────────────────────┘

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
┌─────────────────────────────────────────────────────────────────────────────────┐
│                            AUTHENTICATION FLOW                                   │
└─────────────────────────────────────────────────────────────────────────────────┘

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
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           APPOINTMENT BOOKING FLOW                               │
└─────────────────────────────────────────────────────────────────────────────────┘

  Client App           API              Cache           Database        Professional
     │                  │                 │                 │                 │
     │ Search services  │                 │                 │                 │
     │─────────────────►│                 │                 │                 │
     │                  │ Check cache     │                 │                 │
     │                  │────────────────►│                 │                 │
     │                  │ Cache miss      │                 │                 │
     │                  │◄────────────────│                 │                 │
     │                  │ Query DB        │                 │                 │
     │                  │────────────────────────────────►│                 │
     │                  │◄────────────────────────────────│                 │
     │                  │ Store in cache  │                 │                 │
     │                  │────────────────►│                 │                 │
     │  Services list   │                 │                 │                 │
     │◄─────────────────│                 │                 │                 │
     │                  │                 │                 │                 │
     │ Get availability │                 │                 │                 │
     │─────────────────►│                 │                 │                 │
     │                  │ Query appointments               │                 │
     │                  │────────────────────────────────►│                 │
     │  Available slots │                 │                 │                 │
     │◄─────────────────│                 │                 │                 │
     │                  │                 │                 │                 │
     │ Book appointment │                 │                 │                 │
     │─────────────────►│                 │                 │                 │
     │                  │ Create appointment               │                 │
     │                  │────────────────────────────────►│                 │
     │                  │ Invalidate cache│                 │                 │
     │                  │────────────────►│                 │                 │
     │                  │                 │                 │   Notification  │
     │                  │─────────────────────────────────────────────────►│
     │  Confirmation    │                 │                 │                 │
     │◄─────────────────│                 │                 │                 │
```

## Estratégia de Cache

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              CACHING STRATEGY                                    │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│  Cache Key Pattern              │  TTL        │  Invalidation                   │
├─────────────────────────────────┼─────────────┼─────────────────────────────────┤
│  geocoding:coords:{address}     │  30 days    │  Never (addresses don't change) │
│  geocoding:address:{cep}        │  30 days    │  Never                          │
│  search:services:{query}:{page} │  1 hour     │  On service create/update       │
│  professional:detail:{id}       │  30 min     │  On profile update              │
│  professional:dashboard:{id}    │  5 min      │  On new appointment/review      │
│  client:dashboard:{id}          │  5 min      │  On new appointment             │
│  services:professional:{id}     │  30 min     │  On service CRUD                │
└─────────────────────────────────┴─────────────┴─────────────────────────────────┘

                           Cache-Aside Pattern

     ┌─────────────────────────────────────────────────────────┐
     │                        Request                          │
     └────────────────────────────┬────────────────────────────┘
                                  │
                                  ▼
                    ┌─────────────────────────┐
                    │     Check Redis Cache   │
                    └────────────┬────────────┘
                                 │
                    ┌────────────┴────────────┐
                    │                         │
              Cache HIT                  Cache MISS
                    │                         │
                    ▼                         ▼
           ┌───────────────┐      ┌────────────────────┐
           │ Return cached │      │ Query PostgreSQL   │
           │     data      │      └─────────┬──────────┘
           └───────────────┘                │
                                            ▼
                                 ┌────────────────────┐
                                 │ Store in Redis     │
                                 │ with TTL           │
                                 └─────────┬──────────┘
                                           │
                                           ▼
                                 ┌────────────────────┐
                                 │ Return fresh data  │
                                 └────────────────────┘
```

## Segurança

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              SECURITY LAYERS                                     │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│ Layer 1: Network Security                                                        │
│ ┌─────────────────────────────────────────────────────────────────────────────┐ │
│ │  • VPC with private subnets for RDS                                         │ │
│ │  • Security Groups (firewall rules)                                         │ │
│ │  • HTTPS only (TLS 1.2+)                                                    │ │
│ └─────────────────────────────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────────────────────────┤
│ Layer 2: Application Security                                                    │
│ ┌─────────────────────────────────────────────────────────────────────────────┐ │
│ │  • JWT Authentication (HMAC-SHA256)                                         │ │
│ │  • BCrypt password hashing (strength 10)                                    │ │
│ │  • Rate limiting (auth: 5/min, search: 60/min, general: 100/min)           │ │
│ │  • CORS configuration                                                       │ │
│ │  • Input validation (Bean Validation)                                       │ │
│ └─────────────────────────────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────────────────────────┤
│ Layer 3: Data Security                                                           │
│ ┌─────────────────────────────────────────────────────────────────────────────┐ │
│ │  • Encrypted connections to RDS (SSL)                                       │ │
│ │  • Encrypted connections to Redis (TLS)                                     │ │
│ │  • Secrets in AWS Secrets Manager (prod) or env vars (dev)                 │ │
│ │  • No sensitive data in logs                                                │ │
│ └─────────────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────────┘
```

## Escalabilidade (Futuro)

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           SCALING ARCHITECTURE                                   │
└─────────────────────────────────────────────────────────────────────────────────┘

                              Current (Free Tier)

                                    │
                                    ▼
                            ┌───────────────┐
                            │  EC2 t3.micro │
                            │   (1 inst)    │
                            └───────────────┘
                                    │
                                    ▼
                                   Scale
                                    │
                                    ▼
                              Future (Paid)

                           ┌───────────────┐
                           │     Route 53  │
                           │      (DNS)    │
                           └───────┬───────┘
                                   │
                           ┌───────▼───────┐
                           │  CloudFront   │
                           │    (CDN)      │
                           └───────┬───────┘
                                   │
                           ┌───────▼───────┐
                           │      ALB      │
                           │ (Load Balancer)│
                           └───────┬───────┘
                                   │
              ┌────────────────────┼────────────────────┐
              │                    │                    │
      ┌───────▼───────┐    ┌───────▼───────┐    ┌───────▼───────┐
      │  ECS Fargate  │    │  ECS Fargate  │    │  ECS Fargate  │
      │   Task 1      │    │   Task 2      │    │   Task N      │
      └───────────────┘    └───────────────┘    └───────────────┘
              │                    │                    │
              └────────────────────┼────────────────────┘
                                   │
              ┌────────────────────┼────────────────────┐
              │                    │                    │
      ┌───────▼───────┐    ┌───────▼───────┐    ┌───────▼───────┐
      │ RDS Primary   │    │ RDS Replica   │    │ ElastiCache   │
      │ (Multi-AZ)    │    │ (Read Only)   │    │    Redis      │
      └───────────────┘    └───────────────┘    └───────────────┘
```

## Monitoramento

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           MONITORING & OBSERVABILITY                             │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│                                                                                  │
│    Logs (CloudWatch)           Metrics (Actuator)         Alerts               │
│    ┌─────────────────┐         ┌─────────────────┐        ┌─────────────────┐  │
│    │ Application logs│         │ /actuator/health│        │ Budget > $0     │  │
│    │ Access logs     │         │ /actuator/metrics│       │ CPU > 80%       │  │
│    │ Error logs      │         │ /actuator/info  │        │ Memory > 80%    │  │
│    └─────────────────┘         └─────────────────┘        │ 5xx errors > 10 │  │
│                                                            └─────────────────┘  │
│                                                                                  │
│    Health Checks               Tracing (Future)                                 │
│    ┌─────────────────┐         ┌─────────────────┐                             │
│    │ /health         │         │ AWS X-Ray       │                             │
│    │ /health/liveness│         │ Request tracing │                             │
│    │ /health/readiness│        │ Performance     │                             │
│    └─────────────────┘         └─────────────────┘                             │
│                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘
```

## Tech Stack Resumo

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              TECH STACK                                          │
├──────────────────────────────────────────────────────────────────────────────────┤
│                                                                                  │
│  Backend                        Infrastructure              External Services   │
│  ───────                        ──────────────              ─────────────────   │
│  • Java 21                      • AWS EC2/ECS               • Upstash Redis     │
│  • Spring Boot 3.2              • AWS RDS PostgreSQL        • Nominatim API     │
│  • Spring Security              • AWS S3 (futuro)           • ViaCEP API        │
│  • Spring Data JPA              • AWS ECR                                       │
│  • Hibernate                    • GitHub Actions                                │
│  • Flyway                                                                       │
│  • MapStruct                    Security                                        │
│  • Lombok                       ────────                                        │
│                                 • JWT (JJWT)                                    │
│  Database                       • BCrypt                                        │
│  ────────                       • HTTPS/TLS                                     │
│  • PostgreSQL 16                • CORS                                          │
│  • Redis 7                                                                      │
│                                 Documentation                                   │
│  Testing                        ─────────────                                   │
│  ───────                        • OpenAPI 3.0                                   │
│  • JUnit 5                      • Swagger UI                                    │
│  • Mockito                                                                      │
│  • Testcontainers                                                               │
│  • Rest Assured                                                                 │
│                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘
```
