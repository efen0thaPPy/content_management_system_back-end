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
    public ContentDto postContent(PostContentDto movie) {


        ContentModel contentModel = movieRepo.findById(movie.getImdbId()).orElseGet(()->mapper.toEntity(movie));

        movieRepo.save(contentModel);


            if(contentModel.getType()== ContentType.movie){


              return mapper.toDto(contentModel);

            }

            else{


            int total = Integer.parseInt(movie.getTotalSeasons());

            for (int i = 1; i <= total; i++) {

                String url = "https://www.omdbapi.com/?apikey=" + key + "&i=" + movie.getImdbId() + "&Season=" + i;

                OmdbSeasonDto omdbSeasonDto = restTemplate.getForObject(url, OmdbSeasonDto.class);

                String id=contentModel.getImdbId()+"-S"+omdbSeasonDto.getSeasonNumber();

               SeasonModel seasonModel=seasonRepo.findById(id).orElseGet(SeasonModel::new);
               seasonModel.setId(id);
                seasonModel.setSeasonNumber(omdbSeasonDto.getSeasonNumber());
                seasonModel.setSeries(contentModel);

                seasonRepo.save(seasonModel);

                for (OmdbEpisodeDto dto : omdbSeasonDto.getEpisodes()) {

                    EpisodeModel episodeModel = episodeRepo.findById(dto.getImdbID()).orElseGet(EpisodeModel::new);
                    episodeModel.setEpisode(dto.getEpisode());
                    episodeModel.setTitle(dto.getTitle());
                    episodeModel.setImdbID(dto.getImdbID());
                    episodeModel.setReleased(dto.getReleased());
                    episodeModel.setSeason(seasonModel);
                    episodeModel.setImdbRating(dto.getImdbRating());
                    episodeRepo.save(episodeModel);

                    seasonModel.getEpisodes().add(episodeModel);

                }
                contentModel.getSeasons().add(seasonModel);
            }

            return mapper.toDto(contentModel);
        }

    }

    @Override
    @Transactional
    public ContentDto getContentById(String id) {
        ContentModel model=movieRepo.findById(id).
                orElseThrow(()-> new NotFound("not found"));
        return mapper.toDto(model);
    }

    @Transactional
    @Override
    public ContentDto putContent(UpdateContentDto dto, String id) {

        ContentModel contentModel=movieRepo.findById(id).orElseThrow(()->new RuntimeException("movie not found"));


        contentModel.setPlot(dto.getPlot());
        contentModel.setYear(dto.getYear());
        contentModel.setTitle(dto.getTitle());
        mapper.LinkActor(contentModel,dto.getActors());
        mapper.LinkDirector(contentModel, dto.getDirector());



        if(contentModel.getType()== ContentType.movie){

            return mapper.toDto(contentModel);


        }
        else{
            for(UpdateSeasonDto seasonDto:dto.getSeasonDtoList()){

                String seasonId=contentModel.getImdbId()+"-S"+seasonDto.getSeasonNumber();
                for(UpdateEpisodeDto episodeDto:seasonDto.getEpisodes()){


                    SeasonModel seasonModel=seasonRepo.findById(seasonId).
                            orElseThrow(()->new NotFound("couldn't find the season"));


                    seasonModel.setSeasonNumber(seasonDto.getSeasonNumber());
                    seasonModel.setSeries(contentModel);


                    mapper.toEntity(episodeDto,seasonModel);

                }

            }

            return mapper.toDto(contentModel);

        }

    }
    @Override
    public void deleteContent(String id) {
        movieRepo.deleteById(id);
    }
}
