package uk.gov.companieshouse.accounts.filing.model.types;

import uk.gov.companieshouse.accounts.filing.exceptionhandler.UriValidationException;

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

    private PackageType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.type;

    }

    public static PackageType findPackageType(String packageString) throws UriValidationException {

        for (PackageType packageType : PackageType.values()) {
            if (packageType.toString().equals(packageString)) {
                return packageType;
            }
        }

        throw new UriValidationException(String.format("%s does not match a valid packageType", packageString));
    }
}
