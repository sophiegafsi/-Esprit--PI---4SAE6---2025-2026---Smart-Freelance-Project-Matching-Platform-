# FreeLink Azure Migration Guide (Kubeadm)

This guide walks you through migrating your FreeLink microservice architecture to an Azure VM-based Kubernetes cluster using your Azure Student subscription. We avoid AKS to stay within budget and rely on pure `kubeadm`.

## 1. Prerequisites
- [Azure CLI](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli) installed locally.
- Authenticated via `az login`.

---

## 2. Provision Azure Resources
Run the provided resource creation script to spin up the network, security group, and virtual machines in the `spaincentral` region.
```bash
chmod +x azure/scripts/create-azure-resources.sh
./azure/scripts/create-azure-resources.sh
```
> **Note**: Take note of the Master IP and Worker IP printed at the end of the script!

---

## 3. Install Kubernetes on VMs
You need to run the installation script on **both** the Master and the Worker node.
### On the Master Node
SSH into your master node:
```bash
ssh azureuser@<MASTER_IP>
```
Transfer and run the script:
```bash
# Assuming you copy the script to the VM or create it manually:
chmod +x install-k8s-node.sh
./install-k8s-node.sh
```

### On the Worker Node
Do the same for the worker:
```bash
ssh azureuser@<WORKER_IP>
chmod +x install-k8s-node.sh
./install-k8s-node.sh
```

---

## 4. Initialize the Master Node
On the **Master Node** (`k8s-master`), initialize the cluster:
```bash
sudo kubeadm init --pod-network-cidr=192.168.0.0/16
```
Once it finishes, copy the output command that starts with `kubeadm join ...` to use later. 

Setup your local kubeconfig on the master:
```bash
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config
```

### Install Calico Network Plugin
On the **Master Node**:
```bash
kubectl create -f https://raw.githubusercontent.com/projectcalico/calico/v3.26.1/manifests/tigera-operator.yaml
kubectl create -f https://raw.githubusercontent.com/projectcalico/calico/v3.26.1/manifests/custom-resources.yaml
```

---

## 5. Join the Worker Node
On the **Worker Node** (`k8s-worker-1`), run the `kubeadm join` command you copied earlier from the master node. It will look like this:
```bash
sudo kubeadm join <MASTER_IP>:6443 --token <token> --discovery-token-ca-cert-hash sha256:<hash>
```

---

## 6. Configure Jenkins CD
1. Go to Jenkins and add your SSH Private Key as a credential (ID: `azure-master-ssh`, Username: `azureuser`).
2. Update the `MASTER_IP` variable inside `Jenkinsfile-CD-Azure` with your actual Master VM Public IP.
3. Run the Jenkins pipeline pointing to `Jenkinsfile-CD-Azure`. It will automatically SSH into the master node and apply all Kubernetes manifests in your `k8s/` folder.

---

## 7. Accessing FreeLink Services
Since we used `NodePort` mapping in the Network Security Group, you can access your exposed services using the **Worker Node Public IP**:

- **Keycloak**: `http://<WORKER_IP>:30090`
- **Mailhog**: `http://<WORKER_IP>:30025`
- **API Gateway**: `http://<WORKER_IP>:30081`

Internal microservices (User, Skills, Reservation, Condature, Evaluation, Time-Tracking) and Eureka are securely shielded via `ClusterIP` and only accessible within the cluster routing.
