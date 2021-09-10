package org.georchestra.security;

import static java.lang.String.format;
import static org.georchestra.commons.security.SecurityHeaders.SEC_ORG;
import static org.georchestra.commons.security.SecurityHeaders.SEC_ORGNAME;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.commons.security.SecurityHeaders;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import lombok.NonNull;
import lombok.Value;

/**
 * Support class for {@link LdapUserDetailsRequestHeaderProvider} to load and
 * validate the header mappings configuration.
 * <p>
 * The header mappings define assign LDAP attributes related to the
 * authenticated user, to proxified request headers.
 * <p>
 * Direct user properties, as well as its organization's and manager can be
 * configured.
 * <p>
 * Additions, per-service specific header mappings can be configured to augment
 * and/or override the default header mappings.
 * <p>
 * Default header mappings are specified as
 * {@code <header-name>=<ldap-attribute>}, whereas service-specific mappings as
 * {@code <service-name>.<header-name>=<ldap-attribute>}.
 * <p>
 * Final headers for {@link #getMappings(String) a given service} are the
 * aggregation of default headers plus whichever service specific headers have
 * been set for it.
 * <p>
 * Service names match the ones configured in the data directory's
 * {@code security-prox/tagets-mapping.properties}.
 * <p>
 * Finally, {@code <ldap-attirbute>} is of the form
 * {@code [<encoding>:]<attribute-name>}, allowing to encode the LDAP attribute
 * values with the specified {@code <encoding>} before sending it to the
 * proxified application. The resulting header value, with an encoding
 * specified, is <code>{encoding}&lt;encoded-value&gt;</code>.
 * <p>
 * At this time, only {@code base64} is supported as encoding.
 * <p>
 * For the sake of simplicity and backwards compatibility, the root of LDAP
 * attribute names are the ones in the LDAP context for the authenticated user.
 * The user organization's properties are to be prefixed with {@code org.} or
 * {@code org.seeAlso} depending on whether they're standard or extended
 * organization properties; and the user's manager properties with
 * {@code manager.}.
 * <p>
 * <h2>Embedded headers:</h2> Regardless of the contents of the
 * {@link #loadFrom(Map) configuration map}, the following "embedded" headers
 * are guaranteed to be sent:
 * 
 * <pre>
 * {@code
 * sec-org=org.cn
 * sec-orgname=org.o
 * }
 * </pre>
 * 
 * <h2>Sample config:</h2> The following is an example of a {@code .properties}
 * file containing such mappings:
 * 
 * <pre>
 * {@code
 * sec-email=mail
 * sec-firstname=givenName
 * sec-lastname=sn
 * sec-tel=telephoneNumber
 * datafeeder.sec-firstname=base64:givenName
 * datafeeder.sec-lastname=base64:sn
 * datafeeder.sec-orgname=base64:org.o
 * datafeeder.sec-org-linkage=base64:org.seeAlso.labeledURI
 * datafeeder.sec-org-address:base64:org.seeAlso.postalAddress
 * datafeeder.sec-org-category:base64:org.seeAlso.businessCategory
 * datafeeder.sec-org-description:base64:org.seeAlso.description
 * datafeeder.sec-org-notes:base64:org.seeAlso.knowledgeInformation
 * }
 * </pre>
 * 
 * Note how the mappings specific to the {@code datafeeder} service override the
 * embedded header {@code sec-orgname}, and the configured default headers
 * {@code sec-firstname} and {@code sec-lastname}, to send their values in
 * base64 encoding, and adds other headers related to the user's organization,
 * that'll be sent only to requests targeting the base URL for the
 * {@code datafeeder} service.
 * <p>
 * The following are all the valid LDAP properties that can be configured:
 * 
 * <h2>Authenticated user LDAP attributes:</h2>
 * <ul>
 * <li>uid='testadmin'
 * <li>givenName='Gabriel'
 * <li>sn='Raúl Roldán'
 * <li>cn='Gabriel Raúl Roldán'
 * <li>telephoneNumber='0054-555-7654321'
 * <li>mail='testadmin@test.com'
 * <li>postalAddress='Avenue of Testing 123 10º B'
 * <li>description='Admin user'
 * <li>title='Amo del universo'
 * <li>objectClass='georchestraUser'
 * <li>knowledgeInformation='Internal CRM notes on testadmin'
 * </ul>
 * <p>
 * <h2>Authenticated user's Organization LDAP attributes:</h2>
 * <ul>
 * <li>org.cn='PSC'
 * <li>org.ou='PSC'
 * <li>org.o='Project Steering Committee'
 * <li>org.member='uid=testadmin,ou=users,dc=georchestra,dc=org'
 * <li>org.description='2A004,2B033'
 * <li>org.objectClass='groupOfMembers'
 * <li>org.seeAlso.o='PSC'
 * <li>org.seeAlso.labeledURI='https://www.georchestra.org/'
 * <li>org.seeAlso.businessCategory='Association'
 * <li>org.seeAlso.postalAddress='127 rue georchestra, 73590 Chamblille'
 * <li>org.seeAlso.description='Association PSC geOrchestra'
 * <li>org.seeAlso.knowledgeInformation='Internal CRM notes on PSC'
 * <li>org.seeAlso.objectClass='organization'
 * </ul>
 * <p>
 * <h2>Authenticated user's Manager LDAP attributes:</h2>
 * <ul>
 * <li>manager.uid='testeditor'
 * <li>manager.mail='psc+testeditor@georchestra.org'
 * <li>manager.givenName='Test'
 * <li>manager.description='editor'
 * <li>manager.sn='EDITOR'
 * <li>manager.cn='testeditor'
 * <li>manager.objectClass='georchestraUser'
 * </ul>
 * <p>
 * Note that mapping some attributes is forbidden and not listed above, such as
 * {@code userPassword}.
 * 
 * @since 21.0
 */
