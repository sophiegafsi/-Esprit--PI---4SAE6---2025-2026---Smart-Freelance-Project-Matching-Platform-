#!/bin/sh
# ════════════════════════════════════════════════════════════════
# install-jenkins-wsl.sh  (Alpine / Docker Desktop WSL)
# One-shot setup: Java 17 + Maven + kubectl + kubeadm + Jenkins
# Run as: sh devops/install-jenkins-wsl.sh
# ════════════════════════════════════════════════════════════════

set -e

echo "=== [1/7] Updating Alpine packages ==="
apk update && apk upgrade

# ── Java 17 + Maven + Git ────────────────────────────────────────
echo "=== [2/7] Installing Java 17 + Maven + Git ==="
apk add --no-cache openjdk17 maven git curl wget bash

# Set JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export PATH="$JAVA_HOME/bin:$PATH"
java -version
mvn -v

# ── Docker is already available in Docker Desktop WSL ────────────
echo "=== [3/7] Verifying Docker (already installed by Docker Desktop) ==="
docker version
docker compose version

# ── kubectl ──────────────────────────────────────────────────────
echo "=== [4/7] Installing kubectl ==="
apk add --no-cache kubectl --repository=https://dl-cdn.alpinelinux.org/alpine/edge/community/ || {
  # fallback: download binary directly
  curl -LO "https://dl.k8s.io/release/$(curl -Ls https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
  chmod +x kubectl
  mv kubectl /usr/local/bin/kubectl
}
kubectl version --client

# ── kubeadm + kubelet ────────────────────────────────────────────
echo "=== [5/7] Installing kubeadm + kubelet ==="
apk add --no-cache kubeadm kubelet --repository=https://dl-cdn.alpinelinux.org/alpine/edge/community/ || {
  # fallback: download binaries directly
  K8S_VERSION=$(curl -Ls https://dl.k8s.io/release/stable.txt)
  curl -LO "https://dl.k8s.io/release/${K8S_VERSION}/bin/linux/amd64/kubeadm"
  curl -LO "https://dl.k8s.io/release/${K8S_VERSION}/bin/linux/amd64/kubelet"
  chmod +x kubeadm kubelet
  mv kubeadm kubelet /usr/local/bin/
}

# Disable swap (required by kubeadm)
swapoff -a 2>/dev/null || true

# ── Jenkins as Docker container ──────────────────────────────────
echo "=== [6/7] Starting Jenkins (Docker container) ==="

# Create Jenkins home volume
docker volume create jenkins-home 2>/dev/null || true

# Start Jenkins container (port 8090)
docker rm -f jenkins 2>/dev/null || true
docker run -d \
  --name jenkins \
  --restart unless-stopped \
  -p 8090:8080 \
  -p 50000:50000 \
  -v jenkins-home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v $(which kubectl):/usr/local/bin/kubectl \
  jenkins/jenkins:lts

# ── SonarQube as Docker container ───────────────────────────────
echo "=== [7/7] Starting SonarQube (Docker container) ==="
docker volume create sonarqube-data   2>/dev/null || true
docker volume create sonarqube-logs   2>/dev/null || true
docker volume create sonarqube-extensions 2>/dev/null || true

docker rm -f sonarqube 2>/dev/null || true
docker run -d \
  --name sonarqube \
  --restart unless-stopped \
  -p 9000:9000 \
  -e SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true \
  -v sonarqube-data:/opt/sonarqube/data \
  -v sonarqube-logs:/opt/sonarqube/logs \
  -v sonarqube-extensions:/opt/sonarqube/extensions \
  sonarqube:lts-community

# ── Wait for Jenkins to start and print password ──────────────────
echo ""
echo "Waiting 60s for Jenkins to initialise..."
sleep 60

echo ""
echo "════════════════════════════════════════════════════════════"
echo " ✅ SETUP COMPLETE"
echo "════════════════════════════════════════════════════════════"
echo " Jenkins:    http://localhost:8090"
echo " SonarQube:  http://localhost:9000"
echo ""
echo " Jenkins initial admin password:"
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword 2>/dev/null || \
  echo "  (still starting — run: docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword)"
echo ""
echo " kubeadm init (run as root when ready):"
echo "   kubeadm init --pod-network-cidr=10.244.0.0/16"
echo "════════════════════════════════════════════════════════════"
