package cdn.cdn_project.Mapper;

import cdn.cdn_project.Dto.RequestFront.PostContentDto;
import cdn.cdn_project.Dto.ResponseFront.ContentDto;
import cdn.cdn_project.Dto.ResponseFront.EpisodeDto;
import cdn.cdn_project.Dto.ResponseFront.SeasonDto;
import cdn.cdn_project.Dto.ResponseFront.SummarizedContentDto;
import cdn.cdn_project.Entities.CastModel;
import cdn.cdn_project.Entities.ContentModel;
import cdn.cdn_project.Entities.EpisodeModel;
import cdn.cdn_project.Entities.SeasonModel;
import cdn.cdn_project.Repos.CastRepo;
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


    public ContentModel toEntity(PostContentDto dto){
        ContentModel contentModel=new ContentModel();
        contentModel.setPlot(dto.getPlot());
        contentModel.setType(dto.getType());
        contentModel.setImdbId(dto.getImdbId());
        contentModel.setPoster(dto.getPoster());
        contentModel.setYear(dto.getYear());
        contentModel.setTitle(dto.getTitle());
        contentModel.setTotalSeasons(dto.getTotalSeasons());

        LinkActors(contentModel, dto.getActors());

        return contentModel;


    }

    public void LinkActors(ContentModel contentModel,String actors){

        if(actors==null)return;

        String[] casts=actors.split(", ");

        for(String rawName:casts){

            String normalizedName=rawName.trim().toLowerCase().replaceAll("\\s+", " ");

            CastModel castModel=castRepo.findByNormalizedName(normalizedName).
                    orElseGet(()->{

                CastModel castModel1=new CastModel();
                castModel1.setName(rawName);
                castModel1.setNormalizedName(normalizedName);
                return castRepo.save(castModel1);
            });

            contentModel.getCasts().add(castModel);



        }

    }
    public ContentModel toEntity(ContentDto dto){
        ContentModel contentModel=new ContentModel();
        contentModel.setPlot(dto.getPlot());
        contentModel.setType(dto.getType());
        contentModel.setImdbId(dto.getImdbId());
        contentModel.setPoster(dto.getPoster());
        contentModel.setYear(dto.getYear());
        contentModel.setTitle(dto.getTitle());
        contentModel.setTotalSeasons(dto.getTotalSeasons());

        return contentModel;

    }


    public ContentDto toDto(ContentModel model){

        ContentDto movieDto=new ContentDto();
        movieDto.setImdbId(model.getImdbId());
        movieDto.setPlot(model.getPlot());
        movieDto.setYear(model.getYear());
        movieDto.setTitle(model.getTitle());
        movieDto.setPoster(model.getPoster());
        movieDto.setType(model.getType());
        movieDto.setActors(model.getCasts().stream().map(castMapper::toCastDto).toList());
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
        summarizedContentDto.setPlot(contentModel.getPlot());
        summarizedContentDto.setTitle(contentModel.getTitle());
        summarizedContentDto.setPoster(contentModel.getPoster());
        summarizedContentDto.setImdbId(contentModel.getImdbId());
        summarizedContentDto.setYear(contentModel.getYear());
        summarizedContentDto.setTotalSeasons(contentModel.getTotalSeasons());
        summarizedContentDto.setActors(contentModel.getCasts().stream().map(castMapper::toCastDto).toList());

        return summarizedContentDto;

    }
}
