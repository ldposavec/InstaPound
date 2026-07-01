package hr.algebra.nrako.instapound.model.entity;

import jakarta.persistence.*;
import lombok.*;
import hr.algebra.nrako.instapound.enums.StorageType;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "PHOTOS")
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String storedFileName;

    @Column(length = 2000)
    private String description;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "PHOTO_HASHTAGS",
        joinColumns = @JoinColumn(name = "photo_id"),
        inverseJoinColumns = @JoinColumn(name = "hashtag_id")
    )
    @Builder.Default
    private Set<Hashtag> hashtags = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StorageType storageType;

    private String storageUrl;
    private String thumbnailUrl;
    private String processedUrl;
    private Long fileSizeBytes;
    private Integer width;
    private Integer height;
    private String mimeType;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime uploadedAt = LocalDateTime.now();

    private LocalDateTime editedAt;
    @Builder.Default
    private Long downloadCount = 0L;
    @Builder.Default
    private Long viewCount = 0L;

    public void addHashtag(Hashtag hashtag) {
        hashtags.add(hashtag);
        hashtag.getPhotos().add(this);
    }

    public void removeHashtag(Hashtag hashtag) {
        hashtags.remove(hashtag);
        hashtag.getPhotos().remove(this);
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void incrementDownloadCount() {
        this.downloadCount++;
    }
}
