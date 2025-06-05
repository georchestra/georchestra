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

import java.beans.PropertyEditorSupport;
import java.io.IOException;

import org.georchestra.datafeeder.model.UserInfo;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserInfoPropertyEditor extends PropertyEditorSupport {
    private final @NonNull ObjectMapper mapper;

    @Override
    public UserInfo getValue() {
        return (UserInfo) super.getValue();
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        UserInfo user = null;
        if (StringUtils.hasText(text)) {
            try {
                user = mapper.readerFor(UserInfo.class).readValue(text, UserInfo.class);
            } catch (IOException e) {
                throw new IllegalArgumentException("Error parsing JSON UserInfo", e);
            }
        }
        setValue(user);
    }

    @Override
    public String getAsText() {
        UserInfo user = getValue();
        try {
            return user == null ? null : mapper.writeValueAsString(user);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Error encoding UserInfo as JSON", e);
        }
    }
}