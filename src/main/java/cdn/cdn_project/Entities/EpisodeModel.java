package cdn.cdn_project.Entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class EpisodeModel {
    @Id
    private String imdbID;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String episode;

    @Column(nullable = false)
    private String imdbRating;

    @Column(nullable = false)
    private String released;

    private String poster;

    private String plot;

    

    @ManyToOne
    @JoinColumn(name = "Season")
    private SeasonModel season;

}
