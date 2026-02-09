package io.vobc.vobc_back.service;

import io.vobc.vobc_back.domain.*;
import io.vobc.vobc_back.domain.post.Post;
import io.vobc.vobc_back.domain.post.PostTag;
import io.vobc.vobc_back.domain.post.Translation;
import io.vobc.vobc_back.dto.PagedResponse;
import io.vobc.vobc_back.dto.TagForm;
import io.vobc.vobc_back.dto.post.PostResponse;
import io.vobc.vobc_back.exception.DuplicateTagException;
import io.vobc.vobc_back.repository.PostRepository;
import io.vobc.vobc_back.repository.TagRepository;
import io.vobc.vobc_back.repository.TranslationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagService {

    private final TagRepository tagRepository;
    private final TranslationRepository translationRepository;
    private final PostRepository postRepository;

    @Transactional
    public Tag createTag(String name) {

        boolean exists = tagRepository.existsByName(name);

        if (exists) {
            throw new DuplicateTagException("Tag already exists: " + name);
        }

        return tagRepository.save(new Tag(name));
    }

    @Transactional(readOnly = true)
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Tag getTag(Long id) {
        return tagRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Tag not found: " + id));
    }

    @Transactional
    public void deleteTag(Long id) {
        tagRepository.deleteById(id);
    }

    @Transactional
    public Tag updateTag(Long id, String newName) {
        Tag tag = tagRepository.findById(id).orElseThrow();

        if (tag.getName().equals(newName)) {
            return tag;
        }

        if (tagRepository.existsByName(newName)) {
            throw new DuplicateTagException("Tag already exists: " + newName);
        }

        tag.changeName(newName);
        return tag;
    }

    // 관리자/웹용
    @Transactional(readOnly = true)
    public List<Post> getPostsByTagId(Long id) {

        Tag tag = tagRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Tag not found: " + id));
        return tag.getPostTags().stream()
                .map(PostTag::getPost)
                .distinct()
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<Post> getAllPostsByTagId(Long tagId,
                                         Pageable pageable) {
        return postRepository.findAllWithTagsByTagId(tagId, pageable);
    }

    // API용 (페이징 + 번역)
    @Transactional(readOnly = true)
    public PagedResponse<PostResponse> getPostsByTagId(Long id,
                                                       LanguageCode languageCode,
                                                       Pageable pageable) {
        Page<Post> postPage = postRepository.findByPostTags_Tag_Id(id, pageable);
        List<Post> posts = postPage.getContent();

        if (posts.isEmpty()) {
            return new PagedResponse<>(List.of(), pageable.getPageNumber(), pageable.getPageSize(), postPage.getTotalElements(), postPage.getTotalPages());
        }

        //이번 페이지의 post id들
        List<Long> postIds = posts.stream()
                .map(Post::getId)
                .toList();

        // 해당 언어 번역만 한 번에 조회
        List<Translation> translations = translationRepository.findAllByPostIdInAndLanguageCode(postIds, languageCode);

        Map<Long, Translation> trMap = translations.stream()
                .collect(Collectors.toMap(
                        tr -> tr.getPost().getId(),
                        tr -> tr
                ));

        List<PostResponse> dtoList = posts.stream()
                .map(post -> {
                    Translation tr = trMap.get(post.getId());
                    List<TagForm> tags = post.getPostTags().stream()
                            .map(pt -> new TagForm(pt.getTag().getId(), pt.getTag().getName()))
                            .toList();
                    return PostResponse.ofForList(post, tr, languageCode, tags);
                })
                .toList();

        return new PagedResponse<>(dtoList, pageable.getPageNumber(), pageable.getPageSize(), postPage.getTotalElements(), postPage.getTotalPages());
    }
}
