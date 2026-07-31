package cdn.cdn_project.Mapper;

import cdn.cdn_project.Dto.RequestFront.PostContentDto;
import cdn.cdn_project.Dto.RequestFront.UpdateEpisodeDto;
import cdn.cdn_project.Dto.ResponseFront.ContentResponses.ContentDto;
import cdn.cdn_project.Dto.ResponseFront.ContentResponses.EpisodeDto;
import cdn.cdn_project.Dto.ResponseFront.ContentResponses.SeasonDto;
import cdn.cdn_project.Dto.ResponseFront.CastResponses.SummarizedContentDto;
import cdn.cdn_project.Entities.CastModel;
import cdn.cdn_project.Entities.ContentModel;
import cdn.cdn_project.Entities.EpisodeModel;
import cdn.cdn_project.Entities.SeasonModel;
import cdn.cdn_project.Enums.CastType;
import cdn.cdn_project.ExceptionHandling.NotFound;
import cdn.cdn_project.Repos.CastRepo;
import cdn.cdn_project.Repos.EpisodeRepo;
import cdn.cdn_project.Repos.MovieRepo;
import cdn.cdn_project.Enums.ContentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MovieMapper {

    private final MovieRepo movieRepo;
    private final CastRepo castRepo;
    private final CastMapper castMapper;
    private final EpisodeRepo episodeRepo;


    public ContentModel toEntity(PostContentDto dto){
        ContentModel contentModel=new ContentModel();
        contentModel.setPlot(dto.getPlot());
        contentModel.setType(dto.getType());
        contentModel.setImdbId(dto.getImdbId());
        contentModel.setPoster(dto.getPoster());
        contentModel.setYear(dto.getYear());
        contentModel.setTitle(dto.getTitle());
        contentModel.setTotalSeasons(dto.getTotalSeasons());


        LinkActor(contentModel, dto.getActors());
        LinkDirector(contentModel,dto.getDirector());

        return contentModel;

    }

    public EpisodeModel toEntity(UpdateEpisodeDto episodeDto, SeasonModel seasonModel){
        EpisodeModel episodeModel=episodeRepo.findById(episodeDto.getImdbID()).
                orElseThrow(()->new NotFound("couldn't find the episode"));

        episodeModel.setTitle(episodeDto.getTitle());
        episodeModel.setEpisode(episodeDto.getEpisode());
        episodeModel.setReleased(episodeDto.getReleased());
        episodeModel.setImdbRating(episodeDto.getImdbRating());
        episodeModel.setPoster(episodeDto.getPoster());
        episodeModel.setPlot(episodeDto.getPlot());
        episodeModel.setSeason(seasonModel);

        seasonModel.getEpisodes().add(episodeModel);

        return  episodeModel;
    }

    public void LinkActor(ContentModel contentModel, String actor) {

        if (actor == null) ;

        String[] actors = actor.split(", ");

        for (String rawName : actors) {

            String normalizedName = rawName.trim().toLowerCase().replaceAll("\\s+", " ");

            CastModel castModel = castRepo.findByNormalizedName(normalizedName).
                    orElseGet(() -> {

                        CastModel castModel1 = new CastModel();
                        castModel1.setName(rawName);
                        castModel1.setNormalizedName(normalizedName);
                        castModel1.setCastType(CastType.actor);
                        return castRepo.save(castModel1);
                    });
            contentModel.getCasts().add(castModel);

        }




    }
    public void LinkDirector(ContentModel contentModel,String director){

        if(director==null)return;
        String[] directors = director.split(", ");

        for (String rawName : directors) {

            String normalizedName = rawName.trim().toLowerCase().replaceAll("\\s+", " ");

            CastModel castModel = castRepo.findByNormalizedName(normalizedName).
                    orElseGet(() -> {

                        CastModel castModel1 = new CastModel();
                        castModel1.setName(rawName);
                        castModel1.setNormalizedName(normalizedName);
                        castModel1.setCastType(CastType.director);
                        return castRepo.save(castModel1);
                    });
            contentModel.getCasts().add(castModel);


        }
    }

    public ContentDto toDto(ContentModel model){

        ContentDto movieDto=new ContentDto();
        movieDto.setImdbId(model.getImdbId());
        movieDto.setPlot(model.getPlot());
        movieDto.setYear(model.getYear());
        movieDto.setTitle(model.getTitle());
        movieDto.setPoster(model.getPoster());
        movieDto.setType(model.getType());
        movieDto.setCasts(model.getCasts().stream().map(castMapper::toCastDto).toList());
        if(model.getType()== ContentType.series){
            movieDto.setTotalSeasons(model.getTotalSeasons());
          movieDto.setSeasons(model.getSeasons().stream().map(this::toSeasonDto).toList());

        }
        return movieDto;

    }
    public SeasonDto toSeasonDto(SeasonModel seasonModel){
        SeasonDto seasonDto=new SeasonDto();
        seasonDto.setSeasonNumber(seasonModel.getSeasonNumber());
        seasonDto.setEpisodes(seasonModel.getEpisodes().stream().map(this::toEpisodeDto).toList());
        return seasonDto;


    }

    public EpisodeDto toEpisodeDto(EpisodeModel episodeModel){


       EpisodeDto episodeDto=new EpisodeDto();
       episodeDto.setEpisode(episodeModel.getEpisode());
       episodeDto.setReleased(episodeModel.getReleased());
       episodeDto.setTitle(episodeModel.getTitle());
       episodeDto.setImdbID(episodeModel.getImdbID());
       episodeDto.setImdbRating(episodeModel.getImdbRating());
        episodeDto.setPoster(episodeModel.getPoster());
        episodeDto.setPlot(episodeModel.getPlot());

       return episodeDto;


    }
    public SummarizedContentDto toSummarizedContentDto(ContentModel contentModel){

        SummarizedContentDto summarizedContentDto=new SummarizedContentDto();

        summarizedContentDto.setTitle(contentModel.getTitle());
        summarizedContentDto.setPoster(contentModel.getPoster());
        summarizedContentDto.setImdbId(contentModel.getImdbId());
        summarizedContentDto.setYear(contentModel.getYear());



        return summarizedContentDto;

    }
}
