package uk.gov.companieshouse.accounts.filing.model.types;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import uk.gov.companieshouse.accounts.filing.exceptionhandler.UriValidationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PackageTypeTest {

    @Test
    @DisplayName("with valid package type")
    void testValidPackageTypeUKSEF() {
        PackageType resultPackageType =  PackageType.findPackageType("UKSEF");
        assertEquals(PackageType.UKSEF, resultPackageType);
    }

    @Test
    @DisplayName("with invalid package type")
    void testInvalidPackageType() {
        assertThrows(UriValidationException.class, () -> PackageType.findPackageType("ABC"));
    }
}
