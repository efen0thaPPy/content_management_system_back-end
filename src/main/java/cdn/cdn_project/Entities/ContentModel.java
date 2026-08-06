package cdn.cdn_project.Entities;

import cdn.cdn_project.Enums.ContentType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Persistable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
public class ContentModel implements Persistable<String> {
    @Id
    private String imdbId;


    private String title;


    private String year;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentType type;


    private String poster;


    private String plot;

    @Transient
    private boolean isNew=true;

    private String totalSeasons;

    @OneToMany(mappedBy = "series", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SeasonModel>seasons=new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "content_cast",
            joinColumns = @JoinColumn(name="content_id"),
            inverseJoinColumns = @JoinColumn(name="cast_id")
    )
    private Set<CastModel> casts=new HashSet<>();


    @Override
    public  String getId() {
        return imdbId;
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
