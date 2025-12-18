package io.vobc.vobc_back.domain.term;

import java.util.Arrays;

public enum TermCode {
    use("use"), privacy("privacy"), cookies("cookies");

    private final String code;

    TermCode(String code) {
        this.code = code;
    }

    public static TermCode from(String code) {
        if (code == null) {
            return use;
        }

        return Arrays.stream(values())
                .filter(c -> c.code.equalsIgnoreCase(code))
                .findFirst()
                .orElse(use);
    }
}
