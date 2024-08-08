package uk.gov.companieshouse.accounts.filing.model.types;

public enum AccountsType {
    UNKNOWN("00", "UNKNOWN"),

    AUDITED_FULL("01", "AUDITED FULL"),
    AUDITED_SMALL("02", "AUDITED SMALL"),
    MEDIUM("03", "MEDIUM"),
    GROUP("04", "GROUP"),
    DORMANT("05", "DORMANT"),
    INTERIM("06", "INTERIM"),
    INITIAL("07", "INITIAL"),
    SMALL_COMPANY_FULL("08", "SMALL COMPANY FULL"),
    ABBREVIATED("09", "ABBREVIATED"),
    AUDIT_EXEMPT_SUBSIDIARY("14", "AUDIT EXEMPT"),
    FILING_EXEMPT_SUBSIDIARY("15", "FILING EXEMPT"),
    MICRO_ENTITY("16", "MICRO ENTITY"),
    AUDITED_ABRIDGED("17", "AUDITED ABRIDGED"),
    ABRIDGED("18", "ABRIDGED"),

    AUDITED_FULL_AMENDED("51", "AUDITED FULL AMENDED"),
    AUDITED_SMALL_AMENDED("52", "AUDITED SMALL AMENDED"),
    MEDIUM_AMENDED("53", "MEDIUM AMENDED"),
    GROUP_AMENDED("54", "GROUP AMENDED"),
    DORMANT_AMENDED("55", "DORMANT AMENDED"),
    SMALL_COMPANY_FULL_AMENDED("58", "SMALL COMPANY FULL AMENDED"),
    ABBREVIATED_AMENDED("59", "ABBREVIATED AMENDED"),
    MICRO_ENTITY_AMENDED("66", "MICRO ENTITY AMENDED"),
    AUDITED_ABRIDGED_AMENDED("67", "AUDITED ABRIDGED AMENDED"),
    ABRIDGED_AMENDED("68", "ABRIDGED AMENDED");

    private final String stemCode;

    private final String type;

    AccountsType(String stemCode, String type) {
        this.stemCode = stemCode;
        this.type = type;
    }

    public String getStemCode() {
        return stemCode;
    }

    public String getType() {
        return type;
    }

    public static AccountsType fromStemCode(final String stemCode) {
        for (final AccountsType type : values()) {
            if (type.getStemCode().equals(stemCode)) {
                return type;
            }
        }
        return UNKNOWN;
    }

}
