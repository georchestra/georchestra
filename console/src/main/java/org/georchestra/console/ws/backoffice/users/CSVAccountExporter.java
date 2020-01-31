/*
 * Copyright (C) 2019 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.georchestra.console.ws.backoffice.users;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.georchestra.console.ds.OrgsDao;
import org.georchestra.console.dto.Account;
import org.georchestra.console.dto.orgs.Org;

import lombok.Getter;
import lombok.NonNull;

public class CSVAccountExporter {

    static enum OutlookCSVHeaderField {
        FIRST_NAME("First Name", Account::getCommonName), //
        MIDDLE_NAME("Middle Name"), //
        LAST_NAME("Last Name", Account::getSurname), //
        TITLE("Title", Account::getTitle), //
        SUFFIX("Suffix"), //
        INITIALS("Initials"), //
        WEBPAGE("Web Page"), //
        GENDER("Gender"), //
        BDAY("Birthday"), //
        ANNIVERSARY("Anniversary"), //
        LOCATION("Location"), //
        LANG("Language"), //
        INTERNET_FREE_BUSY("Internet Free Busy"), //
        NOTES("Notes", (a, o) -> o == null ? null : o.getDescription()), //
        EMAIL("E-mail Address", Account::getEmail), //
        EMAIL2("E-mail 2 Address"), //
        EMAIL3("E-mail 3 Address"), //
        PHONE("Primary Phone", Account::getPhone), //
        HOME_PHONE("Home Phone"), //
        HOME_PHONE2("Home Phone 2"), //
        MOBILE_PHONE("Mobile Phone", Account::getMobile), //
        PAGER("Pager"), //
        HOME_FAX("Home Fax"), //
        HOME_ADDRESS("Home Address", Account::getHomePostalAddress), //
        HOME_STREET("Home Street"), //
        HOME_STREET2("Home Street 2"), //
        HOME_STREET3("Home Street 3"), //
        HOME_POBOX("Home Address PO Box"), //
        HOME_CITY("Home City", Account::getLocality), //
        HOME_STATE("Home State"), //
        HOME_PC("Home Postal Code"), //
        HOME_COUNTRY("Home Country"), //
        SPOUSE("Spouse"), //
        CHILDREN("Children"), //
        MANAGER_NAME("Manager's Name", Account::getManager), //
        ASSISTANT_NAME("Assistant's Name"), //
        REFERRED_BY("Referred By"), //
        COMPANY_PHONE("Company Main Phone"), //
        BIZ_PHONE("Business Phone"), //
        BIZ_PHONE2("Business Phone 2"), //
        BIZ_FAX("Business Fax", Account::getFacsimile), //
        ASSISTANT_PHONE("Assistant's Phone"), //
        COMPANY("Company", (a, org) -> org == null ? null : org.getName()), //
        JOB_TITLE("Job Title", Account::getDescription), //
        DEPARTMENT("Department"), //
        OFFICE_LOCATION("Office Location"), //
        ORG_ID("Organizational ID Number"), //
        PROFESSION("Profession"), //
        ACCOUNT("Account"), //
        BIZ_ADDRESS("Business Address", Account::getPostalAddress), //
        BIZ_ADDR_STREET("Business Street", Account::getStreet), //
        BIZ_ADDR_STREET2("Business Street 2"), //
        BIZ_ADDR_STREET3("Business Street 3"), //
        BIZ_ADDR_POBOX("Business Address PO Box", Account::getPostOfficeBox), //
        BIZ_CITY("Business City", (a, o) -> {
            if (o == null || o.getCities() == null) {
                return null;
            }
            return o.getCities().stream().collect(Collectors.joining(","));
        }), //
        BIZ_STATE("Business State"), //
        BIZ_PC("Business Postal Code", Account::getPostalCode), //
        BIZ_COUNTRY("Business Country", Account::getStateOrProvince), //
        OTHER_PHONE("Other Phone"), //
        OTHER_FAX("Other Fax"), //
        OTHER_ADDR("Other Address", Account::getRegisteredAddress), //
        OTHER_ST("Other Street", Account::getPhysicalDeliveryOfficeName), //
        OTHER_ST2("Other Street 2"), //
        OTHER_ST3("Other Street 3"), //
        OTHER_POBOX("Other Address PO Box"), //
        OTHER_CITY("Other City"), //
        OTHER_STATE("Other State"), //
        OTHER_PC("Other Postal Code"), //
        OTHER_COUNTRY("Other Country"), //
        CALLBACK("Callback"), //
        CAR_PHONE("Car Phone"), //
        ISDN("ISDN"), //
        RADIO_PHONE("Radio Phone"), //
        TTY_PHONE("TTY/TDD Phone"), //
        TELEX("Telex"), //
        USER1("User 1"), //
        USER2("User 2"), //
        USER3("User 3"), //
        USER4("User 4"), //
        KEYWORDS("Keywords"), //
        MILEAGE("Mileage"), //
        HOBBY("Hobby"), //
        BILLING_INFO("Billing Information"), //
        DIRECTORY_SERVER("Directory Server"), //
        SENSITIVITY("Sensitivity"), //
        PRIORITY("Priority"), //
        PRIVATE("Private", Account::getNote), //
        CATEGORIES("Categories");

        private final @NonNull @Getter String name;
        private final BiFunction<Account, Org, String> valueExtractor;

        private OutlookCSVHeaderField(String name) {
            this.name = name;
            this.valueExtractor = (a, o) -> null;
        }

        private OutlookCSVHeaderField(@NonNull String name, @NonNull Function<Account, String> valueExtractor) {
            this.name = name;
            this.valueExtractor = (a, o) -> valueExtractor.apply(a);
        }

        private OutlookCSVHeaderField(@NonNull String name, @NonNull BiFunction<Account, Org, String> valueExtractor) {
            this.name = name;
            this.valueExtractor = valueExtractor;
        }

        public String apply(Account acc, Org org) {
            return this.valueExtractor.apply(acc, org);
        }
    }

    /**
     * The commons-csv format used to generate the CSV exports, explicitly using
     * {@link QuoteMode#MINIMAL MINIMAL} quote mode so the delimiters are added only
     * if needed.
     * 
     * @see CSVFormat#RFC4180
     */
    static final CSVFormat FORMAT = CSVFormat.RFC4180.withHeader(headerNames()).withQuoteMode(QuoteMode.MINIMAL);

    private final OrgsDao orgsDao;

    public CSVAccountExporter(@NonNull OrgsDao orgsDao) {
        this.orgsDao = orgsDao;
    }

    private static final String[] headerNames() {
        return Arrays.stream(OutlookCSVHeaderField.values()).map(OutlookCSVHeaderField::getName).toArray(String[]::new);
    }

    /**
     * Exports a CSV file with Outlook compliant headers to the given target for the
     * provided accounts
     */
    public void export(@NonNull Iterable<Account> accounts, @NonNull Appendable target) throws IOException {
        Map<String, Org> orgsById = new HashMap<>();

        final CSVPrinter printer = FORMAT.print(target);
        for (Account acc : accounts) {
            Org org = orgsById.computeIfAbsent(acc.getOrg(), id -> orgsDao.findByCommonNameWithExt(acc));
            printer.printRecord(toRecord(acc, org));
        }
        printer.flush();
    }

    private Iterable<String> toRecord(@NonNull Account account, @Nullable Org org) {
        List<String> values = new ArrayList<>();
        for (OutlookCSVHeaderField header : OutlookCSVHeaderField.values()) {
            values.add(header.apply(account, org));
        }
        return values;
    }

    public static void main(String... args) {
        Arrays.asList(OutlookCSVHeaderField.values()).forEach(f -> System.err.println(f.getName()));
    }
}
