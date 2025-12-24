package io.vobc.vobc_back.service;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.publisher.Publisher;
import io.vobc.vobc_back.domain.publisher.PublisherTranslation;
import io.vobc.vobc_back.dto.publisher.PublisherForm;
import io.vobc.vobc_back.dto.publisher.PublisherTranslationForm;
import io.vobc.vobc_back.repository.publisher.PublisherRepository;
import io.vobc.vobc_back.repository.publisher.PublisherTranslationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublisherService {

    private final PublisherRepository publisherRepository;
    private final PublisherTranslationRepository publisherTranslationRepository;

    @Transactional
    public Long savePublisher(PublisherForm form) {

        Publisher publisher;
        if (form.getId() == null) {
            // CREATE
            publisher = Publisher.create(form.getName(), form.getIntroduction());
            publisherRepository.save(publisher);
        } else {
            publisher = publisherRepository.findById(form.getId()).orElseThrow(() -> new IllegalArgumentException("Publisher not found"));
            publisher.setName(form.getName());
            publisher.setIntroduction(form.getIntroduction());
        }
        return publisher.getId();

    }

    @Transactional(readOnly = true)
    public Page<PublisherForm> getPublishers(Pageable pageable) {
        return publisherRepository.findAll(pageable).map(PublisherForm::new);
    }

    @Transactional(readOnly = true)
    public PublisherForm getPublisher(Long id) {
        Publisher publisher = publisherRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Publisher not found"));
        return new PublisherForm(publisher);
    }

    @Transactional(readOnly = true)
    public PublisherTranslationForm getTranslation(Long publisherId, LanguageCode languageCode) {
        PublisherTranslation publisherTranslation = publisherTranslationRepository.findByPublisherIdAndLanguageCode(publisherId, languageCode);

        log.info("Found translation: {}", publisherTranslation);

        if (publisherTranslation == null) {
            PublisherTranslationForm empty = new PublisherTranslationForm();
            empty.setLanguageCode(languageCode);
            return empty;
        }

        return new PublisherTranslationForm(publisherTranslation);
    }

    @Transactional(readOnly = true)
    public Set<LanguageCode> getLanguageCodes(Long id) {
        return publisherTranslationRepository.findLanguageCodesByPublisherId(id);
    }

    @Transactional
    public void deletePublisher(Long id) {
        Publisher publisher = publisherRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Publisher not found"));
        publisherRepository.delete(publisher);
    }

    @Transactional
    public LanguageCode translate(PublisherTranslationForm translationForm, Long publisherId) {

        Publisher publisher = publisherRepository.findById(publisherId).orElseThrow(() -> new IllegalArgumentException("Publisher not found"));

        PublisherTranslation publisherTranslation;
        if (translationForm.getId() == null) {
            publisherTranslation = PublisherTranslation.create(
                    translationForm.getLanguageCode(),
                    translationForm.getName(),
                    translationForm.getIntroduction(),
                    publisher
            );
            publisherTranslationRepository.save(publisherTranslation);
        } else {
            publisherTranslation = publisherTranslationRepository.findById(translationForm.getId()).orElseThrow(() -> new IllegalArgumentException("Translation not found"));
            publisherTranslation.setName(translationForm.getName());
            publisherTranslation.setIntroduction(translationForm.getIntroduction());
        }
        return publisherTranslation.getLanguageCode();
    }

    public void deleteTranslation(Long id) {
        PublisherTranslation publisherTranslation = publisherTranslationRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Translation not found"));
        publisherTranslationRepository.delete(publisherTranslation);
    }
}
