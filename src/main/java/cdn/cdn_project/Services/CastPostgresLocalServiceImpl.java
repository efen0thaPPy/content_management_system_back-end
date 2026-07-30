package cdn.cdn_project.Services;

import cdn.cdn_project.Dto.fromFront.DetailedCastPutPostDto;
import cdn.cdn_project.Dto.toFront.CastDto;
import cdn.cdn_project.Dto.toFront.DetailedCastDto;
import cdn.cdn_project.Entities.CastModel;
import cdn.cdn_project.Entities.ContentModel;
import cdn.cdn_project.Mapper.CastMapper;
import cdn.cdn_project.Mapper.MovieMapper;
import cdn.cdn_project.Repos.CastRepo;
import cdn.cdn_project.Repos.MovieRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
    public List<CastDto> getCasts(){

        return castRepo.findAll().stream().map(castMapper::toCastDto).toList();


    }
    @Override
    public DetailedCastDto getCast (int id){


        List<ContentModel>contentModels=movieRepo.findContentModelByCastId(id);

       CastModel castModel=castRepo.findById(id).
               orElseThrow(()->new RuntimeException("couldn't find the actor"));

       DetailedCastDto detailedCastDto=new DetailedCastDto();
       detailedCastDto.setId(id);
       detailedCastDto.setName(castModel.getName());
        detailedCastDto.setPoster(castModel.getPoster());
       for(ContentModel contentModel:contentModels){


           detailedCastDto.getContents().add( movieMapper.toSummarizedContentDto(contentModel));


       }

       return detailedCastDto;



    }
    @Override
    @Transactional
    public DetailedCastDto postCast(DetailedCastPutPostDto detailedContentPostDto) {

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
        DetailedCastDto detailedCastDto=new DetailedCastDto();
        detailedCastDto.setName(castModel.getName());
        detailedCastDto.setId(castModel.getId());
        detailedCastDto.setContents(contentModels.stream().
                map(movieMapper::toSummarizedContentDto).toList());


    return detailedCastDto;


    }

    @Override
    @Transactional
    public DetailedCastDto putCast(int id,DetailedCastPutPostDto detailedCastPutPostDto){


        CastModel castModel= castRepo.findById(id).orElseThrow(()->new RuntimeException("cast not found"));
        castModel.setName(detailedCastPutPostDto.getName());
        castModel.setPoster(detailedCastPutPostDto.getPoster());
        String normalizedName= detailedCastPutPostDto.getName().trim().toLowerCase().replaceAll("\\s+", " ");
        castModel.setNormalizedName(normalizedName);

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


        DetailedCastDto detailedCastDto=new DetailedCastDto();

        detailedCastDto.setName(castModel.getName());
        detailedCastDto.setId(id);
        List<ContentModel>contentModels2=movieRepo.findContentModelByCastId(id);
       detailedCastDto.setContents(contentModels2.stream().
                map(movieMapper::toSummarizedContentDto).toList());

        return detailedCastDto;


    }
    @Override
    public void deleteCast(int id) {
        castRepo.deleteById(id);
    }


    @Transactional
    public DetailedCastDto getAlert(int id) {

        CastModel castModel=castRepo.findById(id).orElseThrow(()->new RuntimeException("cast not found"));

        DetailedCastDto detailedCastDto=new DetailedCastDto();
        detailedCastDto.setContents(castModel.getContentModel().stream().
                map(movieMapper::toSummarizedContentDto).toList());

        detailedCastDto.setId(id);
        detailedCastDto.setName(castModel.getName());

        return detailedCastDto;




    }
}
