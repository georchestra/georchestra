package org.georchestra.dlform;

import org.json.JSONObject;

public class Utils {
    public static byte[] serviceDisabled() {
        try {
        return new JSONObject().put("status", "unavailable")
                .put("reason", "downloadform disabled")
                .toString(4).getBytes();
        } catch (Throwable e) {
            return "{ status: \"unavailable\", reason: \"downloadform disabled\" }".getBytes();
        }
    }
}
