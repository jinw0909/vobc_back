package io.vobc.vobc_back.dto.post;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.vobc.vobc_back.domain.post.Post;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Data
@Getter @Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostTranslatedResponse {

    private Long id;
    private String title;
    private String content;
    private String summary;
    private String author;
    private String thumbnail;
    private LocalDate releaseDate;
    private List<PostTagResponse> postTags;

    public PostTranslatedResponse(Long id,
                                  String title,
                                  String content,
                                  String summary,
                                  String author,
                                  String thumbnail,
                                  LocalDate releaseDate
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.summary = summary;
        this.author = author;
        this.thumbnail = thumbnail;
        this.releaseDate = releaseDate;

    }

    // ✅ related 전용 생성자 (content 제외)
    public PostTranslatedResponse(Long id,
                                  String title,
                                  String summary,
                                  String author,
                                  String thumbnail,
                                  LocalDate releaseDate) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.author = author;
        this.thumbnail = thumbnail;
        this.releaseDate = releaseDate;
        // content는 null → JSON에서 자동 제외됨
    }

    public static PostTranslatedResponse from(Post post) {
        PostTranslatedResponse postTranslatedResponse = new PostTranslatedResponse();
        postTranslatedResponse.setId(post.getId());
        postTranslatedResponse.setTitle(post.getTitle());
        postTranslatedResponse.setContent(post.getContent());
        postTranslatedResponse.setSummary(post.getSummary());
        postTranslatedResponse.setAuthor(post.getAuthor());
        postTranslatedResponse.setThumbnail(post.getThumbnail());
        postTranslatedResponse.setReleaseDate(post.getReleaseDate());

        List<PostTagResponse> list = post.getPostTags().stream().map(PostTagResponse::from).toList();
        postTranslatedResponse.setPostTags(list);

        return postTranslatedResponse;
    }


}
