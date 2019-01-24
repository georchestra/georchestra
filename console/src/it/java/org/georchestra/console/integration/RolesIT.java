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
package org.georchestra.console.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Integration tests for {@code /private/roles} role management API.
 */
@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:/webmvc-config-test.xml" })
public class RolesIT {
	private static Logger LOGGER = Logger.getLogger(RolesIT.class);

	public @Rule @Autowired IntegrationTestSupport support;

	private String roleName;

	private void deleteQuiet() {
		try {
			delete();
		} catch (Exception e) {
			LOGGER.info(String.format("Error deleting role %s at %s", roleName, support.testName()), e);
		}
	}

	private MvcResult delete() throws Exception {
		return delete(roleName).andReturn();
	}

	private ResultActions delete(String roleName) throws Exception {
		return support.perform(MockMvcRequestBuilders.delete("/private/roles/{cn}", roleName));
	}

	private ResultActions create() throws Exception {
		roleName = "IT_ROLE_" + RandomStringUtils.randomAlphabetic(8).toUpperCase();
		return create(roleName);
	}

	private ResultActions create(String name) throws Exception {
		String body = "{ \"cn\": \"" + name + "\", \"description\": \"Role Description\", \"isFavorite\": false }";
		return support.perform(post("/private/roles").content(body));
	}

	private ResultActions update(String name, String description, boolean isFavorite) throws Exception {
		String body = "{ \"cn\": \"" + name + "\", \"description\": \"" + description + "\", \"isFavorite\": "
				+ isFavorite + " }";
		return support.perform(put("/private/roles/{cn}", name).content(body));
	}

	private ResultActions get(String name) throws Exception {
		return support.perform(MockMvcRequestBuilders.get("/private/roles/{cn}", name));
	}

	@WithMockUser(username = "user", roles = "USER")
	public @Test void testCreateBadUser() throws Exception {
		create().andExpect(status().isForbidden());
	}

	@WithMockUser(username = "admin", roles = "SUPERUSER")
	public @Test void testCreate() throws Exception {
		try {
			create()//
					.andExpect(status().isOk())// note: should return 201:CREATED instead?
					.andExpect(content().contentTypeCompatibleWith("application/json"))//
					.andExpect(jsonPath("$.cn").value(roleName));
		} finally {
			deleteQuiet();
		}
	}

	@WithMockUser(username = "admin", roles = "SUPERUSER")
	public @Test void testUpdateIsFavoriteNoOp() throws Exception {
		try {
			create().andExpect(status().isOk());

			update(roleName, "", false)//
					.andExpect(status().isOk())//
					.andExpect(content().contentTypeCompatibleWith("application/json"))//
					.andExpect(jsonPath("$.isFavorite").value(false));

			get(roleName)// update says it was updated, but what does get say?
					.andExpect(status().isOk())//
					.andExpect(content().contentTypeCompatibleWith("application/json"))//
					.andExpect(jsonPath("$.isFavorite").value(false));
		} finally {
			deleteQuiet();
		}
	}

	@WithMockUser(username = "admin", roles = "SUPERUSER")
	public @Test void testUpdateIsFavorite() throws Exception {
		try {
			create().andExpect(status().isOk());

			update(roleName, "", true)//
					.andExpect(status().isOk())//
					.andExpect(content().contentTypeCompatibleWith("application/json"))//
					.andExpect(jsonPath("$.isFavorite").value(true));

			get(roleName)// update says it was updated, but what does get say?
					.andExpect(status().isOk())//
					.andExpect(content().contentTypeCompatibleWith("application/json"))//
					.andExpect(jsonPath("$.isFavorite").value(true));

			update(roleName, "", false)//
					.andExpect(status().isOk())//
					.andExpect(content().contentTypeCompatibleWith("application/json"))//
					.andExpect(jsonPath("$.isFavorite").value(false));

			get(roleName)// update says it was updated, but what does get say?
					.andExpect(status().isOk())//
					.andExpect(content().contentTypeCompatibleWith("application/json"))//
					.andExpect(jsonPath("$.isFavorite").value(false));
		} finally {
			deleteQuiet();
		}
	}
}
