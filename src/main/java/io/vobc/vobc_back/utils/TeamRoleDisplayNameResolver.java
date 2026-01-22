package io.vobc.vobc_back.utils;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.team.TeamRole;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class TeamRoleDisplayNameResolver {

    private final MessageSource messageSource;

    public String resolve(TeamRole role, LanguageCode code) {
        if (role == null) return "";
        Locale locale = Langs.toLocale(code);
        return messageSource.getMessage(role.messageKey(), null, role.name(), locale);
    }
}
