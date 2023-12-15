package uk.gov.companieshouse.accounts.filing.model;

import com.fasterxml.jackson.annotation.JsonGetter;

public record AccountsPackageType(@JsonGetter("package_type") String type) {
    
}
