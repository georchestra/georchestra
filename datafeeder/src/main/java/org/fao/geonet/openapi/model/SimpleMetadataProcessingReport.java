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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * SimpleMetadataProcessingReport
 */

public class SimpleMetadataProcessingReport {
    @JsonProperty("ellapsedTimeInSeconds")
    private Long ellapsedTimeInSeconds = null;

    @JsonProperty("endIsoDateTime")
    private String endIsoDateTime = null;

    @JsonProperty("errors")
    private List<Report> errors = null;

    @JsonProperty("infos")
    private List<InfoReport> infos = null;

    @JsonProperty("metadata")
    private List<Integer> metadata = null;

    @JsonProperty("metadataErrors")
    private Map<String, List<Report>> metadataErrors = null;

    @JsonProperty("metadataInfos")
    private Map<String, List<InfoReport>> metadataInfos = null;

    @JsonProperty("numberOfNullRecords")
    private Integer numberOfNullRecords = null;

    @JsonProperty("numberOfRecordNotFound")
    private Integer numberOfRecordNotFound = null;

    @JsonProperty("numberOfRecords")
    private Integer numberOfRecords = null;

    @JsonProperty("numberOfRecordsNotEditable")
    private Integer numberOfRecordsNotEditable = null;

    @JsonProperty("numberOfRecordsProcessed")
    private Integer numberOfRecordsProcessed = null;

    @JsonProperty("numberOfRecordsWithErrors")
    private Integer numberOfRecordsWithErrors = null;

    @JsonProperty("running")
    private Boolean running = null;

    @JsonProperty("startIsoDateTime")
    private String startIsoDateTime = null;

    @JsonProperty("totalTimeInSeconds")
    private Long totalTimeInSeconds = null;

    @JsonProperty("type")
    private String type = null;

    @JsonProperty("uuid")
    private String uuid = null;

    public SimpleMetadataProcessingReport ellapsedTimeInSeconds(Long ellapsedTimeInSeconds) {
        this.ellapsedTimeInSeconds = ellapsedTimeInSeconds;
        return this;
    }

    /**
     * Get ellapsedTimeInSeconds
     * 
     * @return ellapsedTimeInSeconds
     **/
    @ApiModelProperty(value = "")
    public Long getEllapsedTimeInSeconds() {
        return ellapsedTimeInSeconds;
    }

    public void setEllapsedTimeInSeconds(Long ellapsedTimeInSeconds) {
        this.ellapsedTimeInSeconds = ellapsedTimeInSeconds;
    }

    public SimpleMetadataProcessingReport endIsoDateTime(String endIsoDateTime) {
        this.endIsoDateTime = endIsoDateTime;
        return this;
    }

    /**
     * Get endIsoDateTime
     * 
     * @return endIsoDateTime
     **/
    @ApiModelProperty(value = "")
    public String getEndIsoDateTime() {
        return endIsoDateTime;
    }

    public void setEndIsoDateTime(String endIsoDateTime) {
        this.endIsoDateTime = endIsoDateTime;
    }

    public SimpleMetadataProcessingReport errors(List<Report> errors) {
        this.errors = errors;
        return this;
    }

    public SimpleMetadataProcessingReport addErrorsItem(Report errorsItem) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(errorsItem);
        return this;
    }

    /**
     * Get errors
     * 
     * @return errors
     **/
    @ApiModelProperty(value = "")
    public List<Report> getErrors() {
        return errors;
    }

    public void setErrors(List<Report> errors) {
        this.errors = errors;
    }

    public SimpleMetadataProcessingReport infos(List<InfoReport> infos) {
        this.infos = infos;
        return this;
    }

    public SimpleMetadataProcessingReport addInfosItem(InfoReport infosItem) {
        if (this.infos == null) {
            this.infos = new ArrayList<>();
        }
        this.infos.add(infosItem);
        return this;
    }

    /**
     * Get infos
     * 
     * @return infos
     **/
    @ApiModelProperty(value = "")
    public List<InfoReport> getInfos() {
        return infos;
    }

    public void setInfos(List<InfoReport> infos) {
        this.infos = infos;
    }

    public SimpleMetadataProcessingReport metadata(List<Integer> metadata) {
        this.metadata = metadata;
        return this;
    }

    public SimpleMetadataProcessingReport addMetadataItem(Integer metadataItem) {
        if (this.metadata == null) {
            this.metadata = new ArrayList<>();
        }
        this.metadata.add(metadataItem);
        return this;
    }

    /**
     * Get metadata
     * 
     * @return metadata
     **/
    @ApiModelProperty(value = "")
    public List<Integer> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<Integer> metadata) {
        this.metadata = metadata;
    }

    public SimpleMetadataProcessingReport metadataErrors(Map<String, List<Report>> metadataErrors) {
        this.metadataErrors = metadataErrors;
        return this;
    }

    public SimpleMetadataProcessingReport putMetadataErrorsItem(String key, List<Report> metadataErrorsItem) {
        if (this.metadataErrors == null) {
            this.metadataErrors = new HashMap<>();
        }
        this.metadataErrors.put(key, metadataErrorsItem);
        return this;
    }

    /**
     * Get metadataErrors
     * 
     * @return metadataErrors
     **/
    @ApiModelProperty(value = "")
    public Map<String, List<Report>> getMetadataErrors() {
        return metadataErrors;
    }

    public void setMetadataErrors(Map<String, List<Report>> metadataErrors) {
        this.metadataErrors = metadataErrors;
    }

    public SimpleMetadataProcessingReport metadataInfos(Map<String, List<InfoReport>> metadataInfos) {
        this.metadataInfos = metadataInfos;
        return this;
    }

    public SimpleMetadataProcessingReport putMetadataInfosItem(String key, List<InfoReport> metadataInfosItem) {
        if (this.metadataInfos == null) {
            this.metadataInfos = new HashMap<>();
        }
        this.metadataInfos.put(key, metadataInfosItem);
        return this;
    }

    /**
     * Get metadataInfos
     * 
     * @return metadataInfos
     **/
    @ApiModelProperty(value = "")
    public Map<String, List<InfoReport>> getMetadataInfos() {
        return metadataInfos;
    }

    public void setMetadataInfos(Map<String, List<InfoReport>> metadataInfos) {
        this.metadataInfos = metadataInfos;
    }

    public SimpleMetadataProcessingReport numberOfNullRecords(Integer numberOfNullRecords) {
        this.numberOfNullRecords = numberOfNullRecords;
        return this;
    }

    /**
     * Get numberOfNullRecords
     * 
     * @return numberOfNullRecords
     **/
    @ApiModelProperty(value = "")
    public Integer getNumberOfNullRecords() {
        return numberOfNullRecords;
    }

    public void setNumberOfNullRecords(Integer numberOfNullRecords) {
        this.numberOfNullRecords = numberOfNullRecords;
    }

    public SimpleMetadataProcessingReport numberOfRecordNotFound(Integer numberOfRecordNotFound) {
        this.numberOfRecordNotFound = numberOfRecordNotFound;
        return this;
    }

    /**
     * Get numberOfRecordNotFound
     * 
     * @return numberOfRecordNotFound
     **/
    @ApiModelProperty(value = "")
    public Integer getNumberOfRecordNotFound() {
        return numberOfRecordNotFound;
    }

    public void setNumberOfRecordNotFound(Integer numberOfRecordNotFound) {
        this.numberOfRecordNotFound = numberOfRecordNotFound;
    }

    public SimpleMetadataProcessingReport numberOfRecords(Integer numberOfRecords) {
        this.numberOfRecords = numberOfRecords;
        return this;
    }

    /**
     * Get numberOfRecords
     * 
     * @return numberOfRecords
     **/
    @ApiModelProperty(value = "")
    public Integer getNumberOfRecords() {
        return numberOfRecords;
    }

    public void setNumberOfRecords(Integer numberOfRecords) {
        this.numberOfRecords = numberOfRecords;
    }

    public SimpleMetadataProcessingReport numberOfRecordsNotEditable(Integer numberOfRecordsNotEditable) {
        this.numberOfRecordsNotEditable = numberOfRecordsNotEditable;
        return this;
    }

    /**
     * Get numberOfRecordsNotEditable
     * 
     * @return numberOfRecordsNotEditable
     **/
    @ApiModelProperty(value = "")
    public Integer getNumberOfRecordsNotEditable() {
        return numberOfRecordsNotEditable;
    }

    public void setNumberOfRecordsNotEditable(Integer numberOfRecordsNotEditable) {
        this.numberOfRecordsNotEditable = numberOfRecordsNotEditable;
    }

    public SimpleMetadataProcessingReport numberOfRecordsProcessed(Integer numberOfRecordsProcessed) {
        this.numberOfRecordsProcessed = numberOfRecordsProcessed;
        return this;
    }

    /**
     * Get numberOfRecordsProcessed
     * 
     * @return numberOfRecordsProcessed
     **/
    @ApiModelProperty(value = "")
    public Integer getNumberOfRecordsProcessed() {
        return numberOfRecordsProcessed;
    }

    public void setNumberOfRecordsProcessed(Integer numberOfRecordsProcessed) {
        this.numberOfRecordsProcessed = numberOfRecordsProcessed;
    }

    public SimpleMetadataProcessingReport numberOfRecordsWithErrors(Integer numberOfRecordsWithErrors) {
        this.numberOfRecordsWithErrors = numberOfRecordsWithErrors;
        return this;
    }

    /**
     * Get numberOfRecordsWithErrors
     * 
     * @return numberOfRecordsWithErrors
     **/
    @ApiModelProperty(value = "")
    public Integer getNumberOfRecordsWithErrors() {
        return numberOfRecordsWithErrors;
    }

    public void setNumberOfRecordsWithErrors(Integer numberOfRecordsWithErrors) {
        this.numberOfRecordsWithErrors = numberOfRecordsWithErrors;
    }

    public SimpleMetadataProcessingReport running(Boolean running) {
        this.running = running;
        return this;
    }

    /**
     * Get running
     * 
     * @return running
     **/
    @ApiModelProperty(value = "")
    public Boolean isRunning() {
        return running;
    }

    public void setRunning(Boolean running) {
        this.running = running;
    }

    public SimpleMetadataProcessingReport startIsoDateTime(String startIsoDateTime) {
        this.startIsoDateTime = startIsoDateTime;
        return this;
    }

    /**
     * Get startIsoDateTime
     * 
     * @return startIsoDateTime
     **/
    @ApiModelProperty(value = "")
    public String getStartIsoDateTime() {
        return startIsoDateTime;
    }

    public void setStartIsoDateTime(String startIsoDateTime) {
        this.startIsoDateTime = startIsoDateTime;
    }

    public SimpleMetadataProcessingReport totalTimeInSeconds(Long totalTimeInSeconds) {
        this.totalTimeInSeconds = totalTimeInSeconds;
        return this;
    }

    /**
     * Get totalTimeInSeconds
     * 
     * @return totalTimeInSeconds
     **/
    @ApiModelProperty(value = "")
    public Long getTotalTimeInSeconds() {
        return totalTimeInSeconds;
    }

    public void setTotalTimeInSeconds(Long totalTimeInSeconds) {
        this.totalTimeInSeconds = totalTimeInSeconds;
    }

    public SimpleMetadataProcessingReport type(String type) {
        this.type = type;
        return this;
    }

    /**
     * Get type
     * 
     * @return type
     **/
    @ApiModelProperty(value = "")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public SimpleMetadataProcessingReport uuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    /**
     * Get uuid
     * 
     * @return uuid
     **/
    @ApiModelProperty(value = "")
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SimpleMetadataProcessingReport simpleMetadataProcessingReport = (SimpleMetadataProcessingReport) o;
        return Objects.equals(this.ellapsedTimeInSeconds, simpleMetadataProcessingReport.ellapsedTimeInSeconds)
                && Objects.equals(this.endIsoDateTime, simpleMetadataProcessingReport.endIsoDateTime)
                && Objects.equals(this.errors, simpleMetadataProcessingReport.errors)
                && Objects.equals(this.infos, simpleMetadataProcessingReport.infos)
                && Objects.equals(this.metadata, simpleMetadataProcessingReport.metadata)
                && Objects.equals(this.metadataErrors, simpleMetadataProcessingReport.metadataErrors)
                && Objects.equals(this.metadataInfos, simpleMetadataProcessingReport.metadataInfos)
                && Objects.equals(this.numberOfNullRecords, simpleMetadataProcessingReport.numberOfNullRecords)
                && Objects.equals(this.numberOfRecordNotFound, simpleMetadataProcessingReport.numberOfRecordNotFound)
                && Objects.equals(this.numberOfRecords, simpleMetadataProcessingReport.numberOfRecords)
                && Objects.equals(this.numberOfRecordsNotEditable,
                        simpleMetadataProcessingReport.numberOfRecordsNotEditable)
                && Objects.equals(this.numberOfRecordsProcessed,
                        simpleMetadataProcessingReport.numberOfRecordsProcessed)
                && Objects.equals(this.numberOfRecordsWithErrors,
                        simpleMetadataProcessingReport.numberOfRecordsWithErrors)
                && Objects.equals(this.running, simpleMetadataProcessingReport.running)
                && Objects.equals(this.startIsoDateTime, simpleMetadataProcessingReport.startIsoDateTime)
                && Objects.equals(this.totalTimeInSeconds, simpleMetadataProcessingReport.totalTimeInSeconds)
                && Objects.equals(this.type, simpleMetadataProcessingReport.type)
                && Objects.equals(this.uuid, simpleMetadataProcessingReport.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ellapsedTimeInSeconds, endIsoDateTime, errors, infos, metadata, metadataErrors,
                metadataInfos, numberOfNullRecords, numberOfRecordNotFound, numberOfRecords, numberOfRecordsNotEditable,
                numberOfRecordsProcessed, numberOfRecordsWithErrors, running, startIsoDateTime, totalTimeInSeconds,
                type, uuid);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SimpleMetadataProcessingReport {\n");

        sb.append("    ellapsedTimeInSeconds: ").append(toIndentedString(ellapsedTimeInSeconds)).append("\n");
        sb.append("    endIsoDateTime: ").append(toIndentedString(endIsoDateTime)).append("\n");
        sb.append("    errors: ").append(toIndentedString(errors)).append("\n");
        sb.append("    infos: ").append(toIndentedString(infos)).append("\n");
        sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
        sb.append("    metadataErrors: ").append(toIndentedString(metadataErrors)).append("\n");
        sb.append("    metadataInfos: ").append(toIndentedString(metadataInfos)).append("\n");
        sb.append("    numberOfNullRecords: ").append(toIndentedString(numberOfNullRecords)).append("\n");
        sb.append("    numberOfRecordNotFound: ").append(toIndentedString(numberOfRecordNotFound)).append("\n");
        sb.append("    numberOfRecords: ").append(toIndentedString(numberOfRecords)).append("\n");
        sb.append("    numberOfRecordsNotEditable: ").append(toIndentedString(numberOfRecordsNotEditable)).append("\n");
        sb.append("    numberOfRecordsProcessed: ").append(toIndentedString(numberOfRecordsProcessed)).append("\n");
        sb.append("    numberOfRecordsWithErrors: ").append(toIndentedString(numberOfRecordsWithErrors)).append("\n");
        sb.append("    running: ").append(toIndentedString(running)).append("\n");
        sb.append("    startIsoDateTime: ").append(toIndentedString(startIsoDateTime)).append("\n");
        sb.append("    totalTimeInSeconds: ").append(toIndentedString(totalTimeInSeconds)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    uuid: ").append(toIndentedString(uuid)).append("\n");
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
