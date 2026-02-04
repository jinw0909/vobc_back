package io.vobc.vobc_back.service;

import io.vobc.vobc_back.domain.article.Topic;
import io.vobc.vobc_back.dto.TopicForm;
import io.vobc.vobc_back.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;

    @Transactional(readOnly = true)
    public Page<Topic> getAll(Pageable pageable) {
        return topicRepository.findAll(pageable);
    }

    @Transactional
    public Long save(TopicForm form) {
        Topic topic = Topic.create(form.getId(), form.getName());
        topicRepository.save(topic);
        return topic.getId();
    }

    @Transactional(readOnly = true)
    public Topic getOneById(Long topicId) {
        return topicRepository.findWithArticlesById(topicId).orElseThrow(() -> new IllegalArgumentException("Topic not found: " + topicId));
    }

    @Transactional(readOnly = true)
    public Topic getOne(Long topicId) {
        return topicRepository.findById(topicId).orElseThrow(() -> new IllegalArgumentException("Topic not found: " + topicId));
    }

    @Transactional
    public Long update(Long topicId, TopicForm topicForm) {
        Topic topic = topicRepository.findById(topicId).orElseThrow(() -> new IllegalArgumentException("Topic not found: " + topicForm.getId()));
        topic.setName(topicForm.getName());
        return topic.getId();
    }

    @Transactional(readOnly = true)
    public List<Topic> getAllTopics() {
        return topicRepository.findAll();
    }
}
