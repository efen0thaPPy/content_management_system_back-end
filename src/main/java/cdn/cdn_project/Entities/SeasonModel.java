package cdn.cdn_project.Entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class SeasonModel {
    @Id
    @Column(name = "id",length =30 )
    private String id;


    private String seasonNumber;


    @ManyToOne
    @JoinColumn(name = "content_id")
    private ContentModel series;


    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL,orphanRemoval = true)
    private List<EpisodeModel>episodes=new ArrayList<>();


}
