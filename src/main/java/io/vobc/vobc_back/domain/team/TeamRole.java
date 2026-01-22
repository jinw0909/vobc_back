package io.vobc.vobc_back.domain.team;

import lombok.Getter;

@Getter
public enum TeamRole {
    CEO(1),
    COO(2),
    CTO(3),
    CFO(4),
    MANAGING_DIRECTOR(5),
    DIRECTOR(6),
    GENERAL_MANAGER(7),
    DEPUTY_MANAGER(8),
    MANAGER(9),
    TEAM_MANAGER(10),
    ASSISTANT_MANAGER(11),
    GLOBAL_MANAGER(12),
    ASSOCIATE(13),
    EXTERNAL_CONSULTANT(14),
    EXTERNAL_ADVISOR(15),
    EXTERNAL_SPOKESPERSON(16),
    LEGAL_ADVISOR(17);

    private final int priority;

    TeamRole(int priority) {
        this.priority = priority;
    }

    public String messageKey() {
        return "teamRole." + name();
    }

    public static TeamRole from(String value) {
        if (value == null) return null;

        try {
            return TeamRole.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static TeamRole fromOrDefault(String value, TeamRole defaultRole) {
        TeamRole role = from(value);
        return role != null ? role : defaultRole;

    }
}
