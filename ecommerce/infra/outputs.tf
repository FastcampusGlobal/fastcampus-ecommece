output "aws_load_balancer_controller_role_arn" {
  value = aws_iam_role.aws_load_balancer_controller.arn
}

output "aws_public_vpc_id" {
  value = aws_vpc.public.id
}

output "aws_cluster_name" {
  value = aws_eks_cluster.cluster.name
}

output "aws_public_subnet_id" {
  value = aws_subnet.public.id
}