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

package org.georchestra.datafeeder.batch;

import org.georchestra.datafeeder.batch.analysis.UploadAnalysisJobConfiguration;
import org.georchestra.datafeeder.batch.publish.DataPublishingJobConfiguration;
import org.georchestra.datafeeder.batch.service.BatchServicesConfiguration;
import org.georchestra.datafeeder.model.UserInfo;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableBatchProcessing
@Import({ BatchServicesConfiguration.class, UploadAnalysisJobConfiguration.class,
        DataPublishingJobConfiguration.class })
public class DatafeederBatchConfiguration {

    private @Autowired ObjectMapper objectMapper;

    /**
     * {@link UserInfo} property editor to allow establishing a user as a job
     * parameter and read it in steps through
     * <code> @Value("#{jobParameters['user']}") UserInfo user</code>
     */
    @Bean
    public UserInfoPropertyEditor userInfoPropertyEditor() {
        return new UserInfoPropertyEditor(objectMapper);
    }

}