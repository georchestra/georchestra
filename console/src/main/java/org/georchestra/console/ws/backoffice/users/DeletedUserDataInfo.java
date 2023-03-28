package org.georchestra.console.ws.backoffice.users;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

public @Data @Builder class DeletedUserDataInfo {
    private @NonNull String account;
    private Integer metadata;
    private Integer extractor;
    private Integer ogcStats;
}
