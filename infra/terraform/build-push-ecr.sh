#!/bin/bash

# Script que builda e envia as imagens pro ECR

# Configura o aws cli
set -e
AWS_REGION="us-east-2"
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
APP_REPO_NAME="solicitacao-saque-app"
MONGO_REPO_NAME="solicitacao-saque-mongo"
APP_IMAGE_TAG="latest"
MONGO_IMAGE_TAG="7.0"

# Loga no ECR
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com

# Builda imagem da aplicacao
docker build --platform linux/amd64 -t $APP_REPO_NAME:$APP_IMAGE_TAG .

#Tageia e envia a imagem pro ECR
docker tag $APP_REPO_NAME:$APP_IMAGE_TAG $ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$APP_REPO_NAME:$APP_IMAGE_TAG
docker push $ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$APP_REPO_NAME:$APP_IMAGE_TAG

#Atualiza o ECS com as novas imagens
aws ecs update-service --cluster solicitacao-saque-cluster --service solicitacao-saque-app --force-new-deployment