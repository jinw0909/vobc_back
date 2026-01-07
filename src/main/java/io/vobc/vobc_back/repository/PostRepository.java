package io.vobc.vobc_back.repository;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.post.Post;
import io.vobc.vobc_back.domain.Tag;
import io.vobc.vobc_back.domain.post.PostTag;
import io.vobc.vobc_back.dto.post.PostResponse;
import io.vobc.vobc_back.dto.post.PostTagResponse;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("""
        select distinct p
        from Post p
        join fetch p.postTags pt
        where pt.tag in :tags
            and p.id <> :postId
    """)
    List<Post> findCandidatesByTags(@Param("tags") List<Tag> tags,
                                    @Param("postId") Long postId);

    @NonNull
    @EntityGraph(attributePaths = {"postTags", "postTags.tag"})
    @Query("select p from Post p")
    Page<Post> findAllWithTags(@NonNull Pageable pageable);

    @EntityGraph(attributePaths = {"postTags", "postTags.tag"})
    Optional<Post> findWithTagsById(Long id);

    @EntityGraph(attributePaths = {"postTags", "postTags.tag"})
    Page<Post> findByPostTags_Tag_Id(Long tagId, Pageable pageable);

    @Query("select pt from PostTag pt join fetch pt.tag join fetch pt.post p where pt.post.id in :postIds")
    List<PostTag> findAllPostTags(@Param("postIds") List<Long> postIds);

    @Query("""
        select new io.vobc.vobc_back.dto.post.PostResponse(
           p.id,
           coalesce(tr.title, p.title),
           coalesce(tr.content, p.content),
           coalesce(tr.summary, p.summary),
           coalesce(tr.author, p.author),
           p.thumbnail,
           p.releaseDate
        )
        from Post p
        left join Translation tr
          on tr.post.id = p.id
         and tr.languageCode = :languageCode
        where p.id = :id
    """)
    Optional<PostResponse> findPostWithTranslation(@Param("id") Long id,
                                         @Param("languageCode") LanguageCode languageCode);

    @Query("""
        select new io.vobc.vobc_back.dto.post.PostTagResponse(
            pt.sortOrder,
            pt.primaryTag,
            t.name
        )
        from PostTag pt
        join pt.tag t
        where pt.post.id = :id
        order by pt.primaryTag desc, pt.sortOrder asc
    """)
    List<PostTagResponse> findAllPostTagsByPostId(@Param("id") Long id);

    @EntityGraph(attributePaths = {"translations"})
    Optional<Post> findWithTranslationById(Long postId);
}
