package cdn.cdn_project.Entities;

import cdn.cdn_project.Enums.ContentType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
public class ContentModel {
    @Id
    private String imdbId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String year;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentType type;

    @Column(nullable = false)
    private String poster;

    @Column(nullable = false)
    private String plot;


    private String totalSeasons;

    @OneToMany(mappedBy = "series", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<SeasonModel>seasons=new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "content_cast",
            joinColumns = @JoinColumn(name="content_id"),
            inverseJoinColumns = @JoinColumn(name="cast_id")
    )
    private Set<CastModel> casts=new HashSet<>();



}
