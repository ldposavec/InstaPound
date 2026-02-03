package hr.algebra.nrako.instapound.service.interfaces;

import hr.algebra.nrako.instapound.model.dto.response.PhotoResponse;
import hr.algebra.nrako.instapound.model.dto.request.PhotoSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.text.ParseException;
import java.util.List;
import java.util.Optional;

public interface PhotoService {
    List<PhotoResponse> getAll();
    Optional<PhotoResponse> getById(Long id);
    PhotoResponse save(PhotoResponse photoResponse) throws ParseException;
    Optional<PhotoResponse> update(PhotoResponse photoResponse) throws ParseException;
    void deleteById(Long id);
    Page<PhotoResponse> filterByUser(String username, Pageable pageable);
    Page<PhotoResponse> filterByParams(PhotoSearchRequest searchRequest,  Pageable pageable);
}
