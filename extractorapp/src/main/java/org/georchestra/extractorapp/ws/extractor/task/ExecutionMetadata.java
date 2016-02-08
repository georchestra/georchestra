/*
 * Copyright (C) 2009-2016 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.extractorapp.ws.extractor.task;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Future;

public class ExecutionMetadata {

	// mutable attributes
    private ExecutionState state = ExecutionState.WAITING;
    private Date stateChangeTime = new Date();
	private Date beginTime = null;
    private Date endTime = null;
    private ExecutionPriority priority = ExecutionPriority.MEDIUM;

    // this values are immutables
    private final String requestor;
	private final Date requestTime;
	private final String requests;
    private final String uuid;

    private Future<?> future = new PlaceholderFuture();


	public ExecutionMetadata(UUID requestUuid, String userName, Date date,  String requests) {
        this.uuid = requestUuid.toString();
        this.requestor = userName;
        this.requestTime = date;
        this.requests = requests;
    }

    public ExecutionMetadata(ExecutionMetadata toCopy) {
        this.state = toCopy.state;
        this.stateChangeTime = toCopy.stateChangeTime;
        this.beginTime = toCopy.beginTime;
        this.endTime = toCopy.endTime;
        this.requestTime = toCopy.requestTime;
        this.requestor = toCopy.requestor;
        this.priority = toCopy.priority;
        this.future = toCopy.future;
        this.uuid = toCopy.uuid;
        this.requests = toCopy.requests;
    }

    public String getUuid() {
        return uuid;
    }

    public String getSpec() {
		
		return this.requests;
	}

	public String getRequestor() {
		return requestor;
	}


	public Date getRequestTime() {
		return requestTime;
	}

	public synchronized void setWaiting() {
        state = ExecutionState.WAITING;
        stateChangeTime = new Date();
    }
    
    public synchronized void setRunning() {
        state = ExecutionState.RUNNING;
        stateChangeTime = new Date();
        if(beginTime == null){ 
        	beginTime = stateChangeTime;
        }
    }
	public synchronized void setPaused() {
		state = ExecutionState.PAUSED;
        stateChangeTime = new Date();
	}

    public synchronized void setCompleted() {
        state = ExecutionState.COMPLETED;
        stateChangeTime = new Date();
        endTime= stateChangeTime;
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
    public Date getBeginTime() {
		return beginTime;
	}

	public Date getEndTime() {
		return endTime;
	}

    public synchronized void setPriority(ExecutionPriority priority) {
        this.priority = priority;
    }
    public
    synchronized void setFuture(Future<?> future) {
        this.future = future;
    }

    public synchronized Future<?> getFuture() {
        return this.future;
    }

    public synchronized boolean isCompleted() {
        return ExecutionState.COMPLETED == state;
    }

    public synchronized boolean isWaiting() {
        return ExecutionState.WAITING == state;
    }

    public synchronized void cancel() {
        state = ExecutionState.CANCELLED;
    }

	public synchronized boolean isRunning() {
		return ExecutionState.RUNNING == state;
	}

	public synchronized boolean isPaused() {
		return ExecutionState.PAUSED == state;
	}
}