package org.georchestra.datafeeder.autoconf;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.georchestra.datafeeder.autoconf.GeorchestraNameNormalizer;
import org.junit.Before;
import org.junit.Test;

public class GeorchestraNameNormalizerTest {

    private GeorchestraNameNormalizer normalizationService;

    public @Before void setUp() throws IOException {
        normalizationService = new GeorchestraNameNormalizer();
    }

    @Test
    public void testSchemaName() {
        assertEquals("psc", normalizationService.resolveDatabaseSchemaName("PSC"));
        assertEquals("projectcommiteeschema", normalizationService.resolveDatabaseSchemaName("ProjectCommiteeSchema"));
        assertEquals("hellome", normalizationService.resolveDatabaseSchemaName("Hello Me"));
    }
}
