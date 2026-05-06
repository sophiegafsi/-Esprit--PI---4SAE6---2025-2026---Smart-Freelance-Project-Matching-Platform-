#!/bin/bash
# ==============================================================================
# Azure Resource Creation Script for FreeLink Kubernetes Cluster
# Optimized for Azure Student Subscription in spaincentral
# ==============================================================================
# Prerequisites: 
# 1. Install Azure CLI (az)
# 2. Login using: az login
# ==============================================================================

RESOURCE_GROUP="rg-freelink-spain"
LOCATION="swedencentral"
VNET_NAME="vnet-freelink"
SUBNET_NAME="subnet-freelink"
NSG_NAME="nsg-freelink"

echo "Creating Resource Group ($RESOURCE_GROUP)..."
az group create --name $RESOURCE_GROUP --location $LOCATION

echo "Creating Virtual Network and Subnet..."
az network vnet create \
  --resource-group $RESOURCE_GROUP \
  --name $VNET_NAME \
  --address-prefix 10.0.0.0/16 \
  --subnet-name $SUBNET_NAME \
  --subnet-prefix 10.0.1.0/24

echo "Creating Network Security Group (NSG)..."
az network nsg create --resource-group $RESOURCE_GROUP --name $NSG_NAME

echo "Configuring NSG Rules for Kubernetes & FreeLink..."
# SSH
az network nsg rule create --resource-group $RESOURCE_GROUP --nsg-name $NSG_NAME --name Allow-SSH --priority 1000 --destination-port-ranges 22 --protocol Tcp --access Allow
# K8s API
az network nsg rule create --resource-group $RESOURCE_GROUP --nsg-name $NSG_NAME --name Allow-K8s-API --priority 1010 --destination-port-ranges 6443 --protocol Tcp --access Allow
# NodePort Range (for Keycloak, API Gateway, Mailhog)
az network nsg rule create --resource-group $RESOURCE_GROUP --nsg-name $NSG_NAME --name Allow-NodePorts --priority 1020 --destination-port-ranges 30000-32767 --protocol Tcp --access Allow
# HTTP / HTTPS
az network nsg rule create --resource-group $RESOURCE_GROUP --nsg-name $NSG_NAME --name Allow-HTTP --priority 1030 --destination-port-ranges 80 --protocol Tcp --access Allow
az network nsg rule create --resource-group $RESOURCE_GROUP --nsg-name $NSG_NAME --name Allow-HTTPS --priority 1040 --destination-port-ranges 443 --protocol Tcp --access Allow

echo "Creating Master Node VM (Standard_B2s - 2 vCPU, 4GB RAM)..."
az vm create \
  --resource-group $RESOURCE_GROUP \
  --name k8s-master \
  --image Ubuntu2204 \
  --admin-username azureuser \
  --generate-ssh-keys \
  --vnet-name $VNET_NAME \
  --subnet $SUBNET_NAME \
  --nsg $NSG_NAME \
  --public-ip-sku Standard \
  --size Standard_D2s_v3

echo "Creating Worker Node VM (Standard_B2ms - 2 vCPU, 8GB RAM)..."
az vm create \
  --resource-group $RESOURCE_GROUP \
  --name k8s-worker-1 \
  --image Ubuntu2204 \
  --admin-username azureuser \
  --generate-ssh-keys \
  --vnet-name $VNET_NAME \
  --subnet $SUBNET_NAME \
  --nsg $NSG_NAME \
  --public-ip-sku Standard \
  --size Standard_D2s_v3

echo "========================================="
echo "Azure VMs Created Successfully!"
echo "Master IP:"
MASTER_IP=$(az vm show -d -g $RESOURCE_GROUP -n k8s-master --query publicIps -o tsv)
echo $MASTER_IP

echo "Worker IP:"
WORKER_IP=$(az vm show -d -g $RESOURCE_GROUP -n k8s-worker-1 --query publicIps -o tsv)
echo $WORKER_IP
echo "========================================="
echo "Next step: SSH into these VMs and run install-k8s-node.sh"
echo "ssh azureuser@$MASTER_IP"
