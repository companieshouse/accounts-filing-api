package uk.gov.companieshouse.accounts.filing.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.accounts.filing.exceptionhandler.UriValidationException;
import uk.gov.companieshouse.accounts.filing.model.enums.PackageType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
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
