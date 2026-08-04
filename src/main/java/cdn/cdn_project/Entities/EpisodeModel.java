package cdn.cdn_project.Entities;

import jakarta.persistence.*;
import lombok.Data;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Persistable;

@Entity
@Data
public class EpisodeModel implements Persistable<String> {
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

    @Transient
    private boolean isNew=true;

    @ManyToOne
    @JoinColumn(name = "Season")
    private SeasonModel season;

    @Override
    public String getId() {
        return imdbID;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }
    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }

}
