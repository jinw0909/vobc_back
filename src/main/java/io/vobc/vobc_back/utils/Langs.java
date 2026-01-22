package io.vobc.vobc_back.utils;

import io.vobc.vobc_back.domain.LanguageCode;

import java.util.Locale;

public final class Langs {

    private Langs() { }

    public static Locale toLocale(LanguageCode code) {
        if (code == null) return Locale.KOREAN;
        return switch (code) {
            case en -> Locale.ENGLISH;
            case jp -> Locale.JAPAN;
            case cn -> Locale.CHINESE;
            default -> Locale.KOREAN;
        };
    }
}
