package org.georchestra.atlas;

import org.springframework.beans.factory.annotation.Autowired;

public class CamelSetStateComponent {

    @Autowired
    private AtlasJobDao repository;

    public void setState(AtlasMFPJob job, AtlasJobState newState){
        job.setState(newState);
        this.repository.save(job);
    }

    public void setState(Long jobId, AtlasJobState newState){
        AtlasMFPJob job = this.repository.findOne(jobId);
        this.setState(job,newState);
    }

    public void setTodo(Long jobId){
        this.setState(jobId, AtlasJobState.TODO);
    }

    public void setInProgress(Long jobId){
        this.setState(jobId, AtlasJobState.IN_PROGRESS);
    }

    public void setDone(Long jobId){
        this.setState(jobId, AtlasJobState.DONE);
    }

    public void setError(Long jobId){
        this.setState(jobId, AtlasJobState.ERROR);
    }

    public void setTodo(AtlasMFPJob job){
        this.setState(job.getId(), AtlasJobState.TODO);
    }

    public void setInProgress(AtlasMFPJob job){
        this.setState(job.getId(), AtlasJobState.IN_PROGRESS);
    }

    public void setDone(AtlasMFPJob job){
        this.setState(job.getId(), AtlasJobState.DONE);
    }

    public void setError(AtlasMFPJob job){
        this.setState(job.getId(), AtlasJobState.ERROR);
    }

}
