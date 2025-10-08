package hr.algebra.nrako.instapound.service;

import hr.algebra.nrako.instapound.domain.entity.Photo;
import hr.algebra.nrako.instapound.domain.entity.User;
import hr.algebra.nrako.instapound.dto.PhotoSearchRequest;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Specification Pattern (Design Pattern #7)
 * Provides reusable query specifications for Photo entity
 * Adheres to Open/Closed Principle (SOLID) - can add new specifications without modifying existing code
 */
public class PhotoSpecifications {
    
    /**
     * Create a specification from search criteria
     * 
     * @param searchRequest Search criteria
     * @return Combined specification
     */
    public static Specification<Photo> fromSearchRequest(PhotoSearchRequest searchRequest) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Filter by hashtags
            if (searchRequest.getHashtags() != null && !searchRequest.getHashtags().isEmpty()) {
                Join<Object, Object> hashtagJoin = root.join("hashtags");
                predicates.add(hashtagJoin.get("tag").in(searchRequest.getHashtags()));
            }
            
            // Filter by author ID
            if (searchRequest.getAuthorId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), searchRequest.getAuthorId()));
            }
            
            // Filter by author username
            if (searchRequest.getAuthor() != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("username"), searchRequest.getAuthor()));
            }
            
            // Filter by upload date range
            if (searchRequest.getUploadedAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("uploadedAt"), searchRequest.getUploadedAfter()));
            }
            
            if (searchRequest.getUploadedBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("uploadedAt"), searchRequest.getUploadedBefore()));
            }
            
            // Filter by file size range
            if (searchRequest.getMinSizeBytes() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("fileSizeBytes"), searchRequest.getMinSizeBytes()));
            }
            
            if (searchRequest.getMaxSizeBytes() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("fileSizeBytes"), searchRequest.getMaxSizeBytes()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    /**
     * Specification to find photos by user
     */
    public static Specification<Photo> byUser(User user) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("user"), user);
    }
    
    /**
     * Specification to find photos uploaded after a certain date
     */
    public static Specification<Photo> uploadedAfter(java.time.LocalDateTime date) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.greaterThanOrEqualTo(root.get("uploadedAt"), date);
    }
}
