package io.vobc.vobc_back.service;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.Post;
import io.vobc.vobc_back.domain.Translation;
import io.vobc.vobc_back.dto.TranslationForm;
import io.vobc.vobc_back.repository.PostRepository;
import io.vobc.vobc_back.repository.TranslationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TranslationService {

    private final TranslationRepository translationRepository;
    private final PostRepository postRepository;

    public Optional<Translation> findByPostIdAndLanguageCode(Long id, LanguageCode languageCode) {
        return translationRepository.findByPostIdAndLanguageCode(id, languageCode);
    }

    @Transactional
    public void saveOrUpdate(TranslationForm form) {
        Long postId = form.getPostId();

        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));

        Optional<Translation> opt = translationRepository.findByPostIdAndLanguageCode(postId, form.getLanguageCode());

        if (opt.isPresent()) {
            Translation translation = opt.get();
            translation.setTitle(form.getTitle());
            translation.setContent(form.getContent());
            translation.setSummary(form.getSummary());
            translation.setAuthor(form.getAuthor());
            return;
        }

        Translation translation = new Translation(form.getLanguageCode(), form.getTitle(), form.getContent(), form.getSummary(), form.getAuthor());

        post.addTranslation(translation);
        translationRepository.save(translation);
    }
}
