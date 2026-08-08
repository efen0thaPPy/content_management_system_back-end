package cdn.cdn_project.Services;
import cdn.cdn_project.Dto.RequestFront.ContentRequests.BatchPostDto;
import cdn.cdn_project.Dto.RequestFront.ContentRequests.PutPostContentDto;
import cdn.cdn_project.Dto.ResponseFront.ContentResponses.ContentDto;
import cdn.cdn_project.Dto.omdbDtos.OmdbEpisodeDto;
import cdn.cdn_project.Dto.omdbDtos.OmdbSeasonDto;
import cdn.cdn_project.Entities.ContentModel;
import cdn.cdn_project.Entities.EpisodeModel;
import cdn.cdn_project.Entities.SeasonModel;
import cdn.cdn_project.Enums.ContentType;
import cdn.cdn_project.ExceptionHandling.NotFound;
import cdn.cdn_project.Mapper.ContentMapper;
import cdn.cdn_project.Repos.CastRepo;
import cdn.cdn_project.Repos.EpisodeRepo;
import cdn.cdn_project.Repos.MovieRepo;
import cdn.cdn_project.Repos.SeasonRepo;
import cdn.cdn_project.Specifications.ContentSpecifications;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor

public class ContentPostgresLocalServerImpl implements ContentService {

    @Value("${omdb.api.key}")
    private String key;

    private final RestTemplate restTemplate=new RestTemplate();


    private final MovieRepo movieRepo;
    private final SeasonRepo seasonRepo;
    private final EpisodeRepo episodeRepo;
    private final CastRepo castRepo;
    private final ContentMapper mapper;



    @Override
    public Page<ContentDto> getContents(String query, String contentType,Pageable pageable) {

        Specification<ContentModel>textSearch=Specification.where(
                ContentSpecifications.searchByTitle(query)).
                        or(ContentSpecifications.searchByPlot(query)).
                        or(ContentSpecifications.searchByYear(query)).
                        or(ContentSpecifications.searchByActorName(query)).
                        or(ContentSpecifications.searchById(query));



        Specification<ContentModel>spec=textSearch.and(ContentSpecifications.searchByContentType(contentType));

        System.out.println("search_contents called with query=" + query + " contentType=" + contentType);





        Page<ContentDto> contentDto=movieRepo.findAll(spec,pageable).map(mapper::toDto);

        System.out.println(contentDto.getContent());

        return contentDto;



    }

    @Transactional
    @Override
    public ContentDto postContent( PutPostContentDto putPostContentDto){

        String contentId=putPostContentDto.getImdbID();

         boolean isLocal=contentId==null || contentId.trim().isEmpty();


         if(isLocal) {
             contentId="local-"+UUID.randomUUID();
             putPostContentDto.setImdbID(contentId);
         }
         else{
             if(movieRepo.existsById(contentId))
                 throw new RuntimeException("content exists with the same id");
         }


       ContentModel contentModel=mapper.toEntity(putPostContentDto,isLocal);
        movieRepo.save(contentModel);

         if(!isLocal && contentModel.getType()== ContentType.series)
             saveTheSeasonsFromOmdb(contentModel);


         return mapper.toDto(contentModel);

    }

    public void saveTheSeasonsFromOmdb(ContentModel contentModel){

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
    @Override
    @Transactional
    public List<ContentDto>postContents(BatchPostDto batchPostDto){
       return batchPostDto.getPostContentDtoList().stream().map(this::postContent).toList();
    }

    @Override
    @Transactional
    public ContentDto putContent(PutPostContentDto putPostContentDto, String id){


        ContentModel contentModel=mapper.toEntity(putPostContentDto,id);
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


    @Override
    @Transactional
    public void deleteContents(List<String>ids){
        for(String id:ids){
            deleteContent(id);
        }
    }

}
