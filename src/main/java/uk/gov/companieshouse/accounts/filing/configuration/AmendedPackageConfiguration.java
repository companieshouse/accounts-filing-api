package uk.gov.companieshouse.accounts.filing.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "feature.flag.amended")
public record AmendedPackageConfiguration(boolean defaultEnableAmendedPackages, boolean allowCICAmendedPackages) {
}
