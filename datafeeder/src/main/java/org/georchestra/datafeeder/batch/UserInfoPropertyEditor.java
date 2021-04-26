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