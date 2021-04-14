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
package org.georchestra.datafeeder.batch.analysis;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.repository.DatasetUploadStateRepository;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class DatasetUploadStateItemReader implements ItemReader<DatasetUploadState> {

    private @Value("#{jobParameters['uploadId']}") UUID uploadId;
    private @Autowired DatasetUploadStateRepository repository;
    private Iterator<DatasetUploadState> iterator;

    public @Override synchronized DatasetUploadState read() throws UnexpectedInputException {
        if (iterator == null) {
            List<DatasetUploadState> all = repository.findAllByJobId(uploadId);
            this.iterator = all.iterator();
        }
        return iterator.hasNext() ? iterator.next() : null;
    }
}