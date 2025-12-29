package io.vobc.vobc_back.repository;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.Post;
import io.vobc.vobc_back.dto.post.PostQueryDto;
import io.vobc.vobc_back.dto.post.PostTagResponse;
import io.vobc.vobc_back.dto.post.PostTranslatedResponse;
import io.vobc.vobc_back.dto.postTag.PostTagQueryDto;
import io.vobc.vobc_back.dto.translation.TranslationQueryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostQueryRepository extends JpaRepository<Post, Long> {

    // 루트 조회용
    @Query("""
        select new io.vobc.vobc_back.dto.post.PostQueryDto(
            p.id,
            p.title,
            p.content,
            p.summary,
            p.author,
            p.thumbnail,
            p.releaseDate
        )
        from Post p
    """)
    List<PostQueryDto> findAllPostsByDto();

    // 루트 조회용 + 페이징
    @Query(
        value = """
            select new io.vobc.vobc_back.dto.post.PostQueryDto(
                p.id,
                p.title,
                p.content,
                p.summary,
                p.author,
                p.thumbnail,
                p.releaseDate
            )
            from Post p
            order by p.id desc
        """,
        countQuery = """
            select count(p)
            from Post p
        """
    )
    Page<PostQueryDto> findAllPostsByDto(Pageable pageable);


    @Query(
        value = """
            select new io.vobc.vobc_back.dto.post.PostQueryDto(
                p.id,
                p.title,
                p.content,
                p.summary,
                p.author,
                p.thumbnail,
                p.releaseDate
            )
            from Post p
            join p.postTags pt
            join pt.tag t
            where t.name = :tagName
            order by p.id desc
        """,
        countQuery = """
            select count(distinct p)
            from Post p
            join p.postTags pt
            join pt.tag t
            where t.name = :tagName
        """
    )
    Page<PostQueryDto> findAllPostsByDto(Pageable pageable, @Param("tagName") String tagName);




    // 컬렉션 조회용

    @Query("""
        select new io.vobc.vobc_back.dto.postTag.PostTagQueryDto(
            pt.post.id,
            t.name,
            pt.sortOrder,
            pt.primaryTag
        )
        from PostTag pt
        join pt.tag t
        where pt.post.id in :postIds
        order by pt.sortOrder asc, pt.id desc
    """)
    List<PostTagQueryDto> findAllPostTagsByQueryDto(@Param("postIds") List<Long> postIds);



    @Query("""
        select new io.vobc.vobc_back.dto.translation.TranslationQueryDto(
            t.post.id,
            t.title,
            t.content,
            t.summary,
            t.author,
            t.languageCode
        )
        from Translation t
        where t.post.id in :postIds
    """)
    List<TranslationQueryDto> findAllTranslationsByQueryDto(@Param("postIds") List<Long> postIds);


    @Query("""
        select p
        from Post p
        join fetch p.member m
        left join fetch p.postTags pt
        left join fetch pt.tag t
        where p.id = :postId
    """)
    Optional<Post> findOneWithMemberById(@Param("postId") Long postId);

    @Query("""
        select distinct p
        from Post p
        left join fetch p.postTags pt
        left join fetch pt.tag t
        where p.id = :postId
    """)
    Optional<Post> findOneById(@Param("postId") Long postId);

    @EntityGraph(attributePaths = {"postTags", "postTags.tag"})
    List<Post> findTop1ByOrderByReleaseDateDesc();

    @Query(value = """
    select new io.vobc.vobc_back.dto.post.PostTranslatedResponse(
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
    where (:featuredId is null or p.id <> :featuredId)
    order by p.releaseDate desc
    """,
                countQuery = """
    select count(p)
    from Post p
    where (:featuredId is null or p.id <> :featuredId)
    """)
    Page<PostTranslatedResponse> findPagedResponse(
            Pageable pageable,
            @Param("featuredId") Long featuredId,
            @Param("languageCode") LanguageCode languageCode
    );

    @Query("""
    select new io.vobc.vobc_back.dto.post.PostTagResponse(
        t.id,
        t.name,
        pt.sortOrder,
        p.id
    )
    from PostTag pt
    join pt.post p
    join pt.tag t
    where p.id in :postIds
    order by p.id asc, pt.sortOrder asc, pt.id desc
    """)
    List<PostTagResponse> findAllPostTagsByResponse(@Param("postIds") List<Long> postIds);
}
