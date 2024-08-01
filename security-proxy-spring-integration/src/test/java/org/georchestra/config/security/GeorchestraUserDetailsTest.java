package org.georchestra.config.security;

import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;

public class GeorchestraUserDetailsTest {

    public @Test void testAvoidPrefixingRolesTwice() {
        GeorchestraSecurityProxyAuthenticationManager manager = new GeorchestraSecurityProxyAuthenticationManager();
        GeorchestraUserDetails det = GeorchestraUserDetails.fromHeaders(Map.of("sec-organization",
                "{base64}ewogICJpZCI6ICI4YzFlZjg3YS03M2ZjLTRkNzktODBjYi1iYTRmZjcxMD"
                        + "JjY2EiLAogICJzaG9ydE5hbWUiOiAiQzJDIiwKICAibmFtZSI6ICJDYW1wdG9jYW1wIiwKICAibGlua2FnZSI"
                        + "6ICJodHRwczovL3d3dy5jYW1wdG9jYW1wLmNvbS8iLAogICJwb3N0YWxBZGRyZXNzIjogIjE4IFJ1ZSBkdSBs"
                        + "YWMgU2FpbnQgQW5kcsOpLCA3MzAwMCBDaGFtYsOpcnkiLAogICJjYXRlZ29yeSI6ICJBdXRyZSIsCiAgImRlc"
                        + "2NyaXB0aW9uIjogIkNhbXB0b2NhbXAgU0FTIEZyYW5jZSIsCiAgIm5vdGVzIjogIkludGVybmFsIENSTSBub3"
                        + "RlcyBvbiBDYW1wdG9jYW1wIiwKICAibWFpbCI6ICJpbmZvQGNhbXB0b2NhbXAuY29tIiwKICAibGFzdFVwZGF"
                        + "0ZWQiOiAiOTliMzkwMzZjYjc2Njc2NGZkOGM2ZmIzY2JlMzcyYjMwMTZiNTAzZjYxNmRjNjZjZTg1M2Q1Mzcx"
                        + "OWE0ZGZkNSIsCiAgIm1lbWJlcnMiOiBbCiAgICAgInRlc3RhZG1pbiIKICBdCn0K", // c2c
                "sec-user",
                "{base64}ewogICJ1c2VybmFtZSI6ICJ0ZXN0YWRtaW4iLAogICJyb2xlcyI6IFsKICAgICJST0xFX1NVU"
                        + "EVSVVNFUiIsCiAgICAiUk9MRV9HTl9BRE1JTiIsCiAgICAiUk9MRV9VU0VSIiwKICAgICJST0xFX0FETUlOSV"
                        + "NUUkFUT1IiLAogICAgIlJPTEVfSU1QT1JUIgogIF0sCiAgIm9yZ2FuaXphdGlvbiI6ICJDMkMiLAogICJpZCI"
                        + "6ICJkNWYxNDkxYS1mNWY4LTQ5OTgtYWU1MS03NmNmZjM4ZDUwMGIiLAogICJsYXN0VXBkYXRlZCI6ICI3NWNl"
                        + "MzcyOTk5NTliYTVkYThiMWE1MDQ5OWMzZTAxMDYxODA0N2Q1ZmRiMGUxMjE1MjM1MmZmNDNkNGQ1MWNhIiwKI"
                        + "CAiZmlyc3ROYW1lIjogIlRlc3QiLAogICJsYXN0TmFtZSI6ICJBZG1pbiIsCiAgImVtYWlsIjogInBzYyt0ZX"
                        + "N0YWRtaW5AZ2VvcmNoZXN0cmEub3JnIiwKICAidGVsZXBob25lTnVtYmVyIjogIiszMzYxMjM0NTY3OCIsCiA"
                        + "gImxkYXBXYXJuIjogZmFsc2UsCiAgImlzRXh0ZXJuYWxBdXRoIjogZmFsc2UKfQo=") // testadmin
        );

        List<String> roles = det.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        roles.stream().forEach(r -> assertFalse(r.startsWith("ROLE_ROLE_")));
    }
}
