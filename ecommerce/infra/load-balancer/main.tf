terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
    }
  }
}

provider "aws" {
  region  = "us-east-1"
}


# Create NLB
resource "aws_lb" "nlb" {
    name               = "test-nlb-tf"
    internal           = false
    load_balancer_type = "network"
    subnets            = var.public_subnet_ids
    enable_cross_zone_load_balancing = true
}

# Create NLB target group that forwards traffic to alb
# https://docs.aws.amazon.com/elasticloadbalancing/latest/APIReference/API_CreateTargetGroup.html
resource "aws_lb_target_group" "nlb_tg" {
    name         = "tf-nlb-tg"
    port         = 80
    protocol     = "TCP"
    vpc_id       = var.public_vpc_id
    target_type  = "alb"
}

# Create target group attachment
# More details: https://docs.aws.amazon.com/elasticloadbalancing/latest/APIReference/API_TargetDescription.html
# https://docs.aws.amazon.com/elasticloadbalancing/latest/APIReference/API_RegisterTargets.html
resource "aws_lb_target_group_attachment" "tg_attachment" {
    target_group_arn = aws_lb_target_group.nlb_tg.arn
    target_id        = var.eks_load_balancer_arn
    port             = 80
}

resource "aws_lb_listener" "nlb_listener" {
  load_balancer_arn = aws_lb.nlb.arn
  port              = "80"
  protocol          = "TCP"

  default_action {
    type = "forward"
    target_group_arn = aws_lb_target_group.nlb_tg.arn
  }
}