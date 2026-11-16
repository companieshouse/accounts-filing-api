# accounts-filing-api
A backend service allowing users to submit accounts in zip format. This application is written using the [Spring Boot](http://projects.spring.io/spring-boot/) Java framework.

## Requirements
In order to run the API locally you'll need the following installed on your machine:

- [Java 21](https://www.oracle.com/java/technologies/downloads/#java21)
- [Maven](https://maven.apache.org/download.cgi)
- [Git](https://git-scm.com/downloads)

## Getting Started
1. Run `make`
2. Run `./start.sh`

## Amended filing feature flags

Transaction resources are created when the package type is saved, without a costs
link. A successful file-validation result with a numeric accounts type refreshes
the resources; costs links are then included only for non-amended CIC and overseas
accounts. Incomplete or failed validation does not refresh transaction resources.

Amended accounts are identified by the supported amended values in the
`AccountsType` enum, not by a numeric threshold. Both feature flags default to
`false`. Amended filings are allowed only when both
`FEATURE_FLAG_ENABLE_ALL_AMENDED_PACKAGES_22102026` and
`FEATURE_FLAG_CIC_AMENDED_PACKAGES_ENABLED_22102026` are `true`, and the package
type is CIC. All other amended filings, including non-CIC packages, are rejected.
