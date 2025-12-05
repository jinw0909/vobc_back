package io.vobc.vobc_back.domain;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum LanguageCode {

    kr("kr", "한국어"),
    en("en","English"),
    jp("jp", "日本語"),
    cn("cn", "汉文");

    private final String code;
    private final String displayName;

    private LanguageCode(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public static LanguageCode from(String code) {
        if (code == null) {
            return en;
        }

        return Arrays.stream(values())
                .filter(l -> l.code.equalsIgnoreCase(code))
                .findFirst()
                .orElse(en);
    }
}
