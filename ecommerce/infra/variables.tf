# ------------------------------------------------------------
# Define global environments
# ------------------------------------------------------------
variable "environment" {
  type        = string
  default = "platform"
  description = "Environment name or equivalent for CI CD and resource naming purpose."
}

variable account_id {
  type  = string
  default = "959896818063"
}

variable user_iam {
  type = string
  default = "ronald-cli"
}

variable "region" {
  type        = string
  default = "us-east-1"
  description = "Region where resources are deployed in."
}

variable "project" {
  type        = string
  default = ""
  description = "The name of the project."
}

variable "tags" {
  type        = map(string)
  default     = {}
  description = "A mapping of tags to assign to all resources."
}

# variable "cluster_name" {
#   type        = string
#   description = "EKS Cluster name"
# }

# ------------------------------------------------------------
# VPC mobule variables
# ------------------------------------------------------------

variable "public-vpc-id" {
  type    = string
  default = "common-vpc"
}

variable "public_vpc_igw_id" {
  type    = string
  default = "222222"
}


variable "common-vpc-id" {
  type    = string
  default = "common-vpc"
}

variable "app-vpc-id" {
  type    = string
  default = ""
}

variable "database-vpc-id" {
  type    = string
  default = ""
}

variable "integration-vpc-id" {
  type    = string
  default = ""
}

variable "availability_zone_1a" {
  type        = string
  default = "ap-southeast-3a"
  description = "Availability Zone where resources are deployed to."
}

variable "availability_zone_1b" {
  type        = string
  default = "ap-southeast-3b"
  description = "Availability Zone where resources are deployed to."
}

variable "availability_zone_3c" {
  type        = string
  default = "ap-southeast-3c"
  description = "Availability Zone where resources are deployed to."
}

# ------------------------------------------------------------
# Network resources pertaining to genexis Data Source
# ------------------------------------------------------------
# variable "common_subnet_cidr_1" {
#   type        = string
#   description = "Private subnet range for web related resources."
# }

# variable "common_subnet_cidr_2" {
#   type        = string
#   description = "Private subnet range for web related resources."
# }

# variable "vpc_internet_cidr" {
#   type        = string
#   description = "The CIDR block that belongs to the VPC."
# }


# variable "vpc_common_cidr" {
#   type        = string
#   description = "The CIDR block that belongs to the VPC."
# }


# variable "common_pub_subnet_cidr_2" {
#   type        = string
#   description = "public subnet range for web related resources."
# }

# variable "vpc_app_cidr" {
#   type        = string
#   description = "The CIDR block that belongs to the VPC."
# }


# variable "vpc_database_cidr" {
#   type        = string
#   description = "The CIDR block that belongs to the VPC."
# }


# variable "vpc_integration_cidr" {
#   type        = string
#   description = "The CIDR block that belongs to the VPC."
# }

# variable "integration_vpc_igw_id" {
#   type        = string
#   description = "integration_vpc_igw_id."
# # }

# variable "predefined_internet_subnets" {
#   type = list(string)
# }

# variable "predefined_internet_public_subnets" {
#   type = list(string)
# }

# variable "predefined_internet_private_route_table_id" {
#   type = string
# }

# variable "vpc_management_cidr" {
#   type        = string
#   description = "The CIDR block that belongs to the VPC."
# }

# variable "management_vpc_id" {
#   type        = string
#   description = "The CIDR block that belongs to the VPC."
# }

# variable "management_vpc_igw_id" {
#   type        = string
#   description = "The CIDR block that belongs to the VPC."
# }

# variable "environment_tag" {
#   type        = string
#   description = "The CIDR block that belongs to the VPC."
# }

# ------------------------------------------------------------
# Network resources pertaining to genexis SFTP ()
# ------------------------------------------------------------
/* variable "genexis_in_sftp_cidr" {
  type        = string
  description = "Private IP address from genexis SFTP (non-prod) environment."
}

variable "genexis_out_sftp_cidr" {
  type        = string
  description = "Private IP address to genexis SFTP (non-prod) environment."
} */

# variable "internet_vpc_subnet_count" {
#   type = map(string)
#   default = {
#     "platform"   = "1"
#     "template-a" = "2"
#     "template-b" = "0"
#   }
# }

# variable "project_random_id" {
#   type = string
# }

# variable predefined_internet_public_route_tb {
#   type = string
# }
