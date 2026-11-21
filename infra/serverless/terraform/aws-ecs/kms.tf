# Clé de chiffrement gérée par l'organisation (rotation annuelle) : documents, exports, sondes et alertes.
# Contrairement à la clé gérée par AWS, sa politique d'accès, sa rotation et sa révocation appartiennent à l'exploitant.
resource "aws_kms_key" "donnees" {
  description             = "${local.name} - chiffrement des donnees au repos"
  enable_key_rotation     = true
  deletion_window_in_days = 30
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      { Sid = "Administration", Effect = "Allow", Principal = { AWS = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:root" }, Action = "kms:*", Resource = "*" },
      { Sid = "AlertesSNS", Effect = "Allow", Principal = { Service = ["sns.amazonaws.com", "cloudwatch.amazonaws.com"] }, Action = ["kms:GenerateDataKey*", "kms:Decrypt"], Resource = "*" }
    ]
  })
}

resource "aws_kms_alias" "donnees" {
  name          = "alias/${local.name}-donnees"
  target_key_id = aws_kms_key.donnees.key_id
}
