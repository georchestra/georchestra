/*
 * GeoNetwork Api Documentation (beta)
 * Learn how to access the catalog using the GeoNetwork REST API.
 *
 * OpenAPI spec version: 0.1
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package org.fao.geonet.openapi.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * MapServer
 */

public class MapServer {
    @JsonProperty("configurl")
    private String configurl = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("id")
    private Integer id = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("namespace")
    private String namespace = null;

    @JsonProperty("namespacePrefix")
    private String namespacePrefix = null;

    @JsonProperty("password")
    private String password = null;

    @JsonProperty("pushStyleInWorkspace")
    private Boolean pushStyleInWorkspace = null;

    @JsonProperty("pushStyleInWorkspace_JpaWorkaround")
    private String pushStyleInWorkspaceJpaWorkaround = null;

    @JsonProperty("stylerurl")
    private String stylerurl = null;

    @JsonProperty("username")
    private String username = null;

    @JsonProperty("wcsurl")
    private String wcsurl = null;

    @JsonProperty("wfsurl")
    private String wfsurl = null;

    @JsonProperty("wmsurl")
    private String wmsurl = null;

    public MapServer configurl(String configurl) {
        this.configurl = configurl;
        return this;
    }

    /**
     * Get configurl
     * 
     * @return configurl
     **/
    @ApiModelProperty(value = "")
    public String getConfigurl() {
        return configurl;
    }

    public void setConfigurl(String configurl) {
        this.configurl = configurl;
    }

    public MapServer description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Get description
     * 
     * @return description
     **/
    @ApiModelProperty(value = "")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MapServer id(Integer id) {
        this.id = id;
        return this;
    }

    /**
     * Get id
     * 
     * @return id
     **/
    @ApiModelProperty(value = "")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public MapServer name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get name
     * 
     * @return name
     **/
    @ApiModelProperty(value = "")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MapServer namespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    /**
     * Get namespace
     * 
     * @return namespace
     **/
    @ApiModelProperty(value = "")
    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public MapServer namespacePrefix(String namespacePrefix) {
        this.namespacePrefix = namespacePrefix;
        return this;
    }

    /**
     * Get namespacePrefix
     * 
     * @return namespacePrefix
     **/
    @ApiModelProperty(value = "")
    public String getNamespacePrefix() {
        return namespacePrefix;
    }

    public void setNamespacePrefix(String namespacePrefix) {
        this.namespacePrefix = namespacePrefix;
    }

    public MapServer password(String password) {
        this.password = password;
        return this;
    }

    /**
     * Get password
     * 
     * @return password
     **/
    @ApiModelProperty(value = "")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public MapServer pushStyleInWorkspace(Boolean pushStyleInWorkspace) {
        this.pushStyleInWorkspace = pushStyleInWorkspace;
        return this;
    }

    /**
     * Get pushStyleInWorkspace
     * 
     * @return pushStyleInWorkspace
     **/
    @ApiModelProperty(value = "")
    public Boolean isPushStyleInWorkspace() {
        return pushStyleInWorkspace;
    }

    public void setPushStyleInWorkspace(Boolean pushStyleInWorkspace) {
        this.pushStyleInWorkspace = pushStyleInWorkspace;
    }

    public MapServer pushStyleInWorkspaceJpaWorkaround(String pushStyleInWorkspaceJpaWorkaround) {
        this.pushStyleInWorkspaceJpaWorkaround = pushStyleInWorkspaceJpaWorkaround;
        return this;
    }

    /**
     * Get pushStyleInWorkspaceJpaWorkaround
     * 
     * @return pushStyleInWorkspaceJpaWorkaround
     **/
    @ApiModelProperty(value = "")
    public String getPushStyleInWorkspaceJpaWorkaround() {
        return pushStyleInWorkspaceJpaWorkaround;
    }

    public void setPushStyleInWorkspaceJpaWorkaround(String pushStyleInWorkspaceJpaWorkaround) {
        this.pushStyleInWorkspaceJpaWorkaround = pushStyleInWorkspaceJpaWorkaround;
    }

    public MapServer stylerurl(String stylerurl) {
        this.stylerurl = stylerurl;
        return this;
    }

    /**
     * Get stylerurl
     * 
     * @return stylerurl
     **/
    @ApiModelProperty(value = "")
    public String getStylerurl() {
        return stylerurl;
    }

    public void setStylerurl(String stylerurl) {
        this.stylerurl = stylerurl;
    }

    public MapServer username(String username) {
        this.username = username;
        return this;
    }

    /**
     * Get username
     * 
     * @return username
     **/
    @ApiModelProperty(value = "")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public MapServer wcsurl(String wcsurl) {
        this.wcsurl = wcsurl;
        return this;
    }

    /**
     * Get wcsurl
     * 
     * @return wcsurl
     **/
    @ApiModelProperty(value = "")
    public String getWcsurl() {
        return wcsurl;
    }

    public void setWcsurl(String wcsurl) {
        this.wcsurl = wcsurl;
    }

    public MapServer wfsurl(String wfsurl) {
        this.wfsurl = wfsurl;
        return this;
    }

    /**
     * Get wfsurl
     * 
     * @return wfsurl
     **/
    @ApiModelProperty(value = "")
    public String getWfsurl() {
        return wfsurl;
    }

    public void setWfsurl(String wfsurl) {
        this.wfsurl = wfsurl;
    }

    public MapServer wmsurl(String wmsurl) {
        this.wmsurl = wmsurl;
        return this;
    }

    /**
     * Get wmsurl
     * 
     * @return wmsurl
     **/
    @ApiModelProperty(value = "")
    public String getWmsurl() {
        return wmsurl;
    }

    public void setWmsurl(String wmsurl) {
        this.wmsurl = wmsurl;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MapServer mapServer = (MapServer) o;
        return Objects.equals(this.configurl, mapServer.configurl)
                && Objects.equals(this.description, mapServer.description) && Objects.equals(this.id, mapServer.id)
                && Objects.equals(this.name, mapServer.name) && Objects.equals(this.namespace, mapServer.namespace)
                && Objects.equals(this.namespacePrefix, mapServer.namespacePrefix)
                && Objects.equals(this.password, mapServer.password)
                && Objects.equals(this.pushStyleInWorkspace, mapServer.pushStyleInWorkspace)
                && Objects.equals(this.pushStyleInWorkspaceJpaWorkaround, mapServer.pushStyleInWorkspaceJpaWorkaround)
                && Objects.equals(this.stylerurl, mapServer.stylerurl)
                && Objects.equals(this.username, mapServer.username) && Objects.equals(this.wcsurl, mapServer.wcsurl)
                && Objects.equals(this.wfsurl, mapServer.wfsurl) && Objects.equals(this.wmsurl, mapServer.wmsurl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configurl, description, id, name, namespace, namespacePrefix, password,
                pushStyleInWorkspace, pushStyleInWorkspaceJpaWorkaround, stylerurl, username, wcsurl, wfsurl, wmsurl);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MapServer {\n");

        sb.append("    configurl: ").append(toIndentedString(configurl)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    namespace: ").append(toIndentedString(namespace)).append("\n");
        sb.append("    namespacePrefix: ").append(toIndentedString(namespacePrefix)).append("\n");
        sb.append("    password: ").append(toIndentedString(password)).append("\n");
        sb.append("    pushStyleInWorkspace: ").append(toIndentedString(pushStyleInWorkspace)).append("\n");
        sb.append("    pushStyleInWorkspaceJpaWorkaround: ").append(toIndentedString(pushStyleInWorkspaceJpaWorkaround))
                .append("\n");
        sb.append("    stylerurl: ").append(toIndentedString(stylerurl)).append("\n");
        sb.append("    username: ").append(toIndentedString(username)).append("\n");
        sb.append("    wcsurl: ").append(toIndentedString(wcsurl)).append("\n");
        sb.append("    wfsurl: ").append(toIndentedString(wfsurl)).append("\n");
        sb.append("    wmsurl: ").append(toIndentedString(wmsurl)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}
