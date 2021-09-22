package org.georchestra.security.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * List of {@link GeorchestraUser#getUsername() user names} that belong to this
     * role
     */
    private List<String> members = new ArrayList<>();

    public void setMembers(List<String> members) {
        this.members = members == null ? new ArrayList<>() : members;
    }

}
