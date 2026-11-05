package uk.gov.companieshouse.accounts.filing.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(AmendedPackageConfiguration.class)
class AmendedPackageConfigurationTest {
    
    @Autowired
    private AmendedPackageConfiguration configuration;

    @Nested
    @TestPropertySource ("classpath:application.properties")
    @DisplayName("Enabled revised packages")
    class Enabled {
        @Test
        @DisplayName("Enable all revised packages true")
        void testShouldReturnTrueWhenEnableAllRevisedPackagesIsTrue() {
            assertThat(configuration.enableAllAmendedPackages()).isTrue();
        }
    }


    @Nested
    @TestPropertySource(properties = {"feature.flag.revised.enable-all-revised-packages=false"})
    @DisplayName("Disabled revised packages")
    class Disabled {
        @Test
        @DisplayName("Enable all revised packages false")
        void testShouldReturnFalseWhenEnableAllRevisedPackagesIsFalse() {
            assertThat(configuration.enableAllAmendedPackages()).isFalse();
        }
    }

}
