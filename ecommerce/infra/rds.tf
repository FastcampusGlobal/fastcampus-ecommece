resource "aws_security_group" "db_sg" {
  name_prefix = "${var.environment}-db_sg-${random_string.id.result}"
  vpc_id      = aws_vpc.data.id
  ingress {
    from_port = 5432
    to_port   = 5432
    protocol  = "tcp"
    cidr_blocks = aws_subnet.private.*.cidr_block

  }
  ingress {
    from_port = 6379
    to_port   = 6379
    protocol  = "tcp"
    cidr_blocks = aws_subnet.private.*.cidr_block
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}


resource "aws_rds_cluster" "example" {
  iam_database_authentication_enabled = false
  cluster_identifier                  = "${var.environment}-aurora-${random_string.id.result}"
  engine                              = "aurora-postgresql"
  engine_mode                         = "provisioned"
  engine_version                      = "13.12"
  database_name                       = "ecommerce"
  skip_final_snapshot                 = true
  master_username                     = "test"
  master_password                     = "must_be_eight_characters"
  vpc_security_group_ids              = [aws_security_group.db_sg.id]
  db_subnet_group_name                = aws_db_subnet_group.default.name
  storage_encrypted                   = true
  deletion_protection                 = false
  copy_tags_to_snapshot               = true
  enabled_cloudwatch_logs_exports     = ["postgresql"]
  backup_retention_period             = 5
  serverlessv2_scaling_configuration {
    max_capacity = 1.0
    min_capacity = 0.5
  }

  tags = {
    backup_plan = "High"
  }
}


# Create IAM role for enhanced monitoring
resource "aws_iam_role" "enhanced_monitoring_role" {
  name = "enhanced-monitoring-role-${random_string.id.result}"
  assume_role_policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        "Effect" : "Allow",
        "Principal" : {
          "Service" : "monitoring.rds.amazonaws.com"
        },
        "Action" : "sts:AssumeRole"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "enhanced_monitoring_policy_attachment" {
  role       = aws_iam_role.enhanced_monitoring_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonRDSEnhancedMonitoringRole"
}

resource "aws_rds_cluster_instance" "readers" {
  count                = 1
  identifier_prefix    = "${var.environment}-${random_string.id.result}"
  db_subnet_group_name = aws_db_subnet_group.default.name
  cluster_identifier   = aws_rds_cluster.example.id
  instance_class       = "db.serverless"
  engine               = aws_rds_cluster.example.engine
  engine_version       = aws_rds_cluster.example.engine_version
  monitoring_interval  = 60 # Monitoring interval in seconds
  monitoring_role_arn  = aws_iam_role.enhanced_monitoring_role.arn

  tags = {
    Name        = "${var.environment}-${random_string.id.result}"
    backup_plan = "High"
  }
}

resource "aws_db_subnet_group" "default" {
  name       = "${var.environment}-rds-${random_string.id.result}"
  subnet_ids = aws_subnet.data.*.id

  tags = {
    Name = "${var.environment}-rds-${random_string.id.result}"
  }
}



output "subnet_group_name" {
  value = aws_db_subnet_group.default.id
}

# resource "aws_secretsmanager_secret" "secret" {
#   description         = "Secrets for aws rds"
#   name                = "postgres-secret"
# }

# resource "aws_secretsmanager_secret_version" "secret" {
#   lifecycle {
#     ignore_changes = [
#       secret_string
#     ]
#   }
#   secret_id     = aws_secretsmanager_secret.secret.id
#   secret_string = <<EOF
# {
#   "username": "${aws_rds_cluster.example.master_password}",
#   "password": "${aws_rds_cluster.example.master_username}",
#   "engine": "postgres",
#   "host": "${aws_rds_cluster.example.endpoint}",
#   "port": 5432,
#   "dbClusterIdentifier": "${aws_rds_cluster.example.cluster_identifier}",
#   "db" : "${aws_rds_cluster.example.database_name}"
# }
# EOF
# }

# resource "aws_db_proxy" "proxy" {
#   name                   = "ecommerce-proxy"
#   debug_logging          = false
#   engine_family          = "POSTGRESQL"
#   idle_client_timeout    = 1800
#   require_tls            = false
#   role_arn               = aws_iam_role.eks-cluster.arn
#   vpc_security_group_ids = [aws_security_group.db_sg.id]
#   vpc_subnet_ids         = aws_subnet.data.*.id
  

#   auth {
#     auth_scheme = "SECRETS"
#     description = "example"
#     iam_auth    = "DISABLED"
#     client_password_auth_type = "POSTGRES_MD5"
#     secret_arn = aws_secretsmanager_secret.secret.arn
#   }
# }

# resource "aws_db_proxy_default_target_group" "example" {
#   db_proxy_name = aws_db_proxy.proxy.name

#   connection_pool_config {
#     connection_borrow_timeout    = 120
#     max_connections_percent      = 100
#     max_idle_connections_percent = 50
#     session_pinning_filters      = ["EXCLUDE_VARIABLE_SETS"]
#   }
# }

# resource "aws_db_proxy_target" "example" {
#   db_instance_identifier = aws_rds_cluster.example.cluster_identifier
#   db_proxy_name          = aws_db_proxy.proxy.name
#   target_group_name      = aws_db_proxy_default_target_group.example.name
# }