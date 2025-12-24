package io.vobc.vobc_back.domain.team;

import lombok.Getter;

@Getter
public enum TeamRole {

    CEO(1, "CEO"),
    COO(2, "COO"),
    CTO(3, "CTO");

    private final int priority;
    private final String displayName;

    TeamRole(int priority, String displayName) {
        this.priority = priority;
        this.displayName = displayName;
    }

}
