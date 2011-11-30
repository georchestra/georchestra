package extractorapp.ws.extractor.task;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Future;

public class ExecutionMetadata {

    private ExecutionState state = ExecutionState.WAITING;
    private Date stateChangeTime = new Date();
    private ExecutionPriority priority = ExecutionPriority.MEDIUM;
    private Future<?> future = new PlaceholderFuture();
    private String uuid;

    public ExecutionMetadata(UUID requestUuid) {
        this.uuid = requestUuid.toString();
    }
    
    

    public ExecutionMetadata(ExecutionMetadata toCopy) {
        this.state = toCopy.state;
        this.stateChangeTime = toCopy.stateChangeTime;
        this.priority = toCopy.priority;
        this.future = toCopy.future;
        this.uuid = toCopy.uuid;
    }



    public synchronized void setRunning() {
        state = ExecutionState.RUNNING;
        stateChangeTime = new Date();
    }

    public synchronized void setCompleted() {
        state = ExecutionState.COMPLETED;
        stateChangeTime = new Date();
    }

    public synchronized ExecutionState getState() {
        return state;
    }

    public synchronized Date getStateChangeTime() {
        return stateChangeTime;
    }

    public synchronized ExecutionPriority getPriority() {
        return priority;
    }

    public synchronized void setPriority(ExecutionPriority priority) {
        this.priority = priority;
    }

    synchronized void setFuture(Future<?> future) {
        this.future = future;
    }

    public synchronized Future<?> getFuture() {
        return this.future;
    }

    public synchronized boolean isCompleted() {
        return ExecutionState.COMPLETED == state;
    }

    public synchronized String getUuid() {
        return uuid;
    }

    public synchronized boolean isWaiting() {
        return ExecutionState.WAITING == state;
    }

    public synchronized void cancel() {
        state = ExecutionState.CANCELLED;
        
    }
}