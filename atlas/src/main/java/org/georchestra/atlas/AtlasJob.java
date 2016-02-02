package org.georchestra.atlas;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(schema = "atlas", name = "atlas_jobs")
public class AtlasJob {

    private final static String SEQUENCE_NAME = "atlas_jobs_seq";

    @Id
    @SequenceGenerator(name=SEQUENCE_NAME, schema = "atlas", sequenceName=SEQUENCE_NAME, initialValue=1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AtlasJobState state;

    /*
     *   Json encoded query to mapfish print
     */
    @Column(columnDefinition="text")
    private String query;

    private Date created;
    private Date updated;

    public AtlasJob(){}

    public AtlasJob(String query) {
        this.query = query;
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
        return "AtlasJob{" +
                "id=" + id +
                ", state=" + state +
                ", query='" + query + '\'' +
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

    public AtlasJobState getState() {
        return state;
    }

    public AtlasJob setState(AtlasJobState state) {
        this.state = state;
        return this;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }
}
