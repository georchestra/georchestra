package extractorapp.ws.extractor.task;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ExecutionMetadata {

    private ExecutionState state = ExecutionState.WAITING;
    private Date stateChangeTime = new Date();
    private ExecutionPriority priority = ExecutionPriority.MEDIUM;
    private Future<?> future = new DumbFuture();
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

    void setFuture(Future<?> future) {
        this.future = future;
    }

    public Future<?> getFuture() {
        return this.future;
    }

    private static final class DumbFuture implements Future<Object> {
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public Object get() throws InterruptedException, ExecutionException {
            return null;
        }

        @Override
        public Object get(long timeout, TimeUnit unit)
                throws InterruptedException, ExecutionException,
                TimeoutException {
            return null;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return false;
        }
    }

    public boolean isCompleted() {
        return ExecutionState.COMPLETED == state;
    }

    public String getUuid() {
        return uuid;
    }

    public boolean isWaiting() {
        return ExecutionState.WAITING == state;
    }



    public void cancel() {
        state = ExecutionState.CANCELLED;
        
    }
}