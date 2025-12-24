package io.vobc.vobc_back.dto.publisher;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.publisher.PublisherTranslation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PublisherTranslationResponse {

    Long publisherId;
    Long id;
    LanguageCode languageCode;
    String name;
    String introduction;

    public static PublisherTranslationResponse create(Long publisherId, PublisherTranslation translation) {
        PublisherTranslationResponse publisherTranslationResponse = new PublisherTranslationResponse();
        publisherTranslationResponse.publisherId = publisherId;
        publisherTranslationResponse.id = translation.getId();
        publisherTranslationResponse.languageCode = translation.getLanguageCode();
        publisherTranslationResponse.name = translation.getName();
        publisherTranslationResponse.introduction = translation.getIntroduction();
        return publisherTranslationResponse;
    }

}
