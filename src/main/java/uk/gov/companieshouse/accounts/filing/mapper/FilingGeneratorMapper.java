package uk.gov.companieshouse.accounts.filing.mapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.accounts.filing.model.types.AccountsType;
import uk.gov.companieshouse.api.model.felixvalidator.PackageTypeApi;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;

@Component
public class FilingGeneratorMapper {

    @Value("${file.bucket}")
    private String bucket;

    @Value("${file.scheme}")
    private String scheme;

    public FilingApi mapToFilingApi(AccountsFilingEntry accountsFilingEntry) {

        var madeUpDate = accountsFilingEntry.getMadeUpDate();
        // Description value
        Map<String, String> descriptionValue = Collections.singletonMap("made up date", madeUpDate);

        var filingApiEntity = new FilingApi();
        if(PackageTypeApi.OVERSEAS == accountsFilingEntry.getPackageType()){
            filingApiEntity.setDescription("Package accounts with package type overseas");
        }else{
            filingApiEntity.setDescription("Package accounts made up to " + formatMadeUpDate(madeUpDate));
        }

        filingApiEntity.setDescriptionIdentifier(getAccountTypeName(accountsFilingEntry));
        filingApiEntity.setDescriptionValues(descriptionValue);
        filingApiEntity.setKind("accounts");
        filingApiEntity.setData(mapData(accountsFilingEntry, madeUpDate));

        return filingApiEntity;
    }

    private String getAccountTypeName(AccountsFilingEntry accountsFilingEntry) {
        return AccountsType.fromStemCode(accountsFilingEntry.getAccountsType()).getType();
    }

    private Map<String, Object> mapData(AccountsFilingEntry accountsFilingEntry, String madeUpDate) {
        Map<String, Object> data = new HashMap<>();
        data.put("package_type", accountsFilingEntry.getPackageType().toString());
        data.put("accounts_type", accountsFilingEntry.getAccountsType());
        data.put("links", mapLinks(accountsFilingEntry));
        data.put("period_end_on", madeUpDate);
        return data;
    }

    private List<Map<String, String>> mapLinks(AccountsFilingEntry accountsFilingEntry) {
        Map<String, String> location = new HashMap<>();
        location.put("rel", "accounts");
        // The file id location
        location.put("href", String.format("%s%s/%s", scheme, bucket, accountsFilingEntry.getFileId()));
        return List.of(location);
    }

    private String formatMadeUpDate(String madeUpDate) {
        DateTimeFormatter fromFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter toFormat = DateTimeFormatter.ofPattern("d MMMM yyyy");
        return LocalDate.parse(madeUpDate, fromFormat).format(toFormat);
    }

}
