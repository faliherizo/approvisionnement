# API Gateway HTTP : edge managé (limitation de débit, journalisation, WAF possible) devant l'API.
# - route asynchrone /publications → Lambda (mise en file de publication)
# - le trafic applicatif principal passe par l'ingress ALB d'EKS via le VPC Link ci-dessous.
resource "aws_apigatewayv2_api" "this" {
  name          = "${local.name}-api"
  protocol_type = "HTTP"
  cors_configuration {
    allow_origins = ["https://contrat.vinaitsolution.com"]
    allow_methods = ["GET", "POST", "PUT", "OPTIONS"]
    allow_headers = ["authorization", "content-type"]
  }
}

# VPC Link pour intégrer l'ingress privé d'EKS (ALB interne) à l'API Gateway.
resource "aws_security_group" "vpclink" {
  name_prefix = "${local.name}-vpclink-"
  vpc_id      = module.vpc.vpc_id
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = [var.vpc_cidr] # aucune sortie hors du VPC : rien à joindre sur Internet
  }
  lifecycle { create_before_destroy = true }
}

resource "aws_apigatewayv2_vpc_link" "eks" {
  name               = "${local.name}-eks-link"
  security_group_ids = [aws_security_group.vpclink.id]
  subnet_ids         = module.vpc.private_subnets
}

# Intégration concrète : déclenchement de publication asynchrone via la Lambda.
resource "aws_apigatewayv2_integration" "publication" {
  api_id                 = aws_apigatewayv2_api.this.id
  integration_type       = "AWS_PROXY"
  integration_uri        = aws_lambda_function.publication_worker.arn
  payload_format_version = "2.0"
}

resource "aws_apigatewayv2_route" "publication" {
  api_id    = aws_apigatewayv2_api.this.id
  route_key = "POST /publications"
  target    = "integrations/${aws_apigatewayv2_integration.publication.id}"
}

resource "aws_lambda_permission" "apigw" {
  statement_id  = "AllowApiGw"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.publication_worker.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_apigatewayv2_api.this.execution_arn}/*/*"
}

resource "aws_cloudwatch_log_group" "apigw" {
  name              = "/aws/apigw/${local.name}"
  retention_in_days = 90
}

resource "aws_apigatewayv2_stage" "default" {
  api_id      = aws_apigatewayv2_api.this.id
  name        = "$default"
  auto_deploy = true

  default_route_settings {
    throttling_burst_limit = 200
    throttling_rate_limit  = 100
  }
  access_log_settings {
    destination_arn = aws_cloudwatch_log_group.apigw.arn
    format          = jsonencode({ requestId = "$context.requestId", ip = "$context.identity.sourceIp", status = "$context.status", route = "$context.routeKey" })
  }
}
