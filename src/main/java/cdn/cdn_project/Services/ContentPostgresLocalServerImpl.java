package cdn.cdn_project.Services;
import cdn.cdn_project.Dto.RequestFront.PostContentDto;
import cdn.cdn_project.Dto.RequestFront.UpdateEpisodeDto;
import cdn.cdn_project.Dto.RequestFront.UpdateSeasonDto;
import cdn.cdn_project.Dto.ResponseFront.ContentResponses.ContentDto;
import cdn.cdn_project.Dto.RequestFront.UpdateContentDto;
import cdn.cdn_project.Dto.omdbDtos.OmdbEpisodeDto;
import cdn.cdn_project.Dto.omdbDtos.OmdbSeasonDto;
import cdn.cdn_project.Entities.ContentModel;
import cdn.cdn_project.Entities.EpisodeModel;
import cdn.cdn_project.Entities.SeasonModel;
import cdn.cdn_project.ExceptionHandling.NotFound;
import cdn.cdn_project.Mapper.MovieMapper;
import cdn.cdn_project.Repos.CastRepo;
import cdn.cdn_project.Repos.EpisodeRepo;
import cdn.cdn_project.Repos.MovieRepo;
import cdn.cdn_project.Repos.SeasonRepo;
import cdn.cdn_project.Enums.ContentType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.swing.text.AbstractDocument;

@Service
@RequiredArgsConstructor

public class ContentPostgresLocalServerImpl implements MovieService {

    @Value("${omdb.api.key}")
    private String key;

    private final RestTemplate restTemplate=new RestTemplate();


    private final MovieRepo movieRepo;
    private final SeasonRepo seasonRepo;
    private final EpisodeRepo episodeRepo;
    private final CastRepo castRepo;
    private final MovieMapper mapper;



    @Override
    public Page<ContentDto> getContents(String query, Pageable pageable) {

        Page<ContentModel> contentModelPage;

        if( query!=null && !query.isBlank() ){
           return movieRepo.findByTitleContainingIgnoreCase(query,pageable).map(mapper::toDto);

        }
        else {
            contentModelPage=movieRepo.findAll(pageable);
            return contentModelPage.map(mapper::toDto);
        }

    }

    @Transactional
    @Override
    public ContentDto postContent(PostContentDto postContentDto){

       if(movieRepo.existsById(postContentDto.getImdbId())){
           throw new RuntimeException("movie already exists with the same primary key");

        }

       ContentModel contentModel=mapper.toEntity(postContentDto);

       movieRepo.save(contentModel);

       if(contentModel.getType()==ContentType.movie){
           return mapper.toDto(contentModel);
       }
       else{
           int totalSeasons=Integer.parseInt(contentModel.getTotalSeasons());
           for(int i=1;i<=totalSeasons;i++){
               String url="https://www.omdbapi.com/?apikey="+
                       key+"&i="+contentModel.getImdbId()+ "&Season="+i;



               OmdbSeasonDto res=restTemplate.getForObject(url,OmdbSeasonDto.class);

               String seasonId= contentModel.getImdbId()+"-S"+i;

               if(seasonRepo.existsById(seasonId))
                   throw new RuntimeException("season already exists");

               SeasonModel seasonModel=new SeasonModel();
               seasonModel.setId(seasonId);
               seasonModel.setSeries(contentModel);
               seasonModel.setSeasonNumber(Integer.toString(i));
               seasonRepo.save(seasonModel);

               for(OmdbEpisodeDto episodeDto:res.getEpisodes()){
                   if(episodeRepo.existsById(episodeDto.getImdbID()))
                       throw new RuntimeException("episode already exists");


                   EpisodeModel episodeModel=new EpisodeModel();
                   episodeModel.setEpisode(episodeDto.getEpisode());
                   episodeModel.setTitle(episodeDto.getTitle());
                   episodeModel.setReleased(episodeDto.getReleased());
                   episodeModel.setImdbRating(episodeDto.getImdbRating());
                   episodeModel.setImdbID(episodeDto.getImdbID());
                   episodeModel.setSeason(seasonModel);

                   episodeRepo.save(episodeModel);



               }


           }
       }
       return mapper.toDto(contentModel);


    }

    @Override
    @Transactional
    public ContentDto putContent(UpdateContentDto updateContentDto,String id){


        ContentModel contentModel=mapper.toEntity(updateContentDto,id);
        return mapper.toDto(contentModel);
    }


    @Override
    @Transactional
    public ContentDto getContentById(String id) {
        ContentModel model=movieRepo.findById(id).
                orElseThrow(()-> new NotFound("not found"));
        return mapper.toDto(model);
    }


    @Override
    @Transactional
    public void deleteContent(String id) {
        movieRepo.deleteById(id);
    }
}
