package org.georchestra.security;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(locations = { "classpath:applicationContext-test.xml" })
public class SecurityProxyIntegrationTest {

    @Test
    public void test() {
        // If you can reach this code, everything went fine :-)
        // (and transitively, should go fine at runtime also)
        assertTrue(true);
    }

}