public class LdapHeaderMappings {
    protected static final Log logger = LogFactory.getLog(LdapHeaderMappings.class.getPackage().getName());

    static final Set<String> FORBIDDEN_PROPERTIES = ImmutableSet.of("userPassword", "manager.userPassword", "memberOf",
            "org.member", "org.seeAlso.jpegPhoto");

    private static final Set<String> USER_ATTRIBUTES = ImmutableSet.copyOf(Arrays.asList(//
            "telephoneNumber", //
            "mail", //
            "postalAddress", //
            "description", //
            "cn", //
            "title", //
            "objectClass", //
            "uid", //
            "givenName", //
            "sn", //
            "knowledgeInformation", //
            "georchestraObjectIdentifier"//
    ));

    private static final Set<String> MANAGER_ATTRIBUTES = ImmutableSet.copyOf(Arrays.asList(//
            "manager.uid", //
            "manager.mail", //
            "manager.givenName", //
            "manager.description", //
            "manager.sn", //
            "manager.cn", //
            "manager.objectClass"//
    ));

    private static final Set<String> ORGANIZATION_ATTRIBUTES = ImmutableSet.copyOf(Arrays.asList(//
            "org.ou", //
//"org.member='uid=testadmin,ou=users,dc=georchestra,dc=org'
            "org.description", //
            "org.cn", //
            "org.objectClass", //
            "org.o", //
            "org.seeAlso.labeledURI", //
            "org.seeAlso.businessCategory", //
            "org.seeAlso.postalAddress", //
            "org.seeAlso.description", //
            "org.seeAlso.knowledgeInformation", //
            "org.seeAlso.objectClass", //
            "org.seeAlso.o", //
            "org.seeAlso.georchestraObjectIdentifier"//
    ));

    static final Set<String> ALL_VALID_ATTRIBUTES = ImmutableSet.<String>builder()//
            .addAll(USER_ATTRIBUTES)//
            .addAll(MANAGER_ATTRIBUTES)//
            .addAll(ORGANIZATION_ATTRIBUTES)//
            .build();

    /**
     * Header mappings that are to be sent regardless of the {@link #loadFrom(Map)
     * user defined mappings}
     */
    static final Map<String, String> EMBEDDED_MAPPINGS = ImmutableMap.of(SEC_ORG, "org.cn", SEC_ORGNAME, "org.o");

    /**
     * Header mappings that apply to all services (i.e. have no service prefix in
     * header-mappings.properties), for example: {@code sec-email=mail}
     */
    private HeaderMappings defaultMappings = HeaderMappings.valueOf(EMBEDDED_MAPPINGS);

    /**
     * Header mappings that apply to a specific target service, by service name,
     * where service name matches the ones assigned in
     * {@code targets-mapping.properties}. For example, for service
     * {@code analytics}, {@code targets-mappings.properties} contains
     * {@code analytics=http://analytics:8080/analytics/}, and
     * {@code headers-mappings.properties} may contain
     * {@code analytics.sec-firstname=givenName}.
     */
    Map<String, HeaderMappings> serviceMappings = new HashMap<>();

    /**
     * 
     * @param rawMappings
     * @throws IllegalArgumentException if some target property is not recognizable
     *                                  or its usage is forbidden (e.g. a password
     *                                  field)
     */
    public void loadFrom(Map<String, String> rawMappings) {

        final Map<String, String> defaultRawMappings;
        final Map<String, Map<String, String>> perServiceRawMappings;

        defaultRawMappings = loadDefaultMappings(rawMappings);
        perServiceRawMappings = loadPerServiceMappings(rawMappings, defaultRawMappings);

        this.defaultMappings = HeaderMappings.valueOf(defaultRawMappings);
        this.serviceMappings = new HashMap<>();
        perServiceRawMappings.forEach((service, raw) -> {
            serviceMappings.put(service, HeaderMappings.valueOf(raw));
        });
    }

