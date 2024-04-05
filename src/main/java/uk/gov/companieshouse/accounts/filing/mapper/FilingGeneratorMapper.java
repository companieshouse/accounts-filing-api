package uk.gov.companieshouse.accounts.filing.mapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.accounts.filing.model.types.AccountsType;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;

@Component
public class FilingGeneratorMapper {

    @Value("${file.bucket}")
    private static String bucket;

    @Value("${file.scheme}")
    private static String scheme;

    public FilingApi mapToFilingApi(AccountsFilingEntry accountsFilingEntry) {

        var madeUpDate = accountsFilingEntry.getMadeUpDate();
        // Description value
        Map<String, String> descriptionValue = Collections.singletonMap("made up date", madeUpDate);

        var filingApiEntity = new FilingApi();
        filingApiEntity.setDescription("Package accounts made up to " + madeUpDate);
        filingApiEntity.setDescriptionIdentifier(getAccountTypeName(accountsFilingEntry));
        filingApiEntity.setDescriptionValues(descriptionValue);
        filingApiEntity.setKind("package-accounts");
        filingApiEntity.setData(mapData(accountsFilingEntry, madeUpDate));

        return filingApiEntity;
    }

    private String getAccountTypeName(AccountsFilingEntry accountsFilingEntry) {
        return AccountsType.fromStemCode(accountsFilingEntry.getAccountsType()).getType();
    }

    private Map<String, Object> mapData(AccountsFilingEntry accountsFilingEntry, String madeUpDate) {
        Map<String, Object> data = new HashMap<>();
        data.put("packageType", accountsFilingEntry.getPackageType().toString());
        data.put("accountsType", accountsFilingEntry.getAccountsType());
        data.put("links", mapLocation(accountsFilingEntry));
        data.put("madeUpDate", madeUpDate);
        return data;
    }

    private Map<String, String> mapLocation(AccountsFilingEntry accountsFilingEntry) {
        Map<String, String> location = new HashMap<>();
        location.put("rel", "accounts");
        // The file id location
        location.put("href", String.format("%s%s/%s", scheme, bucket, accountsFilingEntry.getFileId()));
        return location;
    }

}
