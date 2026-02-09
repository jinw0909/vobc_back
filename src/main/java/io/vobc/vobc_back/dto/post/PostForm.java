package io.vobc.vobc_back.dto.post;

import io.vobc.vobc_back.domain.post.Post;
import io.vobc.vobc_back.dto.TagForm;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@ToString
@NoArgsConstructor
@Getter @Setter
public class PostForm {

    private Long id;
    private String title;
    private String content;
    private String summary;
    private String author;
    private LocalDate releaseDate;
    private String thumbnail;

    private List<PostTagForm> postTags = new ArrayList<>();
    private List<TagForm> tags = new ArrayList<>();
    private List<MultipartFile> files = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PostForm(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.summary = post.getSummary();
        this.author = post.getAuthor();
        this.releaseDate = post.getReleaseDate();
        this.thumbnail = post.getThumbnail();

        if (post.getPostTags() != null) {
            this.postTags = post.getPostTags().stream()
                    .map(PostTagForm::new)
                    .toList();
        }
    }

    public static PostForm from(Post p) {
        PostForm f = new PostForm();
        f.setId(p.getId());
        f.setTitle(p.getTitle());
        f.setContent(p.getContent());
        f.setSummary(p.getSummary());
        f.setAuthor(p.getAuthor());
        f.setReleaseDate(p.getReleaseDate());
        f.setThumbnail(p.getThumbnail());

        List<TagForm> tagFormList = p.getPostTags().stream()
                .map(pt -> new TagForm(pt.getTag().getId(), pt.getTag().getName()))
                .toList();

        f.setTags(tagFormList);

        return f;
    }
}
