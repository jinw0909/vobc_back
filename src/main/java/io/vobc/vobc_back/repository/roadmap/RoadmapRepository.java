package io.vobc.vobc_back.repository.roadmap;

import io.vobc.vobc_back.domain.roadmap.Roadmap;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {
}
