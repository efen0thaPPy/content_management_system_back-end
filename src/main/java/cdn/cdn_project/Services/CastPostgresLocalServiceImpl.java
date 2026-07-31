package cdn.cdn_project.Services;

import cdn.cdn_project.Dto.RequestFront.CastRequestDto;
import cdn.cdn_project.Dto.ResponseFront.CastResponses.SimpleCastResponseDto;
import cdn.cdn_project.Dto.ResponseFront.CastResponses.PaginatedCastResponseDto;
import cdn.cdn_project.Dto.ResponseFront.CastResponses.CastResponseDto;
import cdn.cdn_project.Entities.CastModel;
import cdn.cdn_project.Entities.ContentModel;
import cdn.cdn_project.ExceptionHandling.NotFound;
import cdn.cdn_project.Mapper.CastMapper;
import cdn.cdn_project.Mapper.MovieMapper;
import cdn.cdn_project.Repos.CastRepo;
import cdn.cdn_project.Repos.MovieRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CastPostgresLocalServiceImpl implements CastService {

    private final CastRepo castRepo;
    private final CastMapper castMapper;
    private final MovieMapper movieMapper;
    private final MovieRepo movieRepo;

    @Override
    public Page<SimpleCastResponseDto> getCasts(Pageable pageable,String query){

            Page<CastModel>castModels;

               if(query!=null&& !query.isBlank()){
                   castModels=castRepo.findCastModelByNameContainingIgnoreCase(pageable,query);
                   return castModels.map(castMapper::toCastDto);

               }
               else{
                  castModels= castRepo.findAll(pageable);
                   return castModels.map(castMapper::toCastDto);
               }




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

           detailedCastDto.setContents(contentModels.map(movieMapper::toSummarizedContentDto));


       return detailedCastDto;



    }
    @Override
    @Transactional
    public CastResponseDto postCast(CastRequestDto detailedContentPostDto) {

        String name= detailedContentPostDto.getName();

        CastModel castModel= new CastModel();

        castModel.setName(name);

        String normalizedName= name.trim().toLowerCase().replaceAll("\\s+", " ");

        castModel.setNormalizedName(normalizedName);
        castModel.setPoster(detailedContentPostDto.getPoster());

        castRepo.save(castModel);

        List<ContentModel>contentModels=movieRepo.findAllById(Arrays.asList(detailedContentPostDto.getIds()));


        for(ContentModel contentModel:contentModels){
            contentModel.getCasts().add(castModel);


        }
        CastResponseDto putPostCastResponseDto=new CastResponseDto();
        putPostCastResponseDto.setName(castModel.getName());
        putPostCastResponseDto.setId(castModel.getId());
        putPostCastResponseDto.setContents(contentModels.stream().
                map(movieMapper::toSummarizedContentDto).toList());


    return putPostCastResponseDto;


    }

    @Override
    @Transactional
    public CastResponseDto putCast(int id, CastRequestDto detailedCastPutPostDto){


        CastModel castModel= castRepo.findById(id).orElseThrow(()->new NotFound("cast not found"));
        castModel.setName(detailedCastPutPostDto.getName());
        castModel.setPoster(detailedCastPutPostDto.getPoster());
        String normalizedName= detailedCastPutPostDto.getName().trim().toLowerCase().replaceAll("\\s+", " ");
        castModel.setNormalizedName(normalizedName);
        castModel.setCastType(detailedCastPutPostDto.getCastType());

        List<ContentModel>contentModels=movieRepo.findContentModelByCastId(id);


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


        CastResponseDto castResponseDto=new CastResponseDto();

        castResponseDto.setName(castModel.getName());
        castResponseDto.setId(id);
        List<ContentModel>contentModels2=movieRepo.findContentModelByCastId(id);
        castResponseDto.setContents(contentModels2.stream().
                map(movieMapper::toSummarizedContentDto).toList());

        return castResponseDto;


    }
    @Override
    public void deleteCast(int id) {
        castRepo.deleteById(id);
    }


    @Transactional
    public CastResponseDto getAlert(int id) {

        CastModel castModel=castRepo.findById(id).orElseThrow(()->new NotFound("cast not found"));

        CastResponseDto castResponseDto=new CastResponseDto();
        castResponseDto.setContents(castModel.getContentModel().stream().
                map(movieMapper::toSummarizedContentDto).toList());

        castResponseDto.setId(id);
        castResponseDto.setName(castModel.getName());

        return castResponseDto;




    }
}
