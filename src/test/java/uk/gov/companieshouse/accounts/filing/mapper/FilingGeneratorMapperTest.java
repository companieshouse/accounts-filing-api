package uk.gov.companieshouse.accounts.filing.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.api.model.felixvalidator.PackageTypeApi;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;

class FilingGeneratorMapperTest {

    AccountsFilingEntry accountsFilingEntry;

    FilingGeneratorMapper filingGeneratorMapper;

    private final static String madeUpDate = "now";
    private final static String accountsType = "01";
    private static List<Map<String, String>> links;

    private AccountsFilingEntry createAccountsFilingEntry() {
        var filingEntry = new AccountsFilingEntry("accountFilingId");
        filingEntry.setAccountsType(accountsType);
        filingEntry.setFileId("fileId");
        filingEntry.setMadeUpDate(madeUpDate);
        filingEntry.setPackageType(PackageTypeApi.UKSEF);
        return filingEntry;
    }

    private List<Map<String, String>> createLinks() {
        Map<String, String> links = new HashMap<>();
        links.put("rel", "accounts");
        links.put("href", "schemebucket/fileId");
        return List.of(links);
    }

    @BeforeEach
    void beforeEach() {
        filingGeneratorMapper = new FilingGeneratorMapper();
        ReflectionTestUtils.setField(filingGeneratorMapper, "bucket", "bucket");
        ReflectionTestUtils.setField(filingGeneratorMapper, "scheme", "scheme");
        links = createLinks();
    }

    @Test
    void testMapToFilingApi() {
        accountsFilingEntry = createAccountsFilingEntry();
        FilingApi filingApi = filingGeneratorMapper.mapToFilingApi(accountsFilingEntry);
        assertEquals("Package accounts made up to " + madeUpDate, filingApi.getDescription());
        assertEquals("AUDITED FULL", filingApi.getDescriptionIdentifier());
        assertEquals(Collections.singletonMap("made up date", madeUpDate), filingApi.getDescriptionValues());
        assertEquals("accounts", filingApi.getKind());
        assertEquals(PackageTypeApi.UKSEF.toString(), filingApi.getData().get("package_type"));
        assertEquals(accountsType, filingApi.getData().get("accounts_type"));
        assertEquals(createLinks(), filingApi.getData().get("links"));
        assertEquals(madeUpDate, filingApi.getData().get("period_end_on"));
        
    }

}
