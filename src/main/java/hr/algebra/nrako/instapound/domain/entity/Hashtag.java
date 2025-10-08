package hr.algebra.nrako.instapound.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Hashtag entity
 * Represents photo hashtags for categorization and search
 * Adheres to Single Responsibility Principle (SOLID)
 */
@Entity
@Table(name = "hashtags")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hashtag {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String tag;  // Without # symbol
    
    @ManyToMany(mappedBy = "hashtags")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Photo> photos = new HashSet<>();
    
    private Long usageCount = 0L;
    
    /**
     * Increment usage counter
     */
    public void incrementUsageCount() {
        this.usageCount++;
    }
}
