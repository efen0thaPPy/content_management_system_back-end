package cdn.cdn_project.Repos;

import cdn.cdn_project.Entities.CastModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CastRepo extends JpaRepository<CastModel,Integer> {
    Optional<CastModel> findByNormalizedName(String normalizedName);

    Page<CastModel> findCastModelByNameContainingIgnoreCase(Pageable pageable,String name );
    
}
