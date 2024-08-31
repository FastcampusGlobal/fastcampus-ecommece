variable public_subnet_ids {
  type = list(string)
  default = ["subnet-02d936e4ceaf2d690"]
}

variable public_vpc_id {
  type = string
  default = "vpc-01eaa73077b68106c"
}

variable eks_load_balancer_arn {
  type = string
  default = "arn:aws:elasticloadbalancing:us-east-1:959896818063:loadbalancer/app/k8s-platform-phpapach-2aa6590727/12b0c0a92d782458"
}