package org.georchestra.security.model;

import java.io.Serializable;

import lombok.Data;

public @Data class Role implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Uniquely identifies a role. Format could be UUID, but it's at the discretion
     * of the provider
     */
    private String id;
    /**
     * Role name, provides a unique identity for the role, but can mutate over time
     */
    private String name;
    /**
     * Role intended purpose
     */
    private String description;
}
