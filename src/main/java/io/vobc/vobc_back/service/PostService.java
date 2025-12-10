package io.vobc.vobc_back.service;

import io.vobc.vobc_back.domain.*;
import io.vobc.vobc_back.domain.member.Member;
import io.vobc.vobc_back.dto.PagedResponse;
import io.vobc.vobc_back.dto.TagForm;
import io.vobc.vobc_back.dto.post.PostCreateRequest;
import io.vobc.vobc_back.dto.post.PostResponse;
import io.vobc.vobc_back.dto.post.PostUpdateRequest;
import io.vobc.vobc_back.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final TranslationRepository translationRepository;
    private final MemberRepository memberRepository;
    private final PostTagRepository postTagRepository;

    @Transactional
    public Long createPost(Long memberId, PostCreateRequest request) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));

        Post post = Post.createPost(
                member, request.getTitle(), request.getContent(), request.getSummary(), request.getAuthor(), request.getReleaseDate(), request.getThumbnail());

        if (request.getTagIds() != null) {
            for (Long tagId : request.getTagIds()) {
                post.addTag(tagRepository.findById(tagId).orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagId)));
            }
            normalizePostTagOrder(post);
        }

        postRepository.save(post);

        return post.getId();
    }

    @Transactional(readOnly = true)
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Post> getPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Post> getAllPosts(Pageable pageable) {
        return postRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Post findWithTagsById(Long id) {
        return postRepository.findWithTagsById(id).orElseThrow(() -> new IllegalArgumentException("Post not found: " + id));
    }

    @Transactional(readOnly = true)
    public Post getPost(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Post not found: " + id));
    }

    @Transactional
    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    @Transactional
    public Post updatePost(Long id, PostUpdateRequest request) {
        Post post = postRepository.findById(id).orElseThrow();

        post.update(request.getTitle(), request.getContent(), request.getSummary(), request.getAuthor(), request.getReleaseDate(), request.getThumbnail());

        updatePostTagsFromForm(post, request.getTagIds());

        normalizePostTagOrder(post);

        return post;
    }

    private void updatePostTagsFromForm(Post post, List<Long> tagIds) {
        // 1) 태그를 모두 제거하는 케이스
        if (tagIds == null || tagIds.isEmpty()) {
            for (PostTag pt : new ArrayList<>(post.getPostTags())) {
                post.removeTag(pt.getTag());
            }
            return;
        }

        // 2) 현재 태그: tagId -> PostTag
        Map<Long, PostTag> currentMap = post.getPostTags().stream()
                .collect(Collectors.toMap(
                        pt -> pt.getTag().getId(),
                        pt -> pt
                ));

        Set<Long> incomingIds = new HashSet<>(tagIds);

        // 3) 요청에 없는 기존 태그 제거
        for (PostTag pt : new ArrayList<>(post.getPostTags())) {
            Long tagId = pt.getTag().getId();
            if (!incomingIds.contains(tagId)) {
                post.removeTag(pt.getTag());
                currentMap.remove(tagId);
            }
        }

        // 4) 요청에 있는 태그들 중에서, 아직 없는 태그는 새로 추가
        //    새 태그의 sortOrder는 일단 "가장 큰 값보다 뒤"로 보내두고, 나중에 normalize에서 정리
        int currentMaxOrder = post.getPostTags().stream()
                .map(PostTag::getSortOrder)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);

        for (Long tagId : tagIds) {
            PostTag postTag = currentMap.get(tagId);
            if (postTag == null) {
                Tag tag = tagRepository.findById(tagId)
                        .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagId));
                postTag = post.addTag(tag);
                postTag.setSortOrder(++currentMaxOrder); // 맨 뒤에 붙여두기
            }
            // 기존 태그는 sortOrder 그대로 둠 (이미 값이 있는 상태)
        }

        // 5) 최종 정렬: 1,2,3,... 으로 압축
        normalizePostTagOrder(post);
    }

    @Transactional
    public List<PostTag> addTagToPost(Long postId, Long tagId) {
        Post post = postRepository.findById(postId).orElseThrow();
        Tag tag = tagRepository.findById(tagId).orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagId));

        boolean exists = post.getPostTags().stream()
                .anyMatch(pt -> pt.getTag().equals(tag));

        if (!exists) {
            PostTag pt = post.addTag(tag);

            // 새 태그는 맨 뒤로 보내기 — normalize가 정리할 예정
            int max = post.getPostTags().stream()
                    .map(PostTag::getSortOrder)
                    .filter(Objects::nonNull).max(Integer::compareTo)
                    .orElse(0);
            pt.setSortOrder(max + 1);
        }

        normalizePostTagOrder(post);

        return getSortedPostTags(post);
    }

    private List<PostTag> getSortedPostTags(Post post) {
        return post.getPostTags().stream()
                .sorted(Comparator.comparing(PostTag::getSortOrder)
                        .thenComparing(PostTag::getId))
                .collect(Collectors.toList());
    }

    @Transactional
    public List<PostTag> removeTagFromPost(Long postId, Long tagId) {
        Post post = postRepository.findById(postId).orElseThrow();
        post.getPostTags().removeIf(pt -> pt.getTag().getId().equals(tagId));

        normalizePostTagOrder(post);

        return getSortedPostTags(post);
    }

    @Transactional
    public List<PostTag> updateTagOrder(Long postId, Long tagId, Integer newOrder) {
        Post post = postRepository.findById(postId).orElseThrow();

        PostTag postTag = post.getPostTags().stream()
                .filter(pt -> pt.getTag().getId().equals(tagId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagId));
        postTag.setSortOrder(newOrder != null ? newOrder : 0);

        normalizePostTagOrder(post);
        return getSortedPostTags(post);
    }

    private void normalizePostTagOrder(Post post) {
        List<PostTag> tags = new ArrayList<>(post.getPostTags());
        if (tags.isEmpty()) { return; }

        Collator collator = Collator.getInstance(Locale.KOREAN);

        tags.sort(
                Comparator
                        .comparing((PostTag pt) -> pt.getSortOrder() == null ? Integer.MAX_VALUE : pt.getSortOrder())
                        .thenComparing(pt -> pt.getTag().getName(), collator));

        int order = 1;
        for (PostTag pt : tags) {
            pt.setSortOrder(order++);
        }
    }

    @Transactional(readOnly = true)
    public List<Post> findRelatedPosts(Long id) {
        // 1. 기준 포스트 조회
        Post base = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Post not found: " + id));

        List<PostTag> basePostTags = base.getPostTags();
        if (basePostTags.isEmpty()) {
            return List.of();
        }

        // 2. 기준 포스트 태그별 baseWeight 계산
        Map<Tag, Integer> baseWeights = basePostTags.stream()
                .collect(Collectors.toMap(
                        PostTag::getTag,
                        pt -> baseWeightBySortOrder(pt.getSortOrder()),
                        Integer::max
                ));

        List<Tag> baseTagsForSearch = baseWeights.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .map(Map.Entry::getKey)
                .toList();

        if (baseTagsForSearch.isEmpty()) {
            return List.of();
        }

        // 3. 공통 태그를 가진 후보 포스트들 조회 (자기 자신 제외)
        List<Post> candidates = postRepository.findCandidatesByTags(baseTagsForSearch, base.getId());
        if (candidates.isEmpty()) {
            return List.of();
        }

        //4. 후보 포스트별 점수 계산
        Map<Long, Integer> scoreMap = new HashMap<>();

        for (Post candidate : candidates) {
            int score = 0;

            for (PostTag candidatePt : candidate.getPostTags()) {
                Tag tag = candidatePt.getTag();

                Integer baseWeight = baseWeights.get(tag);
                if (baseWeight == null || baseWeight <= 0) { continue; }

                Integer candidateSortOrder = candidatePt.getSortOrder();
                int partialScore = scoreByCandidateSort(baseWeight, candidateSortOrder);
                score += partialScore;

            }

            if (score > 0) {
                scoreMap.put(candidate.getId(), score);
            }
        }

        // 5. 점수 기준 내림차순 + tie-breaker로 createdAt 최신순 등
        return candidates.stream()
                .filter(p -> scoreMap.getOrDefault(p.getId(), 0) > 0)
                .sorted(
                        Comparator
                                .comparingInt((Post p) -> scoreMap.get(p.getId()))
                                .reversed()
                                .thenComparing(Post::getCreatedAt, Comparator.reverseOrder())
                )
                .limit(7)
                .collect(Collectors.toList());
    }

    private int scoreByCandidateSort(Integer baseWeight, Integer candidateSortOrder) {
        if (candidateSortOrder == null) { return 0; }
        return switch (candidateSortOrder) {
            case 1 -> baseWeight;
            case 2 -> baseWeight / 2;
            case 3 -> baseWeight / 3;
            case 4 -> baseWeight / 4;
            default -> 0;
        };
    }

    private Integer baseWeightBySortOrder(Integer sortOrder) {
        if (sortOrder == null) { return 0; }
        return switch (sortOrder) {
            case 1 -> 100;
            case 2 -> 50;
            case 3 -> 25;
            case 4 -> 10;
            default -> 0;
        };
    }

    @Transactional(readOnly = true)
    public PagedResponse<PostResponse> getPosts(LanguageCode languageCode, Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("createdAt").descending());
        }
        Page<Post> postPage = postRepository.findAll(pageable);
        List<Post> posts = postPage.getContent();

        if (posts.isEmpty()) {
            return new PagedResponse<>(
                    List.of(), postPage.getNumber(), postPage.getSize(), postPage.getTotalElements(), postPage.getTotalPages()
            );
        }

        // 1) 이 페이지에 포함된 Post ID 목록
        List<Long> postIds = posts.stream()
                .map(Post::getId)
                .toList();

        // 2) 번역: post_id IN + languageCode
        List<Translation> translations = translationRepository.findAllByPostIdInAndLanguageCode(postIds, languageCode);

        Map<Long, Translation> trMap = translations.stream()
                .collect(Collectors.toMap(
                        tr -> tr.getPost().getId(),
                        tr -> tr
                ));

        // 3) 태그: post_id IN + EntityGraph(tag)
        Sort tagSort = Sort
                .by("post_id").ascending()
                .and(Sort.by("sortOrder").ascending())
                .and(Sort.by("id").descending());

        List<PostTag> postTags = postTagRepository.findByPost_IdIn(postIds, tagSort);

        Map<Long, List<TagForm>> tagMap = postTags.stream()
                .collect(Collectors.groupingBy(
                        pt -> pt.getPost().getId(),
                        Collectors.mapping(pt -> new TagForm(pt.getTag().getId(), pt.getTag().getName()), Collectors.toList())
                ));

        // 4) DTO 조립
        List<PostResponse> dtoList = posts.stream()
                .map(post -> {
                    Translation tr = trMap.get(post.getId());
                    List<TagForm> tags = tagMap.getOrDefault(post.getId(), List.of());
                    return PostResponse.ofForList(post, tr, languageCode, tags);
                })
                .toList();

        return new PagedResponse<>(dtoList, postPage.getNumber(), postPage.getSize(), postPage.getTotalElements(), postPage.getTotalPages());
    }


    public Post findPostWithTagsById(Long id) {
        return null;
    }

    public PostResponse getPostDetail(Long id, LanguageCode languageCode) {
        // 1) 태그까지 한 번에 가져온 Post
        Post post = postRepository.findWithTagsById(id).orElseThrow(() -> new IllegalArgumentException("Post not found: " + id));

        // 2) 요청 언어 번역
        Translation tr = translationRepository.findByPostIdAndLanguageCode(id, languageCode).orElse(null);

        // 3) Post + Translation + Tags로 DTO 조립
        return PostResponse.of(post, tr, languageCode);
    }

}
