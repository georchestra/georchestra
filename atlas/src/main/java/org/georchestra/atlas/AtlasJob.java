package org.georchestra.atlas;

import javax.persistence.*;

import org.json.JSONException;
import org.json.JSONObject;

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

    @Column(columnDefinition="character varying (64)")
    private String token;

    public AtlasJob(){}

    public AtlasJob(String query) {
        this.query = query;
        this.onCreate();
    }

    @PrePersist
    protected void onCreate() {
        this.created = new Date();
        this.updated = new Date();
        this.token = UUID.randomUUID().toString();
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
                ", query='" + query + "'" +
                ", token='" + token + "'" +
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

    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token= token;
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
    /**
     * This is a convenient method to retrieve the filename requested by the original
     * request.
     *
     * @return String the original filename.
     * @throws JSONException
     *
     */
    public String getFileName() throws JSONException {
        JSONObject spec = new JSONObject(this.query);
        return spec.getString("filename");
    }
    /**
     * This is a convenient method to retrieve the format of the atlas job (PDF
     * or ZIP), contained in the JSON query.
     *
     * @return String "pdf" or "zip", depending on the client's original request.
     * @throws JSONException
     *
     */
    public String getOutputFormat() throws JSONException {
        JSONObject spec = new JSONObject(this.query);
        return spec.getString("outputFormat");
    }
}
