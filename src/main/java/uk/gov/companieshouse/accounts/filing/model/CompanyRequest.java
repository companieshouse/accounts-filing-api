package uk.gov.companieshouse.accounts.filing.model;

import com.fasterxml.jackson.annotation.JsonGetter;
public record CompanyRequest(@JsonGetter("companyName") String companyName) {
}