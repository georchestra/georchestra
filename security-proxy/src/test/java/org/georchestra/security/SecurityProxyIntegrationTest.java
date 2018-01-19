package org.georchestra.security;

import org.junit.runner.RunWith;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext-test.xml" })
public class SecurityProxyIntegrationTest {

    @Test
    public void test() {
        // If you can reach this code, everything went fine :-)
        // (and transitively, should go fine at runtime also)
        assertTrue(true);
    }

}
