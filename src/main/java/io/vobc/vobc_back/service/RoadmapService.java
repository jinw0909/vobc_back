package io.vobc.vobc_back.service;

import io.vobc.vobc_back.domain.LanguageCode;
import io.vobc.vobc_back.domain.roadmap.Roadmap;
import io.vobc.vobc_back.domain.roadmap.RoadmapTranslation;
import io.vobc.vobc_back.dto.roadmap.RoadmapForm;
import io.vobc.vobc_back.repository.roadmap.RoadmapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final S3Uploader s3Uploader;


    @Transactional(readOnly = true)
    public Page<Roadmap> getRoadmapList(Pageable pageable) {
        return roadmapRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Roadmap getRoadmapById(Long roadmapId) {
        return roadmapRepository.findById(roadmapId).orElseThrow(() -> new IllegalArgumentException("Roadmap not found: " + roadmapId));
    }

    @Transactional
    public Long create(Long roadmapId, RoadmapForm form) {
        if (roadmapRepository.existsById(roadmapId)) {
            throw new IllegalArgumentException("Roadmap already exists: " + roadmapId);
        }

        Roadmap roadmap = Roadmap.create(
                form.getTitle(),
                form.getDescription(),
                form.getContent(),
                form.getDisplayOrder(),
                form.getRoadmapDate(),
                form.getThumbnail()
        );

        roadmapRepository.save(roadmap);
        return roadmap.getId();
    }

    @Transactional
    public Long edit(Long roadmapId, RoadmapForm form) {
        Roadmap roadmap = roadmapRepository.findById(roadmapId).orElseThrow(() -> new IllegalArgumentException("Roadmap not found: " + roadmapId));
        roadmap.setTitle(form.getTitle());
        roadmap.setDescription(form.getDescription());
        roadmap.setContent(form.getContent());
        roadmap.setDisplayOrder(form.getDisplayOrder());
        roadmap.setRoadmapDate(form.getRoadmapDate());
        roadmap.setThumbnail(form.getThumbnail());
        return roadmap.getId();
    }

    @Transactional(readOnly = true)
    public RoadmapTranslation getTranslationByIdAndLangCode(Long roadmapId, LanguageCode languageCode) {
        return null;
    }

    @Transactional(readOnly = true)
    public Set<LanguageCode> getAllLangCodeById(Long roadmapId) {
        return null;
    }
}