    public HeaderMappings getDefaultMappings() {
        return defaultMappings;
    }

    public HeaderMappings getMappings(@NonNull String targetServiceName) {
        HeaderMappings mappings = this.serviceMappings.get(targetServiceName);
        if (null == mappings) {
            return getDefaultMappings();
        }
        return mappings;
    }

    private Map<String, String> loadDefaultMappings(Map<String, String> mappings) {
        Map<String, String> defaultMappings = new HashMap<>(EMBEDDED_MAPPINGS);
        defaultMappings.forEach((h, m) -> logger.info(format("Added embedded header mapping %s=%s", h, m)));

        mappings.entrySet().stream()//
                .filter(LdapHeaderMappings::isGlobalHeader)//
                .map(LdapHeaderMappings::validateTargetProperty)//
                .forEach(e -> {
                    String header = e.getKey();
                    String property = e.getValue();
                    logger.info(format("Loaded default header mapping %s=%s", header, property));
                    defaultMappings.put(header, property);
                });

        return defaultMappings;
    }

    private Map<String, Map<String, String>> loadPerServiceMappings(final Map<String, String> rawMappings,
            final Map<String, String> defaultRawMappings) {

        Map<String, Map<String, String>> perServiceMappings = new HashMap<>();

        rawMappings.entrySet().stream()//
                .filter(LdapHeaderMappings::isServiceSpecificHeader)//
                .map(LdapHeaderMappings::validateTargetProperty)//
                .forEach(e -> {
                    int index = e.getKey().indexOf('.');
                    final String serviceName = e.getKey().substring(0, index);
                    final String headerName = e.getKey().substring(index + 1);
                    final String targetProperty = e.getValue();

                    Map<String, String> serviceHeaders;
                    serviceHeaders = perServiceMappings.computeIfAbsent(serviceName,
                            s -> new HashMap<>(defaultRawMappings));

                    serviceHeaders.put(headerName, targetProperty);

                    if (defaultRawMappings.containsKey(headerName)) {
                        final String defVale = defaultRawMappings.get(headerName);
                        if (defVale.equals(targetProperty)) {
                            return;
                        }
                        logger.info(
                                format("Loaded header mapping for service %s: %s=%s, overrides default header %s=%s", //
                                        serviceName, //
                                        headerName, //
                                        serviceHeaders.get(headerName), //
                                        headerName, //
                                        defVale));
                    } else {
                        logger.info(format("Loaded header mapping for service %s: %s=%s", //
                                serviceName, //
                                headerName, //
                                targetProperty));
                    }
                });
        return perServiceMappings;
    }

    /**
     * @param propertyMapping key is the configured header name, value the target
     *                        property
     * @throws IllegalArgumentException if the target property is not recognizable
     *                                  or its usage is forbidden (e.g. a password
     *                                  field)
     */
    static Map.Entry<String, String> validateTargetProperty(final Map.Entry<String, String> propertyMapping) {
        String error = null;
        final String headerName = propertyMapping.getKey();
        if (!StringUtils.hasLength(propertyMapping.getValue())) {
            error = format("No target attribute is defined for header '%s'.", headerName);
        } else {
            final String targetPropertyName = HeaderMapping.stripEncoding(propertyMapping.getValue());
            if (!StringUtils.hasLength(targetPropertyName)) {
                error = format("No target attribute is defined for header '%s'.", headerName);
            } else if (FORBIDDEN_PROPERTIES.contains(targetPropertyName)) {
                error = format("Target attribute '%s' configured for header '%s' is forbidden.", targetPropertyName,
                        headerName);
            } else if (!ALL_VALID_ATTRIBUTES.contains(targetPropertyName)) {
                error = format("Target attribute '%s' configured for header '%s' does not exist.", targetPropertyName,
                        headerName);
            }
        }
        if (error != null) {
            logger.error(error);
            throw new IllegalArgumentException(error);
        }
        return propertyMapping;
    }

