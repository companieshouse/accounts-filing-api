package uk.gov.companieshouse.accounts.filing.model;

import com.fasterxml.jackson.annotation.JsonGetter;

public record ValidationState(@JsonGetter String state) {
}
