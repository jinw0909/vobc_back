package io.vobc.vobc_back.dto.article;

import io.vobc.vobc_back.domain.article.Article;
import io.vobc.vobc_back.domain.article.Category;
import io.vobc.vobc_back.dto.TopicForm;
import io.vobc.vobc_back.dto.publisher.PublisherForm;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
public class ArticleForm {
    private Long id;
    private String title;
    private String content;
    private String summary;
    private String description;
    private LocalDate releaseDate;
    private String author;
    private String thumbnail;
    private String link;
    private Category category;
    private Long publisherId;

    private PublisherForm publisher;

    private List<ArticleTopicForm> articleTopics = new ArrayList<>();

    private List<MultipartFile> files;

    public ArticleForm(Article article) {
        id = article.getId();
        title = article.getTitle();
        content = article.getContent();
        summary = article.getSummary();
        description = article.getDescription();
        releaseDate = article.getReleaseDate();
        author = article.getAuthor();
        thumbnail = article.getThumbnail();
        link = article.getLink();
        category = article.getCategory();

        if (article.getPublisher() != null) {
            PublisherForm publisherForm = new PublisherForm();
            publisherForm.setId(article.getPublisher().getId());
            publisherForm.setName(article.getPublisher().getName());
            publisher = publisherForm;
        }

        if (article.getTopics() != null) {
            this.articleTopics.addAll(article.getTopics().stream()
                    .map(ArticleTopicForm::new).toList());
        }
    }


}
