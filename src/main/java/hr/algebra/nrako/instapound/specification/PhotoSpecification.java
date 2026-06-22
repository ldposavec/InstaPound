package hr.algebra.nrako.instapound.specification;

import hr.algebra.nrako.instapound.model.dto.request.PhotoSearchRequest;
import hr.algebra.nrako.instapound.model.entity.Photo;
import hr.algebra.nrako.instapound.model.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class PhotoSpecification {
    private PhotoSpecification() {}

    public static Specification<Photo> fromSearchRequest(PhotoSearchRequest searchRequest) {
        return ((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchRequest.getHashtags() != null && !searchRequest.getHashtags().isEmpty()) {
                Join<Object, Object> hashtagJoin = root.join("hashtags");
                predicates.add(hashtagJoin.get("tag").in(searchRequest.getHashtags()));
            }

            if (searchRequest.getAuthorId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), searchRequest.getAuthorId()));
            }

            if (searchRequest.getAuthor() != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("username"), searchRequest.getAuthor()));
            }

            if (searchRequest.getUploadedAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("uploadedAt"), searchRequest.getUploadedAfter()));
            }

            if (searchRequest.getUploadedBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("uploadedAt"), searchRequest.getUploadedBefore()));
            }

            if (searchRequest.getMinSizeBytes() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("fileSizeBytes"), searchRequest.getMinSizeBytes()));
            }

            if (searchRequest.getMaxSizeBytes() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("fileSizeBytes"), searchRequest.getMaxSizeBytes()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });
    }

    public static Specification<Photo> byUser(User user) {
        return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user"), user));
    }
}
