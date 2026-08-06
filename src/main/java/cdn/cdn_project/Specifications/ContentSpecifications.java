package cdn.cdn_project.Specifications;


import cdn.cdn_project.Entities.CastModel;
import cdn.cdn_project.Entities.ContentModel;
import cdn.cdn_project.Enums.CastType;
import cdn.cdn_project.Enums.ContentType;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.BeanRegistry;
import org.springframework.data.jpa.domain.Specification;

import java.util.IllegalFormatConversionException;

public class ContentSpecifications {

    public static Specification<ContentModel>searchByTitle(String title){

        return (root,query,criteriaBuilder)->{

            if(title==null || title.trim().isEmpty()) return null;

            return criteriaBuilder.like
                    (criteriaBuilder.lower(root.get("title")),"%"+title.toLowerCase()+"%");
        };
    }
    public static Specification<ContentModel>searchByYear(String year){
        return (root, query, criteriaBuilder)->
        {
            if(year==null || year.trim().isEmpty()) return null;

            return criteriaBuilder.like(root.get("year"),"%"+year+"%");
        };

    }

    public static Specification<ContentModel>searchByActorName(String actorName){
        return (root, query, criteriaBuilder)->
        {
            if(actorName==null || actorName.trim().isEmpty()) return null;

            Join<ContentModel, CastModel> joinTable=root.join("casts");

            Predicate nameMatches=criteriaBuilder.like(criteriaBuilder.lower(joinTable.get("name")),"%"+actorName.toLowerCase()+"%");


            return criteriaBuilder.and(nameMatches);


        };

    }

    public static Specification<ContentModel>searchByContentType(String contentType){
        return (root,query,criteriaBuilder)->{

            if(contentType == null || contentType.trim().isEmpty()) return null;

            try{
                ContentType c=ContentType.valueOf(contentType);
                return criteriaBuilder.equal(root.get("type"),c);

            }
            catch (IllegalArgumentException ex){
                return null;

            }

        };
    }

    public static Specification<ContentModel>searchByPlot(String plot){
        return (root, query, criteriaBuilder)->
        {
            if(plot==null || plot.trim().isEmpty()) return null;

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("plot")),"%"+plot+"%");


        };
    }

}
