package cdn.cdn_project.Services;

import cdn.cdn_project.Dto.RequestFront.CastRequests.CastPostRequestDto;
import cdn.cdn_project.Dto.RequestFront.CastRequests.CastPutRequestDto;
import cdn.cdn_project.Dto.ResponseFront.CastResponses.SimpleCastResponseDto;
import cdn.cdn_project.Dto.ResponseFront.CastResponses.PaginatedCastResponseDto;
import cdn.cdn_project.Dto.ResponseFront.CastResponses.CastResponseDto;
import cdn.cdn_project.Entities.CastModel;
import cdn.cdn_project.Entities.ContentModel;
import cdn.cdn_project.ExceptionHandling.NotFound;
import cdn.cdn_project.Mapper.CastMapper;
import cdn.cdn_project.Mapper.ContentMapper;
import cdn.cdn_project.Repos.CastRepo;
import cdn.cdn_project.Repos.MovieRepo;
import cdn.cdn_project.Specifications.CastSpecifications;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CastPostgresLocalServiceImpl implements CastService {

    private final CastRepo castRepo;
    private final CastMapper castMapper;
    private final ContentMapper contentMapper;
    private final MovieRepo movieRepo;

    @Override
    public Page<SimpleCastResponseDto> getCasts(Pageable pageable,String castTypes,String query){


        Specification<CastModel>textSearch=Specification.
                where(CastSpecifications.searchById(query)).
                or(CastSpecifications.searchByName(query));

        Specification<CastModel>spec=textSearch.and(CastSpecifications.searchByCastType(castTypes));

           return castRepo.findAll(spec,pageable).map(castMapper::toSimpleCastDto);




    }
    @Override
    public PaginatedCastResponseDto getCast (int id, Pageable pageable){


        Page<ContentModel>contentModels=movieRepo.findContentModelByCastId(id,pageable);

       CastModel castModel=castRepo.findById(id).
               orElseThrow(()->new NotFound("couldn't find the actor"));

       PaginatedCastResponseDto detailedCastDto=new PaginatedCastResponseDto();
       detailedCastDto.setId(id);
       detailedCastDto.setName(castModel.getName());
        detailedCastDto.setPoster(castModel.getPoster());

           detailedCastDto.setContents(contentModels.map(contentMapper::toSummarizedContentDto));


       return detailedCastDto;



    }
    @Override
    @Transactional
    public CastResponseDto postCast(CastPostRequestDto detailedContentPostDto) {

        String name= detailedContentPostDto.getName();

        CastModel castModel= new CastModel();

        castModel.setName(name);

        String normalizedName= name.trim().toLowerCase().replaceAll("\\s+", " ");

        castModel.setNormalizedName(normalizedName);
        castModel.setPoster(detailedContentPostDto.getPoster());
        castModel.setCastType(detailedContentPostDto.getCastType());

        castRepo.save(castModel);

        List<ContentModel>contentModels=movieRepo.findAllById(Arrays.asList(detailedContentPostDto.getIds()));



        for(ContentModel contentModel:contentModels){
            contentModel.getCasts().add(castModel);


        }
        CastResponseDto putPostCastResponseDto = new CastResponseDto();
        putPostCastResponseDto.setName(castModel.getName());
        putPostCastResponseDto.setId(castModel.getId());
        putPostCastResponseDto.setContents(contentModels.stream().
                map(contentMapper::toSummarizedContentDto).toList());
        putPostCastResponseDto.setCastType(castModel.getCastType());

        return putPostCastResponseDto;

    }

    @Override
    @Transactional
    public CastResponseDto putCast(int id, CastPutRequestDto detailedCastPutPostDto){


        CastModel castModel= castRepo.findById(id).orElseThrow(()->new NotFound("cast not found"));
        if(detailedCastPutPostDto.getName()!=null) castModel.setName(detailedCastPutPostDto.getName());
        if(detailedCastPutPostDto.getPoster()!=null) castModel.setPoster(detailedCastPutPostDto.getPoster());
        if(detailedCastPutPostDto.getName()!=null) {
            String normalizedName= detailedCastPutPostDto.getName().trim().toLowerCase().replaceAll("\\s+", " ");
            castModel.setNormalizedName(normalizedName);
        }
        if(detailedCastPutPostDto.getCastType()!=null)  castModel.setCastType(detailedCastPutPostDto.getCastType());


            List<ContentModel>contentModels=movieRepo.findContentModelByCastId(id);

            if(detailedCastPutPostDto.getIds()!=null){


            Set<String>newIds=new HashSet<>(Arrays.asList(detailedCastPutPostDto.getIds()));

            Set<String>currentIds=new HashSet<>(contentModels.stream().map(ContentModel::getImdbId).
                    collect(Collectors.toSet()));

            Set<String> toRemove=new HashSet<>(currentIds);
            toRemove.removeAll(newIds);

            Set<String>toAdd=new HashSet<>(newIds);
            toAdd.removeAll(currentIds);

            for(ContentModel model:contentModels){
                if(toRemove.contains(model.getImdbId()))
                    model.getCasts().remove(castModel);
            }

            List<ContentModel>contentModels1=movieRepo.findAllById(toAdd);
            for(ContentModel contentModel:contentModels1){
                contentModel.getCasts().add(castModel);
            }
        }

        CastResponseDto castResponseDto=new CastResponseDto();
        castResponseDto.setName(castModel.getName());
        castResponseDto.setId(id);
        castResponseDto.setCastType(castModel.getCastType());

        List<ContentModel>contentModels2=movieRepo.findContentModelByCastId(id);
        castResponseDto.setContents(contentModels2.stream().
                map(contentMapper::toSummarizedContentDto).toList());

        return castResponseDto;


    }

    @Override
    public void deleteCast(int id) {
        CastModel castModel=castRepo.findById(id).
                orElseThrow((()->new RuntimeException("planned to be deleted cast not found")));


        List<ContentModel>contentModels=movieRepo.findContentModelByCastId(id);


        for(ContentModel contentModel:contentModels){
            contentModel.getCasts().remove(castModel);
        }
        castRepo.deleteById(id);
    }


    @Transactional
    public CastResponseDto getAlert(int id) {

        CastModel castModel=castRepo.findById(id).orElseThrow(()->new NotFound("cast not found"));

        CastResponseDto castResponseDto=new CastResponseDto();
        castResponseDto.setContents(castModel.getContentModel().stream().
                map(contentMapper::toSummarizedContentDto).toList());

        castResponseDto.setId(id);
        castResponseDto.setName(castModel.getName());

        return castResponseDto;




    }
}
