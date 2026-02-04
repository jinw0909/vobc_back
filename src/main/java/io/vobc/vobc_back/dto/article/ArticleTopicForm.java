package io.vobc.vobc_back.dto.article;

import io.vobc.vobc_back.domain.article.ArticleTopic;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ArticleTopicForm {

    private Long id;
    private Long topicId;
    private Long articleId;
    private boolean primaryTopic;
    private int sortOrder;

    public ArticleTopicForm(ArticleTopic articleTopic) {
        this.id = articleTopic.getId();
        this.topicId = articleTopic.getTopic().getId();
        this.articleId = articleTopic.getArticle().getId();
        this.primaryTopic = articleTopic.getPrimaryTopic();
        this.sortOrder = articleTopic.getSortOrder();
    }
}
