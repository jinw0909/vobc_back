package io.vobc.vobc_back.dto.publisher;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.publisher.PublisherTranslation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PublisherTranslationForm {

    private Long id;
    private LanguageCode languageCode;
    private String name;
    private String introduction;
    private Long publisherId;

    public PublisherTranslationForm(PublisherTranslation translation) {
        this.id = translation.getId();
        this.languageCode = translation.getLanguageCode();
        this.name = translation.getName();
        this.introduction = translation.getIntroduction();
    }

    @Override
    public String toString() {
        return "PublisherTranslationForm{" +
                "id=" + id +
                ", languageCode=" + languageCode +
                ", name='" + name + '\'' +
                ", introduction='" + introduction + '\'' +
                ", publisherId=" + publisherId +
                '}';
    }
}
