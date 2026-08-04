package cdn.cdn_project.Entities;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.domain.Persistable;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class SeasonModel implements Persistable<String> {
    @Id
    @Column(name = "id")
    private String id;


    private String seasonNumber;

    @Transient
    private boolean isNew=true;


    @ManyToOne
    @JoinColumn(name = "content_id")
    private ContentModel series;


    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL,orphanRemoval = true)
    private List<EpisodeModel>episodes=new ArrayList<>();

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
