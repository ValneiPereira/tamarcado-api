# Guia de Deploy AWS - Tá Marcado! API

Este guia explica passo a passo como fazer deploy da API na AWS.

---

## AWS Free Tier - O que é Grátis (Primeiros 12 meses)

| Serviço | Free Tier | Suficiente? |
|---------|-----------|-------------|
| **EC2** | 750h/mês t2.micro ou t3.micro | ✅ Sim |
| **RDS PostgreSQL** | 750h/mês db.t3.micro, 20GB | ✅ Sim |
| **S3** | 5GB armazenamento | ✅ Sim |
| **ECR** | 500MB/mês | ✅ Sim |
| **CloudWatch** | Métricas básicas | ✅ Sim |
| **Secrets Manager** | 30 dias grátis, depois $0.40/secret | ⚠️ Parcial |
| **ECS Fargate** | ❌ NÃO TEM FREE TIER | ❌ Pago |
| **ALB** | ❌ NÃO TEM FREE TIER | ❌ Pago |
| **NAT Gateway** | ❌ NÃO TEM FREE TIER | ❌ Pago |
| **ElastiCache** | ❌ NÃO TEM FREE TIER | ❌ Pago |

---

## Opções de Deploy por Custo

### Opção 1: Arquitetura Profissional (ECS Fargate) - ~$90/mês
Para quando você tiver budget ou clientes pagando.

| Serviço | Custo |
|---------|-------|
| ECS Fargate | ~$15 |
| RDS PostgreSQL | ~$15 |
| ALB | ~$20 |
| NAT Gateway | ~$35 |
| Upstash Redis | $0 |
| Outros | ~$5 |
| **Total** | **~$90/mês** |

### Opção 2: Arquitetura Econômica (EC2) - ~$5-15/mês
Ideal para estudos e portfólio.

| Serviço | Custo |
|---------|-------|
| EC2 t3.micro | $0 (free tier) ou ~$8 |
| RDS db.t3.micro | $0 (free tier) ou ~$15 |
| Upstash Redis | $0 |
| Elastic IP | $0 (se associado) |
| **Total** | **$0-15/mês** |

### Opção 3: 100% Grátis (Free Tier + Serviços Externos)
Para aprender sem gastar nada nos primeiros 12 meses.

| Serviço | Custo |
|---------|-------|
| EC2 t3.micro | $0 (750h free) |
| RDS db.t3.micro | $0 (750h free) |
| Upstash Redis | $0 |
| Domínio | Opcional |
| **Total** | **$0/mês** |

---

## Recomendação para Estudos/Portfólio

**Use a Opção 3 (100% Grátis)** pelos primeiros 12 meses:
- EC2 com Docker (em vez de ECS Fargate)
- RDS PostgreSQL free tier
- Upstash Redis (externo, grátis)
- Acesso direto via IP público (sem ALB)

Quando tiver clientes/trabalho, migre para a Opção 1 (ECS Fargate).

---

## Dicas para Não Gastar Dinheiro

1. **Configure Budget Alerts**: AWS > Billing > Budgets > Create Budget ($1)
2. **Desligue quando não usar**: Pare EC2/RDS à noite
3. **Use apenas uma região**: sa-east-1 (São Paulo)
4. **Evite NAT Gateway**: Use subnet pública para EC2
5. **Evite ALB**: Acesse direto pelo IP do EC2
6. **Monitore o Free Tier**: AWS > Billing > Free Tier

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

# ALTERNATIVA GRATUITA: Deploy com EC2 (Free Tier)

Esta seção ensina como fazer deploy **100% grátis** usando EC2 em vez de ECS Fargate.

## Arquitetura Gratuita

```
Internet ──▶ EC2 t3.micro (Docker) ──▶ RDS PostgreSQL
                    │
                    └──▶ Upstash Redis (externo)
```

**Custo: $0/mês** (nos primeiros 12 meses do Free Tier)

---

## Passo 1: Criar Conta Upstash (Redis Gratuito)

1. Acesse https://console.upstash.com
2. Crie conta com GitHub
3. **Create Database**:
   - Name: `tamarcado-redis`
   - Region: `South America (São Paulo)`
4. Copie a URL: `rediss://default:xxx@xxx.upstash.io:6379`

---

## Passo 2: Criar Conta AWS

1. Acesse https://aws.amazon.com
2. **Create an AWS Account**
3. Use um email novo (para garantir Free Tier)
4. Adicione cartão de crédito (não será cobrado se ficar no free tier)

---

## Passo 3: Configurar Alerta de Custos (IMPORTANTE!)

**Faça isso PRIMEIRO para evitar surpresas:**

1. Vá para **Billing** > **Budgets** > **Create budget**
2. Escolha **Zero spend budget**
3. Configure email para alertas
4. Agora você será avisado se algo sair do free tier

---

## Passo 4: Criar VPC (Rede)

1. Vá para **VPC** > **Create VPC**
2. Escolha **VPC and more**
3. Configurações:
   - Name: `tamarcado`
   - IPv4 CIDR: `10.0.0.0/16`
   - Availability Zones: `1`
   - Public subnets: `1`
   - Private subnets: `1`
   - NAT gateways: `None` (custa dinheiro!)
   - VPC endpoints: `None`
4. **Create VPC**

---

## Passo 5: Criar Security Groups

### 5.1 Security Group para EC2
1. **VPC** > **Security Groups** > **Create**
2. Name: `tamarcado-ec2-sg`
3. Inbound rules:
   - SSH (22) → My IP
   - HTTP (80) → 0.0.0.0/0
   - HTTPS (443) → 0.0.0.0/0
   - Custom TCP (8080) → 0.0.0.0/0

### 5.2 Security Group para RDS
1. Name: `tamarcado-rds-sg`
2. Inbound rules:
   - PostgreSQL (5432) → tamarcado-ec2-sg

---

## Passo 6: Criar RDS PostgreSQL (Free Tier)

1. Vá para **RDS** > **Create database**
2. Configurações:
   - **Engine:** PostgreSQL
   - **Template:** Free tier ✅
   - **DB instance identifier:** tamarcado-db
   - **Master username:** tamarcado
   - **Master password:** (crie uma senha forte, ANOTE!)
   - **DB instance class:** db.t3.micro (free tier)
   - **Storage:** 20 GB gp2
   - **VPC:** tamarcado-vpc
   - **Subnet group:** Create new
   - **Public access:** No
   - **Security group:** tamarcado-rds-sg
   - **Database name:** tamarcado
3. **Create database**
4. Aguarde ~5 minutos
5. Anote o **Endpoint**: `tamarcado-db.xxxxx.sa-east-1.rds.amazonaws.com`

---

## Passo 7: Criar EC2 (Free Tier)

1. Vá para **EC2** > **Launch instance**
2. Configurações:
   - **Name:** tamarcado-api
   - **AMI:** Amazon Linux 2023 (Free tier eligible)
   - **Instance type:** t3.micro (free tier) ou t2.micro
   - **Key pair:** Create new → `tamarcado-key` → Download .pem
   - **Network:** tamarcado-vpc
   - **Subnet:** Public subnet
   - **Auto-assign public IP:** Enable
   - **Security group:** tamarcado-ec2-sg
   - **Storage:** 8 GB gp3 (free tier)
3. **Launch instance**
4. Anote o **Public IP**

---

## Passo 8: Conectar no EC2

### Windows (PowerShell):
```powershell
# Mover a chave para pasta segura
mkdir ~\.ssh -Force
mv ~/Downloads/tamarcado-key.pem ~/.ssh/

# Conectar
ssh -i ~/.ssh/tamarcado-key.pem ec2-user@SEU_IP_PUBLICO
```

### Mac/Linux:
```bash
chmod 400 ~/Downloads/tamarcado-key.pem
ssh -i ~/Downloads/tamarcado-key.pem ec2-user@SEU_IP_PUBLICO
```

---

## Passo 9: Instalar Docker no EC2

Execute estes comandos no EC2:

```bash
# Atualizar sistema
sudo dnf update -y

# Instalar Docker
sudo dnf install docker -y
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker ec2-user

# Instalar Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Sair e entrar novamente para aplicar grupo docker
exit
```

Reconecte via SSH.

---

## Passo 10: Configurar Variáveis de Ambiente

```bash
# Criar arquivo de ambiente
cat > ~/.env << 'EOF'
SPRING_PROFILES_ACTIVE=prod
DATABASE_HOST=tamarcado-db.xxxxx.sa-east-1.rds.amazonaws.com
DATABASE_PORT=5432
DATABASE_NAME=tamarcado
DATABASE_USERNAME=tamarcado
DATABASE_PASSWORD=SUA_SENHA_DO_RDS
REDIS_URL=rediss://default:xxx@xxx.upstash.io:6379
JWT_SECRET=sua-chave-jwt-super-secreta-minimo-256-bits-1234567890abcdef
CORS_ALLOWED_ORIGINS=*
EOF

# Proteger arquivo
chmod 600 ~/.env
```

---

## Passo 11: Fazer Deploy Manual

### Opção A: Build direto no EC2 (mais simples)

```bash
# Clonar repositório
git clone https://github.com/SEU_USUARIO/tamarcado-api.git
cd tamarcado-api

# Build da imagem
docker build -t tamarcado-api .

# Rodar container
docker run -d \
  --name tamarcado-api \
  --env-file ~/.env \
  -p 8080:8080 \
  --restart unless-stopped \
  tamarcado-api
```

### Opção B: Usar imagem do Docker Hub

```bash
# Fazer login no Docker Hub (se for imagem privada)
docker login

# Pull e run
docker run -d \
  --name tamarcado-api \
  --env-file ~/.env \
  -p 8080:8080 \
  --restart unless-stopped \
  SEU_USUARIO/tamarcado-api:latest
```

---

## Passo 12: Testar

```bash
# Ver logs
docker logs -f tamarcado-api

# Testar health check
curl http://localhost:8080/api/v1/actuator/health

# Testar de fora (use o IP público)
# No seu navegador: http://SEU_IP_PUBLICO:8080/api/v1/actuator/health
```

---

## Passo 13: Configurar HTTPS (Opcional, Grátis)

Para ter HTTPS grátis usando Let's Encrypt:

```bash
# Instalar Nginx e Certbot
sudo dnf install nginx -y
sudo dnf install python3-certbot-nginx -y

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

# Iniciar Nginx
sudo systemctl start nginx
sudo systemctl enable nginx

# Obter certificado SSL (precisa de domínio apontando para o IP)
sudo certbot --nginx -d SEU_DOMINIO.com.br
```

---

## Comandos Úteis EC2

```bash
# Ver status do container
docker ps

# Ver logs
docker logs -f tamarcado-api

# Reiniciar container
docker restart tamarcado-api

# Atualizar aplicação
cd ~/tamarcado-api
git pull
docker build -t tamarcado-api .
docker stop tamarcado-api
docker rm tamarcado-api
docker run -d --name tamarcado-api --env-file ~/.env -p 8080:8080 --restart unless-stopped tamarcado-api

# Parar tudo (para não gastar se sair do free tier)
docker stop tamarcado-api
```

---

## Monitorar Custos

```bash
# Via AWS CLI
aws ce get-cost-and-usage \
  --time-period Start=2024-01-01,End=2024-01-31 \
  --granularity MONTHLY \
  --metrics BlendedCost
```

Ou acesse: **AWS Console** > **Billing** > **Bills**

---

## Desligar Tudo (Custo Zero)

Se quiser parar de usar temporariamente:

```bash
# Parar EC2 (no console AWS ou CLI)
aws ec2 stop-instances --instance-ids i-xxxxx

# Parar RDS
aws rds stop-db-instance --db-instance-identifier tamarcado-db
```

**Importante:**
- EC2 parado = $0
- RDS parado = $0 (mas reinicia automaticamente após 7 dias!)
- EBS (disco) continua cobrando ~$0.80/mês

---

## Migrar para ECS Fargate Depois

Quando tiver budget ou clientes, basta:
1. Seguir os passos 7-13 da primeira parte deste guia
2. O código já está pronto para ECS
3. Apontar o domínio para o ALB

---

## Checklist Final - Deploy Gratuito

- [ ] Conta Upstash criada (Redis grátis)
- [ ] Conta AWS criada
- [ ] Budget alert configurado ($0)
- [ ] VPC criada (sem NAT Gateway!)
- [ ] Security Groups criados
- [ ] RDS PostgreSQL criado (free tier)
- [ ] EC2 criado (free tier)
- [ ] Docker instalado
- [ ] Aplicação rodando
- [ ] Health check funcionando

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
