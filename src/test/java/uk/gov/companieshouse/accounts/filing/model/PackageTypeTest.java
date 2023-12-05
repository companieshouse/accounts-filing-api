package uk.gov.companieshouse.accounts.filing.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.accounts.filing.model.enums.PackageType;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PackageTypeTest {

    @Test
    @DisplayName("with valid package type")
    public void testValidPackageTypeUKSEF() {
        Optional<PackageType> resultPackageType =  PackageType.findPackageType("UKSEF");
        assertEquals(PackageType.UKSEF, resultPackageType.get());
    }

    @Test
    @DisplayName("with invalid package type")
    public void testInvalidPackageType() {
        Optional<PackageType> resultPackageType =  PackageType.findPackageType("ABC");
        assertTrue(resultPackageType.isEmpty());
    }
}
