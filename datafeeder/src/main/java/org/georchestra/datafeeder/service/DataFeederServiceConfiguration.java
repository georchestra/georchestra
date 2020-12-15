/*
 * Copyright (C) 2020 by the geOrchestra PSC
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
package org.georchestra.datafeeder.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.georchestra.datafeeder.config.DataFeederConfigurationProperties;
import org.georchestra.datafeeder.config.DataFeederConfigurationProperties.FileUploadConfig;
import org.georchestra.datafeeder.service.batch.DatafeederBatchConfiguration;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.factory.BeanExpressionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Import(value = { DatafeederBatchConfiguration.class })
@Slf4j
public class DataFeederServiceConfiguration {

    private @Autowired DataFeederConfigurationProperties properties;

    public @Bean FileStorageService fileStorageService() {
        FileUploadConfig fileUploadConfig = properties.getFileUpload();
        Path baseDirectory = fileUploadConfig.getPersistentLocation();
        if (null == baseDirectory || baseDirectory.toString().isEmpty()) {
            throw new InvalidPropertyException(DataFeederConfigurationProperties.class,
                    "file-upload.persistent-location", "persitent file upload directory not provided");
        }
        if (!Files.isDirectory(baseDirectory)) {
            log.warn(
                    "Upload files directory does not exist, creating from property datafeeder.file-upload.persistent-location={}",
                    baseDirectory);
            try {
                Files.createDirectories(baseDirectory);
            } catch (IOException e) {
                String msg = String.format(
                        "Unable to create upload files directory from property datafeeder.file-upload.persistent-location=%s",
                        baseDirectory);
                throw new BeanExpressionException(msg, e);
            }
        }
        return new FileStorageService(baseDirectory);
    }

    public @Bean DataUploadService dataUploadService() {
        return new DataUploadService();
    }

    public @Bean DatasetsService datasetsService() {
        return new DatasetsService();
    }
}
