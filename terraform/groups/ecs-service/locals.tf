# Define all hardcoded local variable and local variables looked up from data resources
locals {
  stack_name                = "filing-maintain" # this must match the stack name the service deploys into
  name_prefix               = "${local.stack_name}-${var.environment}"
  service_name              = "accounts-filing-api"
  container_port            = "3000" # default port required here until prod docker container is built allowing port change via env var
  docker_repo               = "accounts-filing-api"
  lb_listener_rule_priority = 15
  lb_listener_paths         = ["/accounts-filing.*", "/transactions/.*/accounts-filing.*"]
  healthcheck_path          = "/accounts-filing/healthcheck" #healthcheck path for accounts-filing-api
  healthcheck_matcher       = "200"

  kms_alias       = "alias/${var.aws_profile}/environment-services-kms"
  service_secrets = jsondecode(data.vault_generic_secret.service_secrets.data_json)

  parameter_store_secrets = {
    "vpc_name"             = local.service_secrets["vpc_name"]
    "chs_api_key"          = local.service_secrets["chs_api_key"]
    "internal_api_url"     = local.service_secrets["internal_api_url"]
  }

  vpc_name             = local.service_secrets["vpc_name"]
  chs_api_key          = local.service_secrets["chs_api_key"]
  internal_api_url     = local.service_secrets["internal_api_url"]

  # create a map of secret name => secret arn to pass into ecs service module
  # using the trimprefix function to remove the prefixed path from the secret name
  secrets_arn_map = {
    for sec in data.aws_ssm_parameter.secret :
    trimprefix(sec.name, "/${local.name_prefix}/") => sec.arn
  }

  service_secrets_arn_map = {
    for sec in module.secrets.secrets :
    trimprefix(sec.name, "/${local.service_name}-${var.environment}/") => sec.arn
  }

  task_secrets = [
    { "name" : "CHS_API_KEY", "valueFrom" : "${local.service_secrets_arn_map.chs_api_key}" },
    { "name" : "INTERNAL_API_URL", "valueFrom" : "${local.service_secrets_arn_map.internal_api_url}" }
  ]

  task_environment = [
    { "name" : "API_URL", "value" : "${var.api_url}" },
    { "name" : "HUMAN_LOG", "value" : "${var.human_log}" },
    { "name" : "LOG_LEVEL", "value" : "${var.log_level}" },
    { "name" : "MONGODB_URL", "value" : "${var.mongodb_url}" }
  ]
}
