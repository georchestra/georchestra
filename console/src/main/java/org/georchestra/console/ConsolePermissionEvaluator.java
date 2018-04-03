package org.georchestra.console;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.console.ws.backoffice.roles.RolesController;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

public class ConsolePermissionEvaluator implements PermissionEvaluator {

	private static final Log LOG = LogFactory.getLog(RolesController.class.getName());

	public ConsolePermissionEvaluator() {
		LOG.info("consolePermissionEvaluator bean initialized");
	}
	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
		LOG.info("ConsolePermissionEvaluator: Here");
		return false;
	}

	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
			Object permission) {
		LOG.info("ConsolePermissionEvaluator: Here");
		return false;
	}

}
