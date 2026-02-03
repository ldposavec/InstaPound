package hr.algebra.nrako.instapound.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "HASHTAGS")
public class Hashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String tag;

    @ManyToMany(mappedBy = "hashtags")
    private Set<Photo> photos = new HashSet<>();

    private Long usageCount = 0L;

    public void incrementUsage() {
        this.usageCount++;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String tag;
        private Set<Photo> photos = new HashSet<>();
        private Long usageCount = 0L;

        public Builder withTag(String tag) {
            this.tag = tag;
            return this;
        }

        public Builder withPhotos(Set<Photo> photos) {
            this.photos = photos;
            return this;
        }

        public Builder withUsageCount(Long usageCount) {
            this.usageCount = usageCount;
            return this;
        }

        public Hashtag build() {
            Hashtag hashtag = new Hashtag();
            hashtag.setTag(this.tag);
            hashtag.setPhotos(this.photos);
            hashtag.setUsageCount(this.usageCount);
            return hashtag;
        }
    }
}
