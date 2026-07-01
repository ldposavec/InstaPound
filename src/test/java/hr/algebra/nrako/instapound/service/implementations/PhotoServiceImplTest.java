package hr.algebra.nrako.instapound.service.implementations;

import hr.algebra.nrako.instapound.model.dto.response.PhotoResponse;
import hr.algebra.nrako.instapound.model.entity.Hashtag;
import hr.algebra.nrako.instapound.model.entity.Photo;
import hr.algebra.nrako.instapound.model.entity.User;
import hr.algebra.nrako.instapound.model.mappers.PhotoMapper;
import hr.algebra.nrako.instapound.repository.HashtagRepository;
import hr.algebra.nrako.instapound.repository.PhotoRepository;
import hr.algebra.nrako.instapound.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PhotoServiceImplTest {

    @Mock
    private PhotoRepository photoRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HashtagRepository hashtagRepository;

    @Mock
    private PhotoMapper photoMapper;

    @InjectMocks
    private PhotoServiceImpl photoService;

    private Photo photo;
    private PhotoResponse photoResponse;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .build();
        photo = Photo.builder()
                .id(1L)
                .originalFileName("original.jpg")
                .description("Test photo")
                .hashtags(new  HashSet<>())
                .user(user)
                .fileSizeBytes(1024L)
                .width(800)
                .height(600)
                .uploadedAt(java.time.LocalDateTime.now())
                .downloadCount(0L)
                .viewCount(0L)
                .build();
        photoResponse = PhotoResponse.builder()
                .id(1L)
                .originalFileName("original.jpg")
                .description("Test photo")
                .hashtags(Set.of("test", "photo"))
                .authorId(1L)
                .author("testuser")
                .fileSizeBytes(1024L)
                .width(800)
                .height(600)
                .uploadedAt(java.time.LocalDateTime.now())
                .downloadCount(0L)
                .viewCount(0L)
                .build();
    }

    @Test
    void getAll_shouldReturnAllPhotosAsDtos() {
        when(photoRepository.findAll()).thenReturn(List.of(photo));
        when(photoMapper.toDto(photo)).thenReturn(photoResponse);

        List<PhotoResponse> result = this.photoService.getAll();

        assertEquals(1, result.size());
        assertEquals("original.jpg", result.getFirst().getOriginalFileName());
    }

    @Test
    void getAll_shouldReturnEmptyListWhenNoPhotos() {
        when(photoRepository.findAll()).thenReturn(List.of());

        List<PhotoResponse> result = this.photoService.getAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void getAll_shouldReturnMultiplePhotos() {
        Photo photo2 = Photo.builder()
                .id(2L)
                .originalFileName("second.jpg")
                .description("Second photo")
                .hashtags(new HashSet<>())
                .user(user)
                .fileSizeBytes(2048L)
                .width(1024)
                .height(768)
                .uploadedAt(java.time.LocalDateTime.now())
                .downloadCount(0L)
                .viewCount(0L)
                .build();
        PhotoResponse photoResponse2 = PhotoResponse.builder()
                .id(2L)
                .originalFileName("second.jpg")
                .description("Second photo")
                .hashtags(Set.of("second", "photo"))
                .authorId(1L)
                .author("testuser")
                .fileSizeBytes(2048L)
                .width(1024)
                .height(768)
                .uploadedAt(java.time.LocalDateTime.now())
                .downloadCount(0L)
                .viewCount(0L)
                .build();

        when(photoRepository.findAll()).thenReturn(List.of(photo, photo2));
        when(photoMapper.toDto(photo)).thenReturn(photoResponse);
        when(photoMapper.toDto(photo2)).thenReturn(photoResponse2);

        List<PhotoResponse> result = this.photoService.getAll();

        assertEquals(2, result.size());
    }

    @Test
    void getById_shouldReturnPhoto() {
        when(photoRepository.findById(1L)).thenReturn(Optional.of(photo));
        when(photoMapper.toDto(photo)).thenReturn(photoResponse);

        Optional<PhotoResponse> result = this.photoService.getById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void getById_shouldReturnEmptyWhenNoPhoto() {
        when(photoRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<PhotoResponse> result = this.photoService.getById(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void save_shouldPersistPhotoAndReturnDto() throws ParseException {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(photoRepository.save(any(Photo.class))).thenReturn(photo);
        when(photoMapper.toDto(photo)).thenReturn(photoResponse);

        PhotoResponse result = photoService.save(photoResponse);

        assertNotNull(result);
        assertEquals("original.jpg", result.getOriginalFileName());
        verify(photoRepository).save(any(Photo.class));
    }

    @Test
    void save_shouldThrowWhenUserNotFound() {
        PhotoResponse response = PhotoResponse.builder().authorId(999L).build();
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> photoService.save(response));
    }

    @Test
    void update_shouldUpdatePhotoAndReturnDto() throws ParseException {
        when(photoRepository.findById(1L)).thenReturn(Optional.of(photo));
        when(photoRepository.save(any(Photo.class))).thenReturn(photo);
        when(photoMapper.toDto(photo)).thenReturn(photoResponse);

        Hashtag testTag = Hashtag.builder().withTag("test").withUsageCount(1L).build();
        Hashtag photoTag = Hashtag.builder().withTag("photo").withUsageCount(1L).build();

        when(hashtagRepository.findByTag("test")).thenReturn(testTag);

        when(hashtagRepository.findByTag("photo")).thenReturn(photoTag);

        Optional<PhotoResponse> updatedResponse = photoService.update(photoResponse);

        assertNotNull(updatedResponse);
        assertEquals("original.jpg", updatedResponse.get().getOriginalFileName());
        verify(photoRepository).save(any(Photo.class));
    }

    @Test
    void update_shouldReturnEmptyWhenNoPhoto() throws ParseException {
        when(photoRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<PhotoResponse> result = photoService.update(photoResponse);

        assertTrue(result.isEmpty());
    }

    @Test
    void deleteById_shouldDeleteWhenPhotoExists() {
        when(photoRepository.findById(1L)).thenReturn(Optional.of(photo));

        photoService.deleteById(1L);

        verify(photoRepository).delete(photo);
    }

    @Test
    void deleteById_shouldDoNothingWhenNoPhotoExists() {
        when(photoRepository.findById(999L)).thenReturn(Optional.empty());

        photoService.deleteById(999L);

        verify(photoRepository, never()).delete(any(Photo.class));
    }

    @Test
    void filterByUser_shouldReturnAllPhotosForValidUser() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Photo> photoPage = new PageImpl<>(List.of(photo));

        when(userRepository.findByUsername("testuser")).thenReturn(user);
        when(photoRepository.findByUserOrderByUploadedAtDesc(user, pageable)).thenReturn(photoPage);
        when(photoMapper.toDto(photo)).thenReturn(photoResponse);

        Page<PhotoResponse> result = photoService.filterByUser("testuser", pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void filterByUser_shouldReturnEmptyWhenNoPhotosFound() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findByUsername("nonexisting")).thenReturn(null);

        Page<PhotoResponse> result = photoService.filterByUser("nonexisting", pageable);

        assertTrue(result.isEmpty());
    }
}
