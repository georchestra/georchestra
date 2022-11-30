package org.georchestra.security;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CookieAffinity {

    private String name;
    private String from;
    private String to;

    public @Override String toString() {
        return String.format("Cookie affinity[name: %s, from path: %s copied to path: %s]", name, from, to);
    }
}
