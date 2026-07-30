package cdn.cdn_project.Repos;

import cdn.cdn_project.Entities.SeasonModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeasonRepo extends JpaRepository<SeasonModel,String> {
}
