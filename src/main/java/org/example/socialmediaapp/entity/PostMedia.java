package org.example.socialmediaapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.example.socialmediaapp.enums.FileMediaType;

@Entity
@Table(name = "post_medias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostMedia extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", length = 20, nullable = false)
    private FileMediaType fileType;

    @Column(name = "object_key", columnDefinition = "TEXT", nullable = false)
    private String objectKey;

    @Builder.Default
    @Column(name = "media_index")
    private Integer mediaIndex = 0;
}
