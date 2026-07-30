package cdn.cdn_project.Repos;

import cdn.cdn_project.Entities.EpisodeModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EpisodeRepo extends JpaRepository<EpisodeModel,String> {
}
