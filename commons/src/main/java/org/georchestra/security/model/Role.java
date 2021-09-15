package org.georchestra.security.model;

import java.io.Serializable;

import lombok.Data;

public @Data class Role implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String description;
}
