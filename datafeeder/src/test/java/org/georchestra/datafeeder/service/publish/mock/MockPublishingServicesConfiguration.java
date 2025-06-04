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

package org.georchestra.datafeeder.service.publish.mock;

import org.georchestra.datafeeder.service.publish.DataBackendService;
import org.georchestra.datafeeder.service.publish.MetadataPublicationService;
import org.georchestra.datafeeder.service.publish.OWSPublicationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MockPublishingServicesConfiguration {

    @ConditionalOnMissingBean(DataBackendService.class)
    public @Bean DataBackendService dataBackendService() {
        return new MockDataBackendService();
    }

    @ConditionalOnMissingBean(OWSPublicationService.class)
    public @Bean OWSPublicationService owsPublicationService() {
        return new MockOWSPublicationService();
    }

    @ConditionalOnMissingBean(MetadataPublicationService.class)
    public @Bean MetadataPublicationService metadataPublicationService() {
        return new MockMetadataPublicationService();
    }
}
