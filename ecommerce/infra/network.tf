data "aws_availability_zones" "available" {
  state = "available"
}

resource "aws_vpc" "public" {
  cidr_block       = "203.0.112.0/23"
  enable_dns_hostnames = true
  enable_dns_support = true
  tags = {
    Name = "main"
  }
}

resource aws_vpc "data" {
  cidr_block = "10.0.0.0/23"
  tags = {
    Name = "data"
  }
}

resource "aws_vpc" "application" {
  cidr_block = "10.0.2.0/23"
  tags = {
    Name = "application"
  }
}

resource "aws_ec2_transit_gateway" "tgw" {
  auto_accept_shared_attachments = "disable"

  description = "central transit gateway"
  tags = {
    Name = "central-tgw"
  }
}

resource "random_string" "id" {
  length  = 7
  lower   = true
  upper   = false
  special = false
}


########internet part ##########
# resource "aws_subnet" "internet_public_subnet" {
#   count             = 3
#   vpc_id            = aws_vpc.public.id
#   cidr_block        = element(local.remaining_cidr, count.index)
#   availability_zone = element([var.availability_zone_1a, var.availability_zone_1b, var.availability_zone_3c], count.index)

#   tags = merge({
#     Name        = "${var.environment}-${element([var.availability_zone_1a, var.availability_zone_1b], count.index)}-internet-subnet"
#     Environment = "${var.environment}"
#   }, local.default_tags)
# }

resource "aws_subnet" "public" {
  map_public_ip_on_launch = true
  vpc_id                  = aws_vpc.public.id
  cidr_block              = "203.0.112.0/25"
  availability_zone       = data.aws_availability_zones.available.names[1]
  tags = {
    Name = "public-subnet"
  }
}

resource "aws_subnet" "private" {
  count                   = 2
  vpc_id                  = aws_vpc.public.id
  cidr_block              = count.index == 0 ? "203.0.112.128/25" : "203.0.113.0/25"
  availability_zone       = data.aws_availability_zones.available.names[count.index + 1]

  tags = {
    Name = "private-subnet-${count.index + 1}"
    "kubernetes.io/role/internal-elb"           = "1"
    "kubernetes.io/cluster/eks-cluster" = "owned"
  }
}

resource "aws_route_table" "internet_public_subnet_tb" {
  vpc_id = aws_vpc.public.id

  tags = {
    Name        = "${var.environment}-internet-route-table"
    Environment = "${var.environment}"
  }
}

resource "aws_route_table" "private" {
  vpc_id = aws_vpc.public.id

  tags = {
    Name = "private-route-table"
  }
}

resource "aws_route_table_association" "internet_private" {
  count          = 2
  subnet_id      = aws_subnet.private[count.index].id
  route_table_id = aws_route_table.private.id
}

resource "aws_route_table_association" "internet" {
  count = 3
  subnet_id      = element(aws_subnet.public.*.id, count.index)
  route_table_id = aws_route_table.internet_public_subnet_tb.id
}

resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.public.id

  tags = {
    Name = "main-igw"
  }
}

resource "aws_eip" "nat" {

}

resource "aws_nat_gateway" "main" {
  allocation_id = aws_eip.nat.id
  subnet_id     = aws_subnet.public.id

  tags = {
    Name = "main-nat-gw"
  }
}

resource "aws_subnet" "application" {
  count             = 3
  vpc_id            = aws_vpc.application.id
  cidr_block        = cidrsubnet(aws_vpc.application.cidr_block, 2, count.index)
  availability_zone = data.aws_availability_zones.available.names[count.index]

  tags = {
    Name = "private-subnet-application-${count.index + 1}"
    "kubernetes.io/role/internal-elb"           = "1"
    "kubernetes.io/cluster/eks-cluster" = "owned"
  }
}

resource "aws_subnet" "data" {
  count             = 3
  vpc_id            = aws_vpc.data.id
  cidr_block        = cidrsubnet(aws_vpc.data.cidr_block, 2, count.index)
  availability_zone = data.aws_availability_zones.available.names[count.index]

  tags = {
    Name = "private-subnet-data-${count.index + 1}"
  }
}

