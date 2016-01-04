package org.georchestra.atlas;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(schema = "atlas", name = "atlas_jobs")
public class AtlasMFPJob {

    @Id
    @SequenceGenerator(name="atlas_jobs_seq", schema = "atlas", sequenceName="atlas_jobs_seq", initialValue=1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "atlas_jobs_seq")
    private Long id;

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

    public AtlasMFPJob(String query, String filename, Short pageIndex) {
        this.query = query;
        this.filename = filename;
        this.pageIndex = pageIndex;
        this.onCreate();
    }

    @PrePersist
    protected void onCreate() {
        this.created = new Date();
        this.updated = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updated = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
