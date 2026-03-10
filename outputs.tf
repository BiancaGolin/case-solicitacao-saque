output "vpc_id" {
  description = "ID of the VPC"
  value       = aws_vpc.main.id
}

output "ecs_cluster_name" {
  description = "Name of the ECS cluster"
  value       = aws_ecs_cluster.main.name
}

output "app_repository_url" {
  description = "URL of the ECR repository for the app"
  value       = aws_ecr_repository.app.repository_url
}

output "mongo_repository_url" {
  description = "URL of the ECR repository for MongoDB"
  value       = aws_ecr_repository.mongo.repository_url
}

output "alb_dns_name" {
  description = "DNS name of the Application Load Balancer"
  value       = aws_lb.app.dns_name
}

output "app_service_name" {
  description = "Name of the ECS service for the app"
  value       = aws_ecs_service.app.name
}

output "mongo_service_name" {
  description = "Name of the ECS service for MongoDB"
  value       = aws_ecs_service.mongo.name
}