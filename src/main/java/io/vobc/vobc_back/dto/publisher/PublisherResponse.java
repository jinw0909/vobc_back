package io.vobc.vobc_back.dto.publisher;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.publisher.Publisher;
import io.vobc.vobc_back.domain.publisher.PublisherTranslation;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PublisherResponse {

    private Long id;
    private String name;
    private String introduction;
    private List<PublisherTranslationResponse> translations;

    public static PublisherResponse create(Publisher publisher) {

        PublisherResponse r = new PublisherResponse();
        if (publisher == null) return r;

        r.setId(publisher.getId());
        r.setName(publisher.getName());
        r.setIntroduction(publisher.getIntroduction());
        return r;
    }

    public static PublisherResponse translate(PublisherResponse publisherResponse,
                                              LanguageCode languageCode
    ) {
        publisherResponse.translations.stream()
                .filter(t -> t.getLanguageCode().equals(languageCode))
                .findFirst()
                .ifPresent(t -> {
                    publisherResponse.setName(t.getName());
                    publisherResponse.setIntroduction(t.getIntroduction());
                });
        return publisherResponse;

    }

}
