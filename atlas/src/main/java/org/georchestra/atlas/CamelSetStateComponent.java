package org.georchestra.atlas;

public class CamelSetStateComponent {


    public void setState(AtlasMFPJob job, AtlasJobState newState){
        job.setState(newState);
    }

    public void setTodo(AtlasMFPJob job){
        this.setState(job, AtlasJobState.TODO);
    }

    public void setInProgress(AtlasMFPJob job){
        this.setState(job, AtlasJobState.IN_PROGRESS);
    }

    public void setDone(AtlasMFPJob job){
        this.setState(job, AtlasJobState.DONE);
    }

    public void setError(AtlasMFPJob job){
        this.setState(job, AtlasJobState.ERROR);
    }

}
