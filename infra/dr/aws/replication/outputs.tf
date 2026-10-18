output "dr_kms_key_arn" {
  description = "ARN de la clé KMS DR (à fournir au stack de failover)."
  value       = aws_kms_key.dr.arn
}

output "dr_documents_bucket" {
  description = "Bucket S3 des documents dans la région DR."
  value       = aws_s3_bucket.documents_dr.bucket
}

output "dr_exports_bucket" {
  description = "Bucket S3 des exports dans la région DR."
  value       = aws_s3_bucket.exports_dr.bucket
}

output "rds_replicated_backups_arn" {
  description = "ARN des sauvegardes automatiques RDS répliquées (source du restore PITR au failover)."
  value       = aws_db_instance_automated_backups_replication.this.id
}
