/*
 * Copyright (C) 2009 by the geOrchestra PSC
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

import org.georchestra.extractorapp.ws.extractor.task.ExecutionPriority;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Encapsulates the parameters required the priority of a task
 * 
 * @author Mauricio Pazos
 *
 */
class ExtractorUpdatePriorityRequest {

    public final String _uuid;
    public final ExecutionPriority _priority;

    public ExtractorUpdatePriorityRequest(String uuid, ExecutionPriority priority) {

        assert uuid != null && priority != null;

        _uuid = uuid;
        _priority = priority;
    }

    /**
     * Makes a new instance of {@link ExtractorUpdatePriorityRequest}
     * 
     * @param jsonData a {"uuid":value, "priority": value}
     * @return {@link ExtractorUpdatePriorityRequest}
     * @throws JSONException
     */
    public static ExtractorUpdatePriorityRequest parseJson(String jsonData) throws JSONException {

        JSONObject jsonRequest = JSONUtil.parseStringToJSon(jsonData);

        final String uuid = jsonRequest.getString(TaskDescriptor.UUID_KEY);

        final String strPriority = jsonRequest.getString(TaskDescriptor.PRIORITY_KEY);
        ExecutionPriority priority = ExecutionPriority.valueOf(strPriority);

        ExtractorUpdatePriorityRequest request = new ExtractorUpdatePriorityRequest(uuid, priority);

        return request;
    }

    /**
     * New instance of {@link ExtractorUpdatePriorityRequest}
     * 
     * @param uuid
     * @param intPriority it should be one of the enumerated values defined in
     *                    {@link ExecutionPriority}}
     * @return {@link ExtractorUpdatePriorityRequest}
     */
    public static ExtractorUpdatePriorityRequest newInstance(final String uuid, final int intPriority) {

        ExecutionPriority priority = null;
        for (ExecutionPriority p : ExecutionPriority.values()) {
            if (p.ordinal() == intPriority) {
                priority = p;
                break;
            }
        }
        if (priority == null) {
            throw new IllegalArgumentException("the priority value: " + intPriority + " is not valid.");
        }

        ExtractorUpdatePriorityRequest request = new ExtractorUpdatePriorityRequest(uuid, priority);

        return request;
    }
}
