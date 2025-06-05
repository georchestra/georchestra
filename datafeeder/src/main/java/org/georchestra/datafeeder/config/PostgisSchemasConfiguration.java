/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
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

package org.georchestra.datafeeder.config;

import lombok.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(exclude = "includesBySchema")
public class PostgisSchemasConfiguration implements InitializingBean {

    public static final String EMPTY_PREFIX = "";

    private static final String ALL_SCHEMAS_WILDCARD = "*";

    private static final Set<String> HARD_SCHEMA_EXCLUDES = Set.of("pg_toast", "pg_catalog", "information_schema",
            "topology", "tiger");

    private String delimiter = ":";

    /**
     * Map of schema names to include and configuration on whether to prefix table
     * names with the schema name, and in that case, an optional schema name alias.
     */
    private List<SchemaConfiguration> include = List.of(SchemaConfiguration.all());

    /**
     * List of PostgreSQL schema names to exclude from automatic FeatureType
     * discovery. The exclude list takes precedence over includes.
     */
    private List<String> exclude = List.of();

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Map<String, SchemaConfiguration> includesBySchema;

    private Map<String, SchemaConfiguration> includesBySchema() {
        if (null == includesBySchema) {
            includesBySchema = include.stream()
                    .collect(Collectors.toMap(SchemaConfiguration::getSchema, Function.identity()));
        }
        return includesBySchema;
    }

    @Override
    public void afterPropertiesSet() {
        validate();
    }

    @Data
    public static class SchemaConfiguration {
        private String schema;
        private boolean prefixTables = true;
        private String alias;

        public SchemaConfiguration withSchema(@NonNull String schema) {
            SchemaConfiguration config = new SchemaConfiguration();
            config.setSchema(schema);
            config.setPrefixTables(prefixTables);
            config.setAlias(alias);
            return config;
        }

        public static SchemaConfiguration all() {
            SchemaConfiguration all = new SchemaConfiguration();
            all.setSchema(ALL_SCHEMAS_WILDCARD);
            all.setPrefixTables(true);
            all.setAlias(null);
            return all;
        }
    }

    public Optional<String> alias(String schema) {
        String alias = null;
        SchemaConfiguration schemaConfig = configForSchema(schema);
        if (schemaConfig.isPrefixTables()) {
            alias = schemaConfig.getAlias();
        }
        return Optional.ofNullable(alias);
    }

    private boolean shouldPrefix(String schema) {
        return configForSchema(schema).isPrefixTables();
    }

    @NonNull
    private SchemaConfiguration configForSchema(String schema) {
        SchemaConfiguration schemaConfig = includesBySchema().get(schema);
        if (null == schemaConfig) {
            schemaConfig = includesBySchema().get(ALL_SCHEMAS_WILDCARD);
            if (null == schemaConfig) {
                schemaConfig = new SchemaConfiguration();
                schemaConfig.setSchema(schema);
            } else {
                schemaConfig = schemaConfig.withSchema(schema);
            }
        }
        return schemaConfig;
    }

    public String prefix(String postgresSchema) {
        String prefix = alias(postgresSchema).orElseGet(() -> shouldPrefix(postgresSchema) ? postgresSchema : null);
        return Optional.ofNullable(prefix).map(pre -> pre + delimiter).orElse(EMPTY_PREFIX);
    }

    private void validate() {
        var bySchema = includesBySchema();
        SchemaConfiguration defaults = bySchema.get(ALL_SCHEMAS_WILDCARD);
        Assert.isTrue(null == defaults || defaults.isPrefixTables(),
                "The '*' schema wildcard can't have prefix-tables=false");

        List<String> unprefixing = bySchema.values().stream().filter(c -> !c.isPrefixTables())
                .map(SchemaConfiguration::getSchema).collect(Collectors.toList());

        Assert.isTrue(unprefixing.size() < 2, String.format("Multiple schemas configured with prefix-tables=false: %s",
                unprefixing.stream().sorted().collect(Collectors.joining(","))));
    }

}
