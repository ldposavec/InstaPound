package hr.algebra.nrako.instapound.domain.entity;

import hr.algebra.nrako.instapound.domain.enums.StorageType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Photo entity
 * Represents uploaded photos
 * Adheres to Single Responsibility Principle (SOLID)
 */
@Entity
@Table(name = "photos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Photo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;
    
    @Column(nullable = false)
    private String originalFileName;
    
    @Column(nullable = false)
    private String storedFileName;  // Unique filename in storage
    
    @Column(length = 2000)
    private String description;
    
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "photo_hashtags",
        joinColumns = @JoinColumn(name = "photo_id"),
        inverseJoinColumns = @JoinColumn(name = "hashtag_id")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Hashtag> hashtags = new HashSet<>();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StorageType storageType;
    
    private String storageUrl;  // URL for cloud storage or path for local
    
    private String thumbnailUrl;
    
    private String processedUrl;  // URL for processed version with filters
    
    private Long fileSizeBytes;
    
    private Integer width;
    
    private Integer height;
    
    private String mimeType;
    
    @Column(nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();
    
    private LocalDateTime editedAt;
    
    private Long downloadCount = 0L;
    
    private Long viewCount = 0L;
    
    /**
     * Add a hashtag to the photo
     */
    public void addHashtag(Hashtag hashtag) {
        hashtags.add(hashtag);
        hashtag.getPhotos().add(this);
    }
    
    /**
     * Remove a hashtag from the photo
     */
    public void removeHashtag(Hashtag hashtag) {
        hashtags.remove(hashtag);
        hashtag.getPhotos().remove(this);
    }
    
    /**
     * Increment download counter
     */
    public void incrementDownloadCount() {
        this.downloadCount++;
    }
    
    /**
     * Increment view counter
     */
    public void incrementViewCount() {
        this.viewCount++;
    }
}
