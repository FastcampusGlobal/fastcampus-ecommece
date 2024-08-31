
# ------------------------------------------------------------
# vpc and transitgate way
# ------------------------------------------------------------

resource "aws_ec2_transit_gateway_vpc_attachment" "vpc-internet_tgw_attachment" {
  subnet_ids         = aws_subnet.private.*.id
  transit_gateway_id = aws_ec2_transit_gateway.tgw.id
  vpc_id             = aws_vpc.public.id

  tags = {
    Name = "${var.environment}-internet-vpc-attachment"
  }
}

resource "aws_ec2_transit_gateway_vpc_attachment" "vpc-app_tgw_attachment" {
  subnet_ids         = aws_subnet.application[*].id
  transit_gateway_id = aws_ec2_transit_gateway.tgw.id
  vpc_id             = aws_vpc.application.id

  tags = {
    Name = "${var.environment}-app-vpc-attachment"
  }
}

resource "aws_ec2_transit_gateway_vpc_attachment" "vpc-database_tgw_attachment" {
  subnet_ids         = aws_subnet.data[*].id
  transit_gateway_id = aws_ec2_transit_gateway.tgw.id
  vpc_id             = aws_vpc.data.id

  tags = {
    Name = "${var.environment}-data-vpc-attachment"
  }
}



resource "aws_route" "tgw-route-platform-internet-private-data" {
  route_table_id         = aws_route_table.private.id
  destination_cidr_block = aws_vpc.data.cidr_block
  transit_gateway_id     = aws_ec2_transit_gateway.tgw.id
}

resource "aws_route" "internet_nat_gateway" {
  route_table_id = aws_route_table.private.id
  destination_cidr_block = "0.0.0.0/0"
  nat_gateway_id = aws_nat_gateway.main.id
}

resource "aws_route" "internet_gateway" {
  route_table_id = aws_route_table.internet_public_subnet_tb.id
  destination_cidr_block = "0.0.0.0/0"
  gateway_id = aws_internet_gateway.main.id
}

#add route from internet to management vpc (include public and private subnetwork)

#no intenet vpc private route id, add it mannually" "name"

resource "aws_route" "application-to-data" {
  route_table_id         = aws_route_table.application.id
  destination_cidr_block = aws_vpc.data.cidr_block
  transit_gateway_id     = aws_ec2_transit_gateway.tgw.id
}

resource "aws_route" "application-to-internet" {
  route_table_id         = aws_route_table.application.id
  destination_cidr_block = "0.0.0.0/0"
  transit_gateway_id     = aws_ec2_transit_gateway.tgw.id
}

resource "aws_route" "data-to-internet" {
  route_table_id         = aws_route_table.data.id
  destination_cidr_block = "0.0.0.0/0"
  transit_gateway_id     = aws_ec2_transit_gateway.tgw.id
}

resource "aws_route" "application-to-public" {
  route_table_id         = aws_route_table.application.id
  destination_cidr_block = aws_vpc.public.cidr_block
  transit_gateway_id     = aws_ec2_transit_gateway.tgw.id
}


resource "aws_route" "data-to-main" {
  route_table_id         = aws_route_table.data.id
  destination_cidr_block = aws_vpc.public.cidr_block
  transit_gateway_id     = aws_ec2_transit_gateway.tgw.id
}

resource "aws_ec2_transit_gateway_route" "route_public_internet_destination_to_platform_internet_vpc_attachment" {
  destination_cidr_block         = "0.0.0.0/0"
  transit_gateway_route_table_id = aws_ec2_transit_gateway.tgw.association_default_route_table_id
  transit_gateway_attachment_id  = aws_ec2_transit_gateway_vpc_attachment.vpc-internet_tgw_attachment.id
}