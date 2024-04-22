package uk.gov.companieshouse.accounts.filing.model.types;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.felixvalidator.PackageTypeApi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PackageTypeTest {

    private static final String examplePackageType = "uksef";

    @Test
    @DisplayName("with valid package type")
    void testValidPackageTypeUKSEF() throws URIValidationException {
        PackageTypeApi resultPackageType =  PackageTypeApi.findPackageType(examplePackageType);
        assertEquals(PackageTypeApi.UKSEF, resultPackageType);
    }

    @Test
    @DisplayName("with invalid package type")
    void testInvalidPackageType() {
        assertThrows(URIValidationException.class, () -> PackageTypeApi.findPackageType("ABC"));
    }
}