    public static @Value class HeaderMapping {

        public enum Encoding {
            NONE, BASE64
        }

        @NonNull
        String headerName;
        @NonNull
        String ldapAttribute;
        @NonNull
        Encoding encoding;
        @NonNull
        String fullPropertyName;

        public String encode(String... values) {
            if (encoding == Encoding.BASE64) {
                return SecurityHeaders.encodeBase64(values);
            }
            return values == null ? null
                    : (values.length == 1 ? values[0] : Arrays.stream(values).collect(Collectors.joining(",")));
        }

        static HeaderMapping valueOf(final String header, final String targetProperty) {
            Encoding encoding = getEncoding(targetProperty);
            String ldapAttribute = removePrefix(stripEncoding(targetProperty));
            return new HeaderMapping(header, ldapAttribute, encoding, targetProperty);
        }

        static String stripEncoding(String targetProperty) {
            final int index = targetProperty.indexOf(':');
            if (index > -1) {
                targetProperty = targetProperty.substring(index + 1);
            }
            return targetProperty;
        }

        static Encoding getEncoding(String targetProperty) {
            final int index = targetProperty.indexOf(':');
            if (index > -1) {
                String encName = targetProperty.substring(0, index);
                try {
                    return Encoding.valueOf(encName.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid encoding '" + encName + "' for " + targetProperty
                            + ". expected one of: " + Arrays.stream(Encoding.values())
                                    .map(enc -> enc.toString().toLowerCase()).collect(Collectors.joining(",")),
                            e);
                }
            }
            return Encoding.NONE;
        }

        private static String removePrefix(String targetProperty) {
            if (targetProperty.startsWith("org.seeAlso."))
                return targetProperty.substring("org.seeAlso.".length());
            if (targetProperty.startsWith("org."))
                return targetProperty.substring("org.".length());
            if (targetProperty.startsWith("manager."))
                return targetProperty.substring("manager.".length());
            return targetProperty;
        }

        public @Override String toString() {
            return format("%s=%s", this.headerName, this.fullPropertyName);
        }
    }

    public static @Value class HeaderMappings {
        List<HeaderMapping> userHeaders;
        List<HeaderMapping> userManagerHeaders;
        List<HeaderMapping> orgHeaders;
        List<HeaderMapping> orgExtensionHeaders;

        static HeaderMappings valueOf(Map<String, String> rawMappings) {
            List<HeaderMapping> user = valueOf(rawMappings, LdapHeaderMappings::isUserProperty);
            List<HeaderMapping> manager = valueOf(rawMappings, LdapHeaderMappings::isManagerProperty);
            List<HeaderMapping> org = valueOf(rawMappings, LdapHeaderMappings::isOrgProperty);
            List<HeaderMapping> orgExt = valueOf(rawMappings, LdapHeaderMappings::isOrgExtProperty);
            return new HeaderMappings(user, manager, org, orgExt);
        }

        private static List<HeaderMapping> valueOf(Map<String, String> rawMappings,
                Predicate<Map.Entry<String, String>> filter) {
            return rawMappings.entrySet().stream()//
                    .filter(filter)//
                    .map(HeaderMappings::toMapping)//
                    .collect(ImmutableList.toImmutableList());
        }

        private static HeaderMapping toMapping(Map.Entry<String, String> entry) {
            return HeaderMapping.valueOf(entry.getKey(), entry.getValue());
        }

        public List<HeaderMapping> all() {
            List<HeaderMapping> all = new ArrayList<>(userHeaders);
            all.addAll(userManagerHeaders);
            all.addAll(orgHeaders);
            all.addAll(orgExtensionHeaders);
            return all;
        }

        Map<String, String> toMap() {
            Map<String, String> map = new HashMap<>();
            getUserHeaders().forEach(m -> map.put(m.getHeaderName(), m.getFullPropertyName()));
            getOrgHeaders().forEach(m -> map.put(m.getHeaderName(), m.getFullPropertyName()));
            getOrgExtensionHeaders().forEach(m -> map.put(m.getHeaderName(), m.getFullPropertyName()));
            getUserManagerHeaders().forEach(m -> map.put(m.getHeaderName(), m.getFullPropertyName()));
            return map;
        }

    }

    static boolean isGlobalHeader(Map.Entry<String, String> entry) {
        String headerName = entry.getKey();
        return headerName.indexOf('.') == -1;
    }

    static boolean isServiceSpecificHeader(Map.Entry<String, String> entry) {
        if (isGlobalHeader(entry))
            return false;
        String headerName = entry.getKey();
        if (headerName.indexOf('.') < 1 || headerName.indexOf('.') != headerName.lastIndexOf('.')) {
            throw new IllegalArgumentException(
                    "Invalid header name, expected '<header-name>' or '<service>.<header-name>', got '" + headerName
                            + "'");
        }
        return true;
    }

    private static boolean isUserProperty(Map.Entry<String, String> entry) {
        String property = entry.getValue();
        return property.indexOf('.') == -1;
    }

    private static boolean isManagerProperty(Map.Entry<String, String> entry) {
        String property = entry.getValue();
        return HeaderMapping.stripEncoding(property).startsWith("manager.");
    }

    private static boolean isOrgProperty(Map.Entry<String, String> entry) {
        String property = entry.getValue();
        return !isOrgExtProperty(entry) && HeaderMapping.stripEncoding(property).startsWith("org.");
    }

    private static boolean isOrgExtProperty(Map.Entry<String, String> entry) {
        String property = entry.getValue();
        return HeaderMapping.stripEncoding(property).startsWith("org.seeAlso.");
    }
}
