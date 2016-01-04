package org.georchestra.atlas;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(schema = "atlas", name = "atlas_jobs")
public class AtlasMFPJob {

    @Id
    @SequenceGenerator(name="atlas_jobs_seq", schema = "atlas", sequenceName="atlas_jobs_seq", initialValue=1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "atlas_jobs_seq")
    private Long id;

    private UUID uuid;

    @Enumerated(EnumType.STRING)
    private AtlasJobState state;

    /*
     *   Json encoded query to mapfish print
     */
    @Column(columnDefinition="text")
    private String query;

    private String filename;

    private Short pageIndex;

    private Date created;
    private Date updated;

    public AtlasMFPJob(){}

    public AtlasMFPJob(UUID uuid, String query, String filename, Short pageIndex) {
        this.uuid = uuid;
        this.query = query;
        this.filename = filename;
        this.pageIndex = pageIndex;
        this.onCreate();
    }

    @PrePersist
    protected void onCreate() {
        this.created = new Date();
        this.updated = new Date();
        this.state = AtlasJobState.TODO;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updated = new Date();
    }


    @Override
    public String toString() {
        return "AtlasMFPJob{" +
                "id=" + id +
                ", query='" + query + '\'' +
                ", filename='" + filename + '\'' +
                ", pageIndex=" + pageIndex +
                ", created=" + created +
                ", updated=" + updated +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public AtlasJobState getState() {
        return state;
    }

    public void setState(AtlasJobState state) {
        this.state = state;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Short getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(Short pageIndex) {
        this.pageIndex = pageIndex;
    }

    public Date getCreated() {
        return created;
    }

    public Date getUpdated() {
        return updated;
    }
}
