package io.vobc.vobc_back.service;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.post.Post;
import io.vobc.vobc_back.domain.post.Translation;
import io.vobc.vobc_back.dto.post.PostDto;
import io.vobc.vobc_back.dto.post.PostQueryDto;
import io.vobc.vobc_back.dto.post.PostTagResponse;
import io.vobc.vobc_back.dto.post.PostTranslatedResponse;
import io.vobc.vobc_back.dto.postTag.PostTagQueryDto;
import io.vobc.vobc_back.dto.translation.TranslationQueryDto;
import io.vobc.vobc_back.repository.PostQueryRepository;
import io.vobc.vobc_back.repository.PostRepository;
import io.vobc.vobc_back.repository.TagRepository;
import io.vobc.vobc_back.repository.TranslationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryService {

    private final PostQueryRepository postQueryRepository;
    private final TagRepository tagRepository;
    private final TranslationRepository translationRepository;
    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public List<PostQueryDto> findAllByDto() {
        List<PostQueryDto> result = postQueryRepository.findAllPostsByDto();
        attachTags(result);
        return result;
    }

//    public Page<PostQueryDto> findAllByDto(Pageable pageable, LanguageCode languageCode) {
//        Page<PostQueryDto> page = postQueryRepository.findAllPostsByDto(pageable);
//        List<PostQueryDto> content = page.getContent();
//        attachTags(content);
//        attachTranslations(content);
//        applyLanguage(content, languageCode);
//        return page;
//    }

    @Transactional(readOnly = true)
    public Page<PostQueryDto> findAllByDto(Pageable pageable, LanguageCode languageCode, String tagName) {

        Page<PostQueryDto> page;

        if (tagName == null || tagName.isBlank()) {
            page = postQueryRepository.findAllPostsByDto(pageable);
        } else {
            page = postQueryRepository.findAllPostsByDto(pageable, tagName);
        }

        List<PostQueryDto> content = page.getContent();
        attachTags(content);
        attachTranslations(content);
        applyLanguage(content, languageCode);

        return page;
    }

    @Transactional(readOnly = true)
    public PostDto findOneById(Long postId, LanguageCode languageCode) {
        Post post = postQueryRepository.findOneById(postId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // 기본값 en으로 두고 싶다면 여기서 처리
        if (languageCode == null) {
            languageCode = LanguageCode.en;
        }
        LanguageCode languageCodeFinal = languageCode;

        PostDto dto = new PostDto(post);

        // en 이 “원문”이 아니라, 따로 Translation row 로도 있을 수 있으면
        // 아래 로직 그대로 사용
        Translation tr = post.getTranslations().stream()
                .filter(t -> t.getLanguageCode() == languageCodeFinal)
                .findFirst()
                .orElse(null);

        if (tr == null) {
            // 해당 언어 번역이 없으면 원문 그대로 반환
            return dto;
        }

        dto.setTitle(tr.getTitle());
        dto.setContent(tr.getContent());
        dto.setSummary(tr.getSummary());
        dto.setAuthor(tr.getAuthor());

        return dto;
    }


    private void attachTags(List<PostQueryDto> result) {
        if (result.isEmpty()) { return; }

        // postId 리스트 추출
        List<Long> postIds = result.stream().map(PostQueryDto::getPostId).toList();

        // postTag DTO 한 방에 조회
        List<PostTagQueryDto> postTags = postQueryRepository.findAllPostTagsByQueryDto(postIds);

        // Map<Long, List<PostTagQueryDto>>로 변환
        Map<Long, List<PostTagQueryDto>> postTagMap = postTags.stream().collect(Collectors.groupingBy(PostTagQueryDto::getPostId));

        // 각 PostQueryDto에 세팅
        result.forEach(dto -> dto.setPostTags(postTagMap.getOrDefault(dto.getPostId(), List.of())));
    }

    private void attachTranslations(List<PostQueryDto> result) {
        if (result.isEmpty()) { return; }
        // postId 리스트 추출
        List<Long> postIds = result.stream().map(PostQueryDto::getPostId).toList();
        List<TranslationQueryDto> translations = postQueryRepository.findAllTranslationsByQueryDto(postIds);
        Map<Long, List<TranslationQueryDto>> translationMap = translations.stream().collect(Collectors.groupingBy(TranslationQueryDto::getPostId));
        result.forEach(dto -> dto.setTranslations(translationMap.getOrDefault(dto.getPostId(), List.of())));
    }

    private void applyLanguage(List<PostQueryDto> result, LanguageCode languageCode) {
        if (result.isEmpty()) return;
        if (languageCode == null) {
            languageCode = LanguageCode.en;
        }

        LanguageCode languageCodeFinal = languageCode;

        for (PostQueryDto dto : result) {
            List<TranslationQueryDto> translations = dto.getTranslations();
            if (translations == null || translations.isEmpty()) {
                continue; //번역이 없으면 원본 그대로
            }

            TranslationQueryDto matched = translations.stream()
                    .filter(t -> languageCodeFinal == t.getLanguageCode())
                    .findFirst()
                    .orElse(null);

            if (matched == null) {
                continue;
            }

            dto.setTitle(matched.getTitle());
            dto.setContent(matched.getContent());
            dto.setSummary(matched.getSummary());
            dto.setAuthor(matched.getAuthor());
        }

    }

    @Transactional(readOnly = true)
    public PostTranslatedResponse getFeatured(LanguageCode languageCode) {

        PostTranslatedResponse post = postQueryRepository.findFeaturedTranslated(languageCode, PageRequest.of(0, 1)).stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        List<PostTagResponse> postTags = postRepository.findAllPostTagsByPostId(post.getId());
        post.setPostTags(postTags);

        return post;
    }

    @Transactional(readOnly = true)
    public Page<PostTranslatedResponse> getRest(Pageable pageable, Long featuredId, LanguageCode languageCode) {
        Page<PostTranslatedResponse> posts = postQueryRepository.findPagedResponse(pageable, featuredId, languageCode);
        List<Long> postIds = posts.map(PostTranslatedResponse::getId).toList();

        if (postIds.isEmpty()) return posts;

        List<PostTagResponse> postTags = postQueryRepository.findAllPostTagsByResponse(postIds);

        Map<Long, List<PostTagResponse>> postTagMap = postTags.stream().collect(Collectors.groupingBy(PostTagResponse::getPostId));
        posts.forEach(dto -> dto.setPostTags(postTagMap.getOrDefault(dto.getId(), List.of())));

        return posts;
    }
}
