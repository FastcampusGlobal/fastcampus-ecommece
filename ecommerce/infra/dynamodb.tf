resource "aws_dynamodb_table" "location_table" {
  name           = "location-table"
  hash_key       = "id"
  billing_mode   = "PAY_PER_REQUEST"

  attribute {
    name = "id"
    type = "S"
  }

  stream_enabled   = true
  stream_view_type = "NEW_IMAGE"
}

resource "aws_kinesis_stream" "location_table" {
  name        = "location_table_changes"
  shard_count = 1
}

resource "aws_dynamodb_kinesis_streaming_destination" "location_table" {
  stream_arn                               = aws_kinesis_stream.location_table.arn
  table_name                               = aws_dynamodb_table.location_table.name
}

resource "aws_redshift_subnet_group" "subnet_redshift" {
  name       = "subnet-redshift"
  subnet_ids = [aws_subnet.public.id]

  tags = {
    environment = "Production"
  }
}

resource "aws_redshift_cluster" "redshift_cluster" {
  cluster_identifier = "tf-redshift-cluster"
  database_name      = "test"
  master_username    = "testuser"
  master_password    = "T3stPass"
  node_type          = "ra3.large"
  cluster_type       = "single-node"
  publicly_accessible = true
  cluster_subnet_group_name = aws_redshift_subnet_group.subnet_redshift.name
}

resource "aws_cloudwatch_log_group" "firehose_logs" {
  name              = "/aws/vendedlogs/firehose/delivery-dynamodb"
  retention_in_days = 14  # Adjust retention period as needed
}

resource "aws_cloudwatch_log_stream" "firehose_delivery" {
  name           = "delivery-redshift"
  log_group_name = aws_cloudwatch_log_group.firehose_logs.name
}

resource "aws_kinesis_firehose_delivery_stream" "test_stream" {
  name        = "kinesis-firehose-redshift"
  destination = "redshift"
  kinesis_source_configuration {
    kinesis_stream_arn = aws_kinesis_stream.location_table.arn
    role_arn           = aws_iam_role.firehose_role.arn
  }

  redshift_configuration {
    role_arn           = aws_iam_role.firehose_role.arn
    cluster_jdbcurl    = "jdbc:redshift://${aws_redshift_cluster.redshift_cluster.endpoint}/${aws_redshift_cluster.redshift_cluster.database_name}"
    username           = "testuser"
    password           = "T3stPass"
    data_table_name    = "driver_location"
    data_table_columns = "trip_id, driver_id, driver_name, driver_phone, vehicle_id, vehicle_model, passenger_id, passenger_name, passenger_phone, start_time, end_time, start_latitude, start_longitude, end_latitude, end_longitude, trip_status, recorded_time, current_latitude, current_longitude, accuracy, speed"
    s3_backup_mode     = "Enabled"
    copy_options       = "json 'auto' TRUNCATECOLUMNS blanksasnull emptyasnull" # the default delimiter

    s3_configuration {
      role_arn           = aws_iam_role.firehose_role.arn
      bucket_arn         = aws_s3_bucket.bucket.arn
      buffering_size     = 1
      buffering_interval = 60
      compression_format = "UNCOMPRESSED"
      prefix               = "dynamo-redshift-destination/"
    }

    s3_backup_configuration {
      role_arn           = aws_iam_role.firehose_role.arn
      bucket_arn         = aws_s3_bucket.bucket.arn
      buffering_size     = 1
      buffering_interval = 60
      compression_format = "UNCOMPRESSED"
      prefix               = "dynamo-redshift-destination-backup/"
    }

    cloudwatch_logging_options {
      enabled = true
      log_group_name = aws_cloudwatch_log_group.firehose_logs.name
      log_stream_name = aws_cloudwatch_log_stream.firehose_delivery.name
    }

  }
}

resource "aws_iam_role" "firehose_role" {
  name = "firehose_admin_role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "firehose.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "admin_policy" {
  role       = aws_iam_role.firehose_role.name
  policy_arn = "arn:aws:iam::aws:policy/AdministratorAccess"
}

resource "aws_s3_bucket" "bucket" {
  bucket = "redshift-ingestion"
}
