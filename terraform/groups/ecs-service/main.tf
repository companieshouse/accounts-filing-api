terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.54.0"
    }
    vault = {
      source  = "hashicorp/vault"
      version = "~> 3.18.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

terraform {
  backend "s3" {}
}

module "secrets" {
  source = "git@github.com:companieshouse/terraform-modules//aws/ecs/secrets?ref=1.0.229"

  name_prefix = "${local.service_name}-${var.environment}"
  environment = var.environment
  kms_key_id  = data.aws_kms_key.kms_key.id
  secrets     = local.parameter_store_secrets
}

module "ecs-service" {
  source = "git@github.com:companieshouse/terraform-modules//aws/ecs/ecs-service?ref=1.0.229"

  # Environmental configuration
  environment             = var.environment
  aws_region              = var.aws_region
  aws_profile             = var.aws_profile
  vpc_id                  = data.aws_vpc.vpc.id
  ecs_cluster_id          = data.aws_ecs_cluster.ecs_cluster.id
  task_execution_role_arn = data.aws_iam_role.ecs_cluster_iam_role.arn

  # Load balancer configuration
  lb_listener_arn           = data.aws_lb_listener.filing_maintain_lb_listener.arn
  lb_listener_rule_priority = local.lb_listener_rule_priority
  lb_listener_paths         = local.lb_listener_paths

  use_task_container_healthcheck = true
  healthcheck_path               = local.healthcheck_path
  healthcheck_matcher            = local.healthcheck_matcher

  # Docker container details
  docker_registry   = var.docker_registry
  docker_repo       = local.docker_repo
  container_version = var.accounts_filing_api_version
  container_port    = local.container_port

  # Service configuration
  service_name = local.service_name
  name_prefix  = local.name_prefix

  # Service Healthcheck configuration

  # Service performance and scaling configs
  desired_task_count                 = var.desired_task_count
  required_cpus                      = var.required_cpus
  required_memory                    = var.required_memory
  service_autoscale_enabled          = var.service_autoscale_enabled
  service_autoscale_target_value_cpu = var.service_autoscale_target_value_cpu
  service_scaledown_schedule         = var.service_scaledown_schedule
  service_scaleup_schedule           = var.service_scaleup_schedule
  use_capacity_provider              = var.use_capacity_provider
  use_fargate                        = var.use_fargate
  fargate_subnets                    = local.application_subnet_ids

  # Cloudwatch
  cloudwatch_alarms_enabled = var.cloudwatch_alarms_enabled

  # Service environment variable and secret configs
  task_environment = local.task_environment
  task_secrets     = local.task_secrets

  # Eric variables
  use_eric_reverse_proxy  = local.use_eric_reverse_proxy
  eric_port               = local.eric_port
  eric_version            = local.eric_version
  eric_cpus               = var.required_cpus
  eric_memory             = var.required_memory
  eric_environment        = local.eric_environment
  eric_secrets            = local.eric_secrets

  depends_on = [module.secrets]
}
