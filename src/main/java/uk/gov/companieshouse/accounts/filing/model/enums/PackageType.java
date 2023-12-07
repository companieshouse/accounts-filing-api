package uk.gov.companieshouse.accounts.filing.model.enums;

import java.util.Optional;

public enum PackageType {

    UKSEF("UKSEF"),
    WELSH("Welsh"),
    CIC("CIC"),
    LIMITED_PARTNERSHIP("Limited Partnership"),
    GROUP_PACKAGE_400("Group Package 400"),
    GROUP_PACKAGE_401("Group Package 401"),
    OVERSEAS("overseas"),
    AUDIT_EXEMPT_SUBSIDIARY("Audit Exempt Subsidiary"),
    FILING_EXEMPT_SUBSIDIARY("Filing Exempt Subsidiary");

    private final String type;

    private PackageType (String type) {
        this.type = type;
    }

    private String lowerCase(){
        return this.type.toLowerCase();
    }

    public static Optional<PackageType> findPackageType(String packageString) {

        if(packageString == null){
            return Optional.empty();
        }

        String formattedPackageString = packageString.strip().toLowerCase();
        for (PackageType packageType : PackageType.values()) {
            if (packageType.lowerCase().contentEquals(formattedPackageString))
            {
                return Optional.of(packageType);
            }
        }
        return Optional.empty();
    }
}
