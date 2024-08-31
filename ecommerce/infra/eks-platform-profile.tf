resource "aws_eks_fargate_profile" "platform_service" {
  cluster_name           = aws_eks_cluster.cluster.name
  fargate_profile_name   = "platform_service"
  pod_execution_role_arn = aws_iam_role.eks-fargate-profile.arn

  # These subnets must have the following resource tag: 
  # kubernetes.io/cluster/<CLUSTER_NAME>.
  subnet_ids = flatten([aws_subnet.private[*].id])
  

  selector {
    namespace = "platform-service"
  }

  depends_on = [ aws_eks_cluster.cluster ]

  lifecycle {
    replace_triggered_by = [ aws_eks_cluster.cluster ]
  }
}