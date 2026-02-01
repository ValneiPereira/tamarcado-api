#!/bin/bash
# =============================================================================
# Tá Marcado! API - EC2 Setup Script
# Tested on: Amazon Linux 2023 (AL2023) - ARM64 (t4g.micro)
#
# Usage:
#   ssh -i key.pem ec2-user@IP
#   curl -sL https://raw.githubusercontent.com/ValneiPereira/tamarcado-api/main/aws/setup-ec2.sh | bash
#   OR
#   bash setup-ec2.sh
# =============================================================================

set -e

echo "==========================================="
echo "  Tá Marcado! API - EC2 Setup"
echo "  Instance: t4g.micro (ARM64, 1GB RAM)"
echo "==========================================="

# --- 1. System Update ---
echo ""
echo "[1/6] Updating system..."
sudo dnf update -y -q

# --- 2. Install Docker ---
echo ""
echo "[2/6] Installing Docker..."
sudo dnf install docker git -y -q
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker ec2-user

# --- 3. Install Docker Compose ---
echo ""
echo "[3/6] Installing Docker Compose..."
ARCH=$(uname -m)
sudo curl -sL "https://github.com/docker/compose/releases/latest/download/docker-compose-linux-${ARCH}" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Verify
docker-compose --version

# --- 4. Configure Swap (important for 1GB RAM!) ---
echo ""
echo "[4/6] Configuring 1GB swap..."
if [ ! -f /swapfile ]; then
    sudo dd if=/dev/zero of=/swapfile bs=128M count=8
    sudo chmod 600 /swapfile
    sudo mkswap /swapfile
    sudo swapon /swapfile
    echo '/swapfile swap swap defaults 0 0' | sudo tee -a /etc/fstab
    echo "Swap configured: 1GB"
else
    echo "Swap already exists"
fi

# Optimize swap usage for low memory
sudo sysctl vm.swappiness=60
echo 'vm.swappiness=60' | sudo tee -a /etc/sysctl.conf

# --- 5. Clone Repository ---
echo ""
echo "[5/6] Cloning repository..."
if [ -d ~/tamarcado-api ]; then
    echo "Repository already exists, pulling latest..."
    cd ~/tamarcado-api && git pull
else
    git clone https://github.com/ValneiPereira/tamarcado-api.git ~/tamarcado-api
fi

# --- 6. Setup Environment File ---
echo ""
echo "[6/6] Setting up environment..."
if [ ! -f ~/tamarcado-api/.env.prod ]; then
    cp ~/tamarcado-api/.env.prod.example ~/tamarcado-api/.env.prod
    chmod 600 ~/tamarcado-api/.env.prod
    echo ""
    echo "==========================================="
    echo "  IMPORTANT: Edit .env.prod before starting!"
    echo "  nano ~/tamarcado-api/.env.prod"
    echo "==========================================="
else
    echo ".env.prod already exists"
fi

echo ""
echo "==========================================="
echo "  Setup complete!"
echo ""
echo "  Next steps:"
echo "  1. EXIT and reconnect (for docker group):"
echo "     exit"
echo "     ssh -i key.pem ec2-user@IP"
echo ""
echo "  2. Edit environment variables:"
echo "     nano ~/tamarcado-api/.env.prod"
echo ""
echo "  3. Start the application:"
echo "     cd ~/tamarcado-api"
echo "     docker-compose -f docker-compose.prod.yml --env-file .env.prod up -d --build"
echo ""
echo "  4. Check status:"
echo "     docker ps"
echo "     docker logs -f tamarcado-api"
echo ""
echo "  5. Test health:"
echo "     curl http://localhost:8080/api/v1/actuator/health"
echo "==========================================="
