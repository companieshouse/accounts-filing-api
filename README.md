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

## Revised filing feature flags

Set `FEATURE_FLAG_DISABLE_REVISED_FILING_22102026=true` to block revised filings
(numeric validator account types of 50 or greater). Set
`FEATURE_FLAG_ALLOW_CIC_REVISED_FILING_22102026=true` to allow revised CIC packages
while keeping other revised packages blocked. Both flags default to `false`;
when the disable flag is `false`, revised filings remain permitted for all packages.
