# ☁️ Azure Kubernetes (KubeAdm) Deployment Guide
**Project**: FreeLink Platform
**Platform**: Azure for Students (Ubuntu 22.04 LTS)

---

## 🛠️ Step 1: Provision the Azure VM
1.  **Create Resource**: Virtual Machine.
2.  **Image**: Ubuntu Server 22.04 LTS.
3.  **Size**: Standard_B2s (2 vCPUs, 4GB RAM) - *Min requirements for K8s*.
4.  **Networking**: Open the following ports in the Security Group:
    *   `22` (SSH)
    *   `6443` (K8s API)
    *   `30000-32767` (NodePorts for Frontend/Gateway/Keycloak/Monitoring)

---

## 🐋 Step 2: Install Container Runtime (Docker/Containerd)
Run these on the VM:
```bash
# Update system
sudo apt-get update && sudo apt-get upgrade -y

# Install Containerd
sudo apt-get install -y containerd
sudo mkdir -p /etc/containerd
containerd config default | sudo tee /etc/containerd/config.toml
sudo sed -i 's/SystemdCgroup = false/SystemdCgroup = true/g' /etc/containerd/config.toml
sudo systemctl restart containerd
```

---

## ☸️ Step 3: Install Kubeadm, Kubelet & Kubectl
```bash
sudo apt-get install -y apt-transport-https ca-certificates curl
curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.28/deb/Release.key | sudo gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg
echo 'deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.28/deb/ /' | sudo tee /etc/apt/sources.list.d/kubernetes.list

sudo apt-get update
sudo apt-get install -y kubelet kubeadm kubectl
sudo apt-mark hold kubelet kubeadm kubectl
```

---

## 🏗️ Step 4: Initialize the Cluster
```bash
# Initialize Master Node
sudo kubeadm init --pod-network-cidr=10.244.0.0/16

# Configure Kubectl for the 'azureuser'
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config

# Install Network Plugin (Flannel)
kubectl apply -f https://github.com/flannel-io/flannel/releases/latest/download/kube-flannel.yml
```

---

## 🔐 Step 5: Registry Secrets (Docker Hub)
To pull your private images, run:
```bash
kubectl create secret docker-registry regcred \
  --docker-server=https://index.docker.io/v1/ \
  --docker-username=YOUR_DOCKER_USER \
  --docker-password=YOUR_DOCKER_PASSWORD \
  --namespace=freelink
```

---

## 🚀 Step 6: Deploying the Application
1.  **Clone the Repo**: `git clone [YOUR_REPO_URL]`
2.  **Apply Manifests**:
    ```bash
    kubectl apply -f k8s/namespace.yml
    kubectl apply -f k8s/  # This deploys all 15 services + Monitoring
    ```
3.  **Verify**:
    ```bash
    kubectl get pods -n freelink
    kubectl get svc -n freelink
    ```

---

## 📊 Summary of NodePorts
*   **Frontend**: `30080`
*   **Keycloak**: `30090`
*   **API Gateway**: `30082`
*   **Prometheus**: `30001`
*   **Grafana**: `30002`

---
**Guide prepared for FreeLink DevOps Team.** 🚀🏆
