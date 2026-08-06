package cdn.cdn_project.Specifications;

import cdn.cdn_project.Entities.CastModel;
import cdn.cdn_project.Enums.CastType;
import cdn.cdn_project.Enums.ContentType;
import org.springframework.data.jpa.domain.Specification;

public class CastSpecifications {

    public static Specification<CastModel>searchByName(String query){
        return (root,q,criteriaBuilder)->{
            if(query==null || query.trim().isEmpty())return null;

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),"%"+query+"%");


        };

    }
    public static Specification<CastModel>searchById(String query){
        return (root,q,criteriaBuilder)->{
            if(query==null || !query.matches("\\d+")) return null;

            return criteriaBuilder.equal(root.get("id"),Integer.parseInt(query));


        };

    }
    public static Specification<CastModel>searchByCastType(String query){
        return (root,q,criteriaBuilder)->{
            if(query==null || query.trim().isEmpty())return null;

            try{
                CastType castType= CastType.valueOf(query);
                return criteriaBuilder.equal(root.get("castType"),castType);
            }
            catch(IllegalArgumentException ex){
                return null;
            }


        };

    }
}
