package cdn.cdn_project.Entities;

import cdn.cdn_project.Enums.CastType;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "cast_table",uniqueConstraints =@UniqueConstraint(columnNames ="normalized_name"))

public class CastModel {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private int id;

    private String name;

    private String poster;

    @Enumerated(EnumType.STRING)
    private CastType castType;


    @Column(name = "normalized_name",nullable = false, unique = true)
    private String normalizedName;

    @ManyToMany(mappedBy = "casts")
    private List<ContentModel> contentModel;



}
