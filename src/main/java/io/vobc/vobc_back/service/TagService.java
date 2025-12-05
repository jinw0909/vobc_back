package io.vobc.vobc_back.service;

import io.vobc.vobc_back.domain.Post;
import io.vobc.vobc_back.domain.PostTag;
import io.vobc.vobc_back.domain.Tag;
import io.vobc.vobc_back.exception.DuplicateTagException;
import io.vobc.vobc_back.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagService {

    private final TagRepository tagRepository;

    @Transactional
    public Tag createTag(String name) {

        boolean exists = tagRepository.existsByName(name);

        if (exists) {
            throw new DuplicateTagException("Tag already exists: " + name);
        }

        return tagRepository.save(new Tag(name));
    }

    @Transactional(readOnly = true)
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Tag getTag(Long id) {
        return tagRepository.findById(id).orElseThrow();
    }

    @Transactional
    public void deleteTag(Long id) {
        tagRepository.deleteById(id);
    }

    @Transactional
    public Tag updateTag(Long id, String newName) {
        Tag tag = tagRepository.findById(id).orElseThrow();

        if (tag.getName().equals(newName)) {
            return tag;
        }

        if (tagRepository.existsByName(newName)) {
            throw new DuplicateTagException("Tag already exists: " + newName);
        }

        tag.changeName(newName);
        return tag;
    }

    public List<Post> getPostsByTagId(Long id) {

        Tag tag = tagRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Tag not found: " + id));
        return tag.getPostTags().stream()
                .map(PostTag::getPost)
                .distinct()
                .toList();
    }
}
