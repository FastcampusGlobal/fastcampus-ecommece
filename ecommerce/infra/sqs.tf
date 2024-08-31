resource "aws_sqs_queue" "ecomm_queue" {
  name                        = "ecommerce-fast"
  fifo_queue                  = false
  content_based_deduplication = false
  sqs_managed_sse_enabled     = false
}

output "url" {
  value = "https://${var.region}.console.aws.amazon.com/sqs/v2/home?region=${var.region}#/queues/${urlencode(aws_sqs_queue.ecomm_queue.url)}"
}