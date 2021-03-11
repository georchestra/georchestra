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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.annotations.ApiModelProperty;

/**
 * Setting
 */

public class Setting {
    /**
     * Gets or Sets dataType
     */
    public enum DataTypeEnum {
        STRING("STRING"),

        INT("INT"),

        BOOLEAN("BOOLEAN"),

        JSON("JSON");

        private String value;

        DataTypeEnum(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static DataTypeEnum fromValue(String text) {
            for (DataTypeEnum b : DataTypeEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    @JsonProperty("dataType")
    private DataTypeEnum dataType = null;

    @JsonProperty("internal")
    private Boolean internal = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("position")
    private Integer position = null;

    @JsonProperty("value")
    private String value = null;

    public Setting dataType(DataTypeEnum dataType) {
        this.dataType = dataType;
        return this;
    }

    /**
     * Get dataType
     * 
     * @return dataType
     **/
    @ApiModelProperty(value = "")
    public DataTypeEnum getDataType() {
        return dataType;
    }

    public void setDataType(DataTypeEnum dataType) {
        this.dataType = dataType;
    }

    public Setting internal(Boolean internal) {
        this.internal = internal;
        return this;
    }

    /**
     * Get internal
     * 
     * @return internal
     **/
    @ApiModelProperty(value = "")
    public Boolean isInternal() {
        return internal;
    }

    public void setInternal(Boolean internal) {
        this.internal = internal;
    }

    public Setting name(String name) {
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

    public Setting position(Integer position) {
        this.position = position;
        return this;
    }

    /**
     * Get position
     * 
     * @return position
     **/
    @ApiModelProperty(value = "")
    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Setting value(String value) {
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
        Setting setting = (Setting) o;
        return Objects.equals(this.dataType, setting.dataType) && Objects.equals(this.internal, setting.internal)
                && Objects.equals(this.name, setting.name) && Objects.equals(this.position, setting.position)
                && Objects.equals(this.value, setting.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataType, internal, name, position, value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Setting {\n");

        sb.append("    dataType: ").append(toIndentedString(dataType)).append("\n");
        sb.append("    internal: ").append(toIndentedString(internal)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    position: ").append(toIndentedString(position)).append("\n");
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
