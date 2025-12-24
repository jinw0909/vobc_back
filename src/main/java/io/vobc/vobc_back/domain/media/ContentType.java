package io.vobc.vobc_back.domain.media;

import java.util.Arrays;

public enum ContentType {
    IMAGE("image"), VIDEO("video"), AUDIO("audio");

    private final String displayName;

    ContentType(String displayName) {
        this.displayName = displayName;
    }

    public ContentType from(String displayName) {
        return Arrays.stream(values())
                .filter(t -> t.displayName.equalsIgnoreCase(displayName))
                .findFirst()
                .orElse(null);
    }
}
