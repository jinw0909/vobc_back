package io.vobc.vobc_back.service;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.post.Post;
import io.vobc.vobc_back.domain.post.Translation;
import io.vobc.vobc_back.dto.TranslationForm;
import io.vobc.vobc_back.repository.PostRepository;
import io.vobc.vobc_back.repository.TranslationRepository;
import io.vobc.vobc_back.service.media.MediaService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TranslationService {

    private final TranslationRepository translationRepository;
    private final PostRepository postRepository;
    private final MediaService mediaService;

    public Optional<Translation> findByPostIdAndLanguageCode(Long id, LanguageCode languageCode) {
        return translationRepository.findByPostIdAndLanguageCode(id, languageCode);
    }

    @Transactional
    public Long saveTranslation(TranslationForm form) {
        Long postId = form.getPostId();

        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));

        String replacedContent = mediaService.replaceImagesAndSave(
                post,
                form.getContent(),
                form.getFiles()
        );

        Optional<Translation> opt = translationRepository.findByPostIdAndLanguageCode(postId, form.getLanguageCode());

        boolean hasFiles = form.getFiles() != null && form.getFiles().stream().anyMatch(f -> !f.isEmpty());

        if (opt.isPresent()) {
            Translation translation = opt.get();
            translation.setTitle(form.getTitle());
            translation.setContent(replacedContent);
            translation.setSummary(form.getSummary());
            translation.setAuthor(form.getAuthor());

//            translationRepository.flush();
            if (hasFiles) mediaService.cleanUpPostConsideringAllContents(post.getId());
            return translation.getId();
        }

        Translation translation = new Translation(
                form.getLanguageCode(),
                form.getTitle(),
                replacedContent,
                form.getSummary(),
                form.getAuthor()
        );

        post.addTranslation(translation);
        Translation saved = translationRepository.save(translation);

//        translationRepository.flush();
        if (hasFiles) mediaService.cleanUpPostConsideringAllContents(post.getId());

        return saved.getId();
    }

    @Transactional
    public void deleteByPostIdAndLanguageCode(Long postId, LanguageCode languageCode) {
        translationRepository.deleteByPost_IdAndLanguageCode(postId, languageCode);
    }
}
