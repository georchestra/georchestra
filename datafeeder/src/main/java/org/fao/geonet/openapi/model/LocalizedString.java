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
 * LocalizedString
 */

public class LocalizedString {
    @JsonProperty("href")
    private String href = null;

    @JsonProperty("lang")
    private String lang = null;

    @JsonProperty("value")
    private String value = null;

    public LocalizedString href(String href) {
        this.href = href;
        return this;
    }

    /**
     * Get href
     * 
     * @return href
     **/
    @ApiModelProperty(value = "")
    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public LocalizedString lang(String lang) {
        this.lang = lang;
        return this;
    }

    /**
     * Get lang
     * 
     * @return lang
     **/
    @ApiModelProperty(value = "")
    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public LocalizedString value(String value) {
        this.value = value;
        return this;
    }

    /**
     * Get value
     * 
     * @return value
     **/
    @ApiModelProperty(value = "")
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LocalizedString localizedString = (LocalizedString) o;
        return Objects.equals(this.href, localizedString.href) && Objects.equals(this.lang, localizedString.lang)
                && Objects.equals(this.value, localizedString.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(href, lang, value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LocalizedString {\n");

        sb.append("    href: ").append(toIndentedString(href)).append("\n");
        sb.append("    lang: ").append(toIndentedString(lang)).append("\n");
        sb.append("    value: ").append(toIndentedString(value)).append("\n");
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
