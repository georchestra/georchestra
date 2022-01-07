/*
 * Copyright (C) 2009-2022 by the geOrchestra PSC
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

package org.georchestra.extractorapp.ws.extractor;

import java.util.List;

import org.georchestra.extractorapp.ws.extractor.task.ExecutionMetadata;
import org.georchestra.extractorapp.ws.extractor.task.ExecutionState;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONWriter;

/**
 * Maintains the The response data for the getTaskQueue operation.
 * 
 * @author Mauricio Pazos
 *
 */
final class ExtractorGetTaskQueueResponse {

    private List<ExecutionMetadata> taskQueue;

    private ExtractorGetTaskQueueResponse(List<ExecutionMetadata> taskQueue) {
        this.taskQueue = taskQueue;

    }

    public static ExtractorGetTaskQueueResponse newInstance(List<ExecutionMetadata> taskQueue) {
        return new ExtractorGetTaskQueueResponse(taskQueue);
    }

    /**
     * Returns the tasks as a json object. The tasks are added to a json object
     * where each task object is made from a {@link ExecutionMetadata}
     * 
     * <pre>
     * 
     * <b>JSON format:</b> {"tasks":[ {"uuid":"value", "priority":value,"status":value,...}, ...]}
     * 
     * </pre>
     * 
     * @return the list of task as a json array
     * @throws JSONException
     */
    public String asJsonString() throws JSONException {

        JSONArray jsonTaskArray = new JSONArray();
        int i = 0;
        for (ExecutionMetadata metadata : this.taskQueue) {

            String uuid = metadata.getUuid();
            String requestor = metadata.getRequestor();
            Integer priority = metadata.getPriority().ordinal();
            ExecutionState status = metadata.getState();
            JSONObject spec = new JSONObject(metadata.getSpec());
            String requestTimeStamp = TaskDescriptor.formatDate(metadata.getRequestTime());

            String beginTimeStamp = TaskDescriptor.formatDate(metadata.getBeginTime());
            String endTimeStamp = TaskDescriptor.formatDate(metadata.getEndTime());

            JSONObject jsonTask = new JSONObject();
            jsonTask.put(TaskDescriptor.UUID_KEY, uuid);
            jsonTask.put(TaskDescriptor.REQUESTOR_KEY, requestor);
            jsonTask.put(TaskDescriptor.PRIORITY_KEY, priority);
            jsonTask.put(TaskDescriptor.STATE_KEY, status.toString());
            jsonTask.put(TaskDescriptor.SPEC_KEY, spec);
            jsonTask.put(TaskDescriptor.REQUEST_TS_KEY, requestTimeStamp);
            jsonTask.put(TaskDescriptor.BEGIN_TS_KEY, beginTimeStamp);
            jsonTask.put(TaskDescriptor.END_TS_KEY, endTimeStamp);

            jsonTaskArray.put(i, jsonTask);
            i++;
        }

        JSONWriter jsonTaskQueue = new JSONStringer().object().key("tasks").value(jsonTaskArray).endObject();

        String strTaskQueue = jsonTaskQueue.toString();

        return strTaskQueue;
    }

}
