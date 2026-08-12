package cdn.cdn_project.Mapper;

import cdn.cdn_project.Dto.RequestFront.ContentRequests.PutPostContentDto;
import cdn.cdn_project.Dto.RequestFront.ContentRequests.PutPostEpisodeDto;

import cdn.cdn_project.Dto.RequestFront.ContentRequests.PutPostSeasonDto;
import cdn.cdn_project.Dto.ResponseFront.ContentResponses.ContentDto;
import cdn.cdn_project.Dto.ResponseFront.ContentResponses.EpisodeDto;
import cdn.cdn_project.Dto.ResponseFront.ContentResponses.SeasonDto;
import cdn.cdn_project.Dto.ResponseFront.ContentResponses.SummarizedContentDto;
import cdn.cdn_project.Entities.CastModel;
import cdn.cdn_project.Entities.ContentModel;
import cdn.cdn_project.Entities.EpisodeModel;
import cdn.cdn_project.Entities.SeasonModel;
import cdn.cdn_project.Enums.CastType;
import cdn.cdn_project.Repos.CastRepo;
import cdn.cdn_project.Repos.EpisodeRepo;
import cdn.cdn_project.Repos.MovieRepo;
import cdn.cdn_project.Enums.ContentType;
import cdn.cdn_project.Repos.SeasonRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ContentMapper {

    private final MovieRepo movieRepo;
    private final CastRepo castRepo;
    private final CastMapper castMapper;
    private final EpisodeRepo episodeRepo;
    private final SeasonRepo seasonRepo;


    public ContentModel toEntity(PutPostContentDto dto, boolean isLocal){
        ContentModel contentModel=new ContentModel();
        contentModel.setPlot(dto.getPlot());
        contentModel.setType(dto.getContentType());
        contentModel.setImdbId(dto.getImdbID());
        contentModel.setPoster(dto.getPoster());
        contentModel.setYear(dto.getYear());
        contentModel.setTitle(dto.getTitle());


        if(isLocal && dto.getContentType()!=ContentType.movie && dto.getSeasons()!=null )
            contentModel.setSeasons(dto.getSeasons().stream().
                map((e)->this.toSeasonModel(e, contentModel.getImdbId(), contentModel)).toList());

        if(dto.getContentType()!=ContentType.movie){
            contentModel.setTotalSeasons(dto.getTotalSeasons());
        }
        LinkActor(contentModel, dto.getActors());
        LinkDirector(contentModel,dto.getDirector());


        return contentModel;

    }


    public ContentModel toEntity(PutPostContentDto putPostContentDto, String id) {

        ContentModel contentModel = movieRepo.findById(id).
                orElseThrow(() -> new RuntimeException("content doesnt exist"));

        if (putPostContentDto.getPlot() != null) contentModel.setPlot(putPostContentDto.getPlot());
        if (putPostContentDto.getYear() != null) contentModel.setYear(putPostContentDto.getYear());
        if (putPostContentDto.getTitle() != null) contentModel.setTitle(putPostContentDto.getTitle());
        if (putPostContentDto.getTotalSeasons() != null)
            contentModel.setTotalSeasons(putPostContentDto.getTotalSeasons());

        LinkActor(contentModel, putPostContentDto.getActors());
        LinkDirector(contentModel, putPostContentDto.getDirector());

        if (putPostContentDto.getSeasons() != null) {
            for (PutPostSeasonDto putPostSeasonDto : putPostContentDto.getSeasons()) {

                if (putPostSeasonDto.getSeason() == null)
                    throw new RuntimeException("season number doesnt exist");

                String seasonId = id + "-S" + putPostSeasonDto.getSeason();
                SeasonModel seasonModel = seasonRepo.findById(seasonId).
                        orElseGet(() ->
                        {
                            SeasonModel seasonModel1 = new SeasonModel();
                            seasonModel1.setId(seasonId);
                            seasonModel1.setSeasonNumber(putPostSeasonDto.getSeason());
                            seasonModel1.setSeries(contentModel);
                            return seasonModel1;
                        });
                seasonRepo.save(seasonModel);
                if(putPostContentDto.getSeasons()!=null)
                for (PutPostEpisodeDto pustPostEpisodeDto : putPostSeasonDto.getEpisodes()) {
                    EpisodeModel episodeModel = episodeRepo.findById(pustPostEpisodeDto.getImdbID()).
                            orElseGet(() ->
                            {
                                EpisodeModel episodeModel1=new EpisodeModel();
                                episodeModel1.setImdbID(UUID.randomUUID().toString());
                                return episodeModel1;
                            });




                    if (pustPostEpisodeDto.getPlot() != null) episodeModel.setPlot(pustPostEpisodeDto.getPlot());
                    if (pustPostEpisodeDto.getPoster() != null) episodeModel.setPoster(pustPostEpisodeDto.getPoster());
                    if (pustPostEpisodeDto.getTitle() != null) episodeModel.setTitle(pustPostEpisodeDto.getTitle());
                    if (pustPostEpisodeDto.getEpisode() != null) episodeModel.setEpisode(pustPostEpisodeDto.getEpisode());
                    if (pustPostEpisodeDto.getImdbRating() != null) episodeModel.setImdbRating(pustPostEpisodeDto.getImdbRating());
                    episodeModel.setSeason(seasonModel);
                    episodeRepo.save(episodeModel);

                }
            }

        }
        return contentModel;

    }

    public void LinkActor(ContentModel contentModel, String actor) {

        if (actor == null) return;

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
        movieDto.setCasts(model.getCasts().stream().map(castMapper::toSimpleCastDto).toList());
        if(model.getType()== ContentType.series){
            movieDto.setTotalSeasons(model.getTotalSeasons());
          movieDto.setSeasons(model.getSeasons().stream().map(this::toSeasonDto).toList());

        }
        return movieDto;

    }
    public SeasonDto toSeasonDto(SeasonModel seasonModel){
        SeasonDto seasonDto=new SeasonDto();
        seasonDto.setSeason(seasonModel.getSeasonNumber());
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
        summarizedContentDto.setType(contentModel.getType());



        return summarizedContentDto;

    }

    public SeasonModel toSeasonModel(PutPostSeasonDto seasonDto,String id,ContentModel contentModel){
        SeasonModel seasonModel=new SeasonModel();
        seasonModel.setSeasonNumber(seasonDto.getSeason());
        String seasonId=id+"-S"+seasonModel.getSeasonNumber();
        seasonModel.setId(seasonId);

       seasonModel.setEpisodes(seasonDto.getEpisodes().stream().
               map((e)->this.toEpisodeModel(e,seasonModel)).collect(Collectors.toList()));
       seasonModel.setSeries(contentModel);
       return seasonModel;

    }
    public EpisodeModel toEpisodeModel(PutPostEpisodeDto episodeDto, SeasonModel seasonModel){

        EpisodeModel episodeModel=new EpisodeModel();
        if(episodeDto.getImdbID()==null){
            String episodeId= UUID.randomUUID().toString();
            episodeModel.setImdbID(episodeId);
        }
        else  episodeModel.setImdbID(episodeDto.getImdbID());

        episodeModel.setPlot(episodeDto.getPlot());
        episodeModel.setEpisode(episodeDto.getEpisode());
        episodeModel.setPoster(episodeDto.getPoster());
        episodeModel.setTitle(episodeDto.getTitle());
        episodeModel.setReleased(episodeDto.getReleased());
        episodeModel.setSeason(seasonModel);
        episodeModel.setImdbRating(episodeDto.getImdbRating());


        return episodeModel;
    }
}
