resource "aws_iam_role" "eks-cluster" {
  name = "eks-cluster"

  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Effect = "Allow",
      Principal = {
        Service = "eks.amazonaws.com"
      },
      Action = "sts:AssumeRole"
    }, {
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = {
        AWS = "arn:aws:iam::${var.account_id}:user/${var.user_iam}"
      }
    }]
  })
}

resource "aws_iam_role_policy_attachment" "amazon-eks-cluster-policy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSClusterPolicy"
  role       = aws_iam_role.eks-cluster.name
}

resource "aws_iam_role_policy_attachment" "amazon-eks-admin" {
  policy_arn = "arn:aws:iam::aws:policy/AdministratorAccess"
  role       = aws_iam_role.eks-cluster.name
}

resource "aws_eks_cluster" "cluster" {
  name     = "eks-cluster"
  role_arn = aws_iam_role.eks-cluster.arn

  vpc_config {
    endpoint_private_access = true
    endpoint_public_access  = true
    subnet_ids              = flatten([aws_subnet.private[*].id])
    security_group_ids      = [aws_security_group.platform_eks.id]
  }
  enabled_cluster_log_types = ["audit", "api", "authenticator", "controllerManager", "scheduler"]

  depends_on = [
    aws_iam_role_policy_attachment.amazon-eks-cluster-policy
  ]
}

resource "aws_security_group" "platform_eks" {
  name   = "eks-sg"
  vpc_id = aws_vpc.public.id

  ingress {
    from_port = 443
    to_port   = 443
    protocol  = "tcp"
    cidr_blocks = [aws_vpc.data.cidr_block, aws_vpc.public.cidr_block]
  }

  ingress {
    from_port = 80
    to_port   = 80
    protocol  = "tcp"
    cidr_blocks = [  aws_vpc.data.cidr_block, aws_vpc.public.cidr_block]

  }

  ingress {
    from_port = 8080
    to_port   = 8080
    protocol  = "tcp"
    cidr_blocks = [  aws_vpc.data.cidr_block, aws_vpc.public.cidr_block]

  }

  egress {
    from_port = 443
    to_port   = 443
    protocol  = "tcp"
    cidr_blocks = [  aws_vpc.data.cidr_block, aws_vpc.public.cidr_block]

  }

  egress {
    from_port = 8080
    to_port   = 8080
    protocol  = "tcp"
    cidr_blocks = [  aws_vpc.data.cidr_block, aws_vpc.public.cidr_block]
  }

  egress {
    from_port = 80
    to_port   = 80
    protocol  = "tcp"
    cidr_blocks = [  aws_vpc.data.cidr_block, aws_vpc.public.cidr_block]
  }

  tags = {
    Name = "eks-sg"
  }
}
