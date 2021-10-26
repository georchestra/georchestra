package org.georchestra.console.integration.instruments;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockRandomUidUserSecurityContextFactory.class)
public @interface WithMockRandomUidUser {
}
