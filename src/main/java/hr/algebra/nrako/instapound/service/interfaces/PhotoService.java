package hr.algebra.nrako.instapound.service.interfaces;

import hr.algebra.nrako.instapound.model.dto.PhotoResponse;
import hr.algebra.nrako.instapound.model.dto.PhotoSearchRequest;
import hr.algebra.nrako.instapound.model.entity.Photo;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

public interface PhotoService {
    List<PhotoResponse> getAll();
    Optional<PhotoResponse> getById(Long id);
    PhotoResponse save(PhotoResponse photoResponse) throws ParseException;
    Optional<PhotoResponse> update(PhotoResponse photoResponse) throws ParseException;
    void deleteById(Long id);
    List<PhotoResponse> filterByUser(String username);
    List<PhotoResponse> filterByParams(PhotoSearchRequest searchRequest);
}
