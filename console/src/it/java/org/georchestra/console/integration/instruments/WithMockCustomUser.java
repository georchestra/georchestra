package org.georchestra.console.integration.instruments;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(
        factory = WithMockCustomUserSecurityContextFactory.class
)
public @interface WithMockCustomUser {
}
