package cdn.cdn_project.Repos;

import cdn.cdn_project.Entities.CastModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface CastRepo extends JpaRepository<CastModel,Integer>, JpaSpecificationExecutor<CastModel> {
    Optional<CastModel> findByNormalizedName(String normalizedName);

    Page<CastModel> findCastModelByNameContainingIgnoreCase(Pageable pageable,String name );
    
}
