variable "aws_region" {
  description = "Região da AWS onde os recursos serão implantados"
  type        = string
  default     = "us-east-2"
}

variable "app_name" {
  description = "Nome da aplicação"
  type        = string
  default     = "solicitacao-saque"
}

variable "environment" {
  description = "Nome do ambiente"
  type        = string
  default     = "dev"
}

variable "vpc_cidr" {
  description = "Bloco CIDR da VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnets" {
  description = "Blocos CIDR das sub-redes públicas"
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "private_subnets" {
  description = "Blocos CIDR das sub-redes privadas"
  type        = list(string)
  default     = ["10.0.3.0/24", "10.0.4.0/24"]
}

variable "app_image_tag" {
  description = "Tag da imagem Docker da aplicação"
  type        = string
  default     = "latest"
}

variable "mongo_image_tag" {
  description = "Tag da imagem Docker do MongoDB"
  type        = string
  default     = "7.0"
}