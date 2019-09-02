package org.georchestra.console.ws.editorgdetails;

import lombok.Data;

final @Data class EditOrgDetailsFormBean implements java.io.Serializable {
    private static final long serialVersionUID = -5836489312467203512L;
    private String id;
    private String name;
    private String shortName;
    private String description;
    private String address;
    private String url;
    private String orgType;
}
