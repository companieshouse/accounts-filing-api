package uk.gov.companieshouse.accounts.filing.model;

import com.fasterxml.jackson.annotation.JsonGetter;

public record FilingValidationResponse(@JsonGetter boolean isValid) {
}