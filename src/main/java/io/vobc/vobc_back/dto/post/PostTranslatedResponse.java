package io.vobc.vobc_back.dto.post;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.Post;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter
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

    public PostTranslatedResponse() {
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
