package io.vobc.vobc_back.domain.post;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum PostType {
    review("review"), feature("feature"), interview("interview");

    private final String type;

    PostType(String type) {
        this.type = type;
    }

    public static PostType from(String type) {
        return Arrays.stream(values())
                .filter(p -> p.type.equalsIgnoreCase(type))
                .findFirst()
                .orElse(null);
    }

}
