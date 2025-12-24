package io.vobc.vobc_back.domain.article;

import java.util.Arrays;

public enum Category {

    editorial("editorial"), event("event"), interview("interview"), publicity("publicity"), news("news");

    private final String type;

    Category(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static Category from(String type) {
        if (type == null) {
            return editorial;
        }

        return Arrays.stream(values())
                .filter(c -> c.type.equalsIgnoreCase(type))
                .findFirst()
                .orElse(null);
    }



}
