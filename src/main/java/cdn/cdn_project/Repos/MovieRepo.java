package cdn.cdn_project.Repos;

import cdn.cdn_project.Entities.ContentModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepo extends JpaRepository<ContentModel,String>, JpaSpecificationExecutor<ContentModel> {

    @Query("SELECT c from  ContentModel c join c.casts ca where ca.id= :castId")
    Page<ContentModel> findContentModelByCastId(@Param("castId")int castId, Pageable pageable);



    @Query("SELECT c from  ContentModel c join c.casts ca where ca.id= :castId")

    List<ContentModel> findContentModelByCastId(@Param("castId")int castId);







}