resource "aws_route_table" "application" {
  vpc_id = aws_vpc.application.id


  tags = {
    Name = "application-private-route-table"
  }
}

# Associate private subnets with the private route table
resource "aws_route_table_association" "application" {
  count          = 3
  subnet_id      = element(aws_subnet.application.*.id, count.index)
  route_table_id = aws_route_table.application.id
}

resource "aws_route_table" "data" {
  vpc_id = aws_vpc.data.id
  tags = {
    Name = "data-private-route-table"
  }
}

resource "aws_route_table_association" "data" {
  count          = 3
  subnet_id      = element(aws_subnet.data.*.id, count.index)
  route_table_id = aws_route_table.data.id
}




# ########common part ##########
# resource "aws_subnet" "common_intra_subnet" {
#   vpc_id            = var.common-vpc-id
#   count             = 2
#   cidr_block        = element(cidrsubnets(var.vpc_common_cidr, 1, 1), count.index)
#   availability_zone = element([var.availability_zone_1a, var.availability_zone_1b], count.index)
#   #  map_public_ip_on_launch = true

#   tags = merge({
#     Name        = "${var.environment}-${element([var.availability_zone_1a, var.availability_zone_1b], count.index)}-common-subnet"
#     Environment = "${var.environment}"
#   }, local.default_tags)
# }

# resource "aws_route_table" "common_intra_subnet_tb" {
#   vpc_id = var.common-vpc-id

#   tags = merge({
#     Name        = "${var.environment}-gcc-common-route-table"
#     Environment = "${var.environment}"
#   }, local.default_tags)
# }

# resource "aws_route_table_association" "intra_common" {
#   count          = 2
#   subnet_id      = element(aws_subnet.common_intra_subnet.*.id, count.index)
#   route_table_id = aws_route_table.common_intra_subnet_tb.id
# }

# ##########app part##############
# resource "aws_subnet" "app_subnet" {
#   vpc_id            = var.app-vpc-id
#   count             = 2
#   cidr_block        = element(cidrsubnets(var.vpc_app_cidr, 1, 1), count.index)
#   availability_zone = element([var.availability_zone_1a, var.availability_zone_1b], count.index)
#   tags = merge({
#     Name                                        = "${var.environment}-${element([var.availability_zone_1a, var.availability_zone_1b], count.index)}-app-subnet"
#     Environment                                 = "${var.environment}"
#     "kubernetes.io/role/internal-elb"           = "1"
#     "kubernetes.io/cluster/${var.cluster_name}" = "owned"
#   }, local.default_tags)
# }

# resource "aws_route_table" "app_subnet_tb" {
#   vpc_id = var.app-vpc-id

#   tags = merge({
#     Name        = "${var.environment}-gcc-app-route-table"
#     Environment = "${var.environment}"
#   }, local.default_tags)
# }

# resource "aws_route_table_association" "app" {
#   count          = 2
#   subnet_id      = element(aws_subnet.app_subnet.*.id, count.index)
#   route_table_id = aws_route_table.app_subnet_tb.id
# }


# ########## database part ##############
# resource "aws_subnet" "database_subnet" {
#   vpc_id            = var.database-vpc-id
#   count             = 2
#   cidr_block        = element(cidrsubnets(var.vpc_database_cidr, 1, 1), count.index)
#   availability_zone = element([var.availability_zone_1a, var.availability_zone_1b], count.index)

#   tags = merge({
#     Name        = "${var.environment}-${element([var.availability_zone_1a, var.availability_zone_1b], count.index)}-database-subnet"
#     Environment = "${var.environment}"
#   }, local.default_tags)
# }


# resource "aws_route_table" "database_subnet_tb" {
#   vpc_id = var.database-vpc-id

#   tags = merge({
#     Name        = "${var.environment}-gcc-database-route-table"
#     Environment = "${var.environment}"
#   }, local.default_tags)
# }

# resource "aws_route_table_association" "database" {
#   count          = 2
#   subnet_id      = element(aws_subnet.database_subnet.*.id, count.index)
#   route_table_id = aws_route_table.database_subnet_tb.id
# }
