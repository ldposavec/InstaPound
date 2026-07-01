package hr.algebra.nrako.instapound.controller;

import hr.algebra.nrako.instapound.enums.ImageFormat;
import hr.algebra.nrako.instapound.enums.StorageType;
import hr.algebra.nrako.instapound.enums.UserRole;
import hr.algebra.nrako.instapound.model.dto.request.PhotoEditRequest;
import hr.algebra.nrako.instapound.model.dto.request.PhotoSearchRequest;
import hr.algebra.nrako.instapound.model.dto.response.PhotoResponse;
import hr.algebra.nrako.instapound.model.entity.Hashtag;
import hr.algebra.nrako.instapound.model.entity.Photo;
import hr.algebra.nrako.instapound.model.entity.User;
import hr.algebra.nrako.instapound.model.entity.UserPackage;
import hr.algebra.nrako.instapound.model.mappers.PhotoMapper;
import hr.algebra.nrako.instapound.model.valueobject.ImageProcessingOptions;
import hr.algebra.nrako.instapound.repository.HashtagRepository;
import hr.algebra.nrako.instapound.repository.PhotoRepository;
import hr.algebra.nrako.instapound.repository.UserRepository;
import hr.algebra.nrako.instapound.service.implementations.ImageProcessorServiceImpl;
import hr.algebra.nrako.instapound.service.interfaces.ActionLogService;
import hr.algebra.nrako.instapound.service.interfaces.UserPackageService;
import hr.algebra.nrako.instapound.service.storage.StorageServiceImpl;
import hr.algebra.nrako.instapound.utils.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PhotoControllerUnitTest {

    @Mock
    private PhotoRepository photoRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HashtagRepository hashtagRepository;

    @Mock
    private StorageServiceImpl storageService;

    @Mock
    private ImageProcessorServiceImpl imageProcessorService;

    @Mock
    private ActionLogService actionLogService;

    @Mock
    private UserPackageService userPackageService;

    @Mock
    private PhotoMapper photoMapper;

    @Mock
    private IpUtils ipUtils;

    @Mock
    private HttpServletRequest request;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private PhotoController controller;

    private User buildUser(Long id, String username, UserRole role) {
        return User.builder()
                .id(id)
                .username(username)
                .role(role)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Photo buildPhoto(Long id, User owner) {
        return Photo.builder()
                .id(id)
                .user(owner)
                .originalFileName("a.jpg")
                .storedFileName("stored-a.jpg")
                .storageType(StorageType.LOCAL)
                .mimeType("image/jpeg")
                .build();
    }

    private PhotoResponse buildResponse(Long id) {
        return PhotoResponse.builder().id(id).originalFileName("a.jpg").build();
    }

    @BeforeEach
    void setUp() {
        lenient().when(userDetails.getUsername()).thenReturn("testuser");
        lenient().when(ipUtils.getClientIp(any())).thenReturn("192.168.0.23");
    }

    @Test
    void uploadPhoto_shouldReturnUnauthorizedWhenUserNotFound() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(null);
        MultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg", "data".getBytes());

        ResponseEntity<Object> response = controller.uploadPhoto(
                file, null, null, StorageType.LOCAL, null, null, null, null, userDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isEqualTo("User not found");
        verify(photoRepository, never()).save(any());
    }

    @Test
    void uploadPhoto_shouldReturnForbiddenWhenPackageLimitExceeded() throws Exception {
        User user = buildUser(1L, "testuser", UserRole.REGISTERED);
        UserPackage pkg = mock(UserPackage.class);
        when(userRepository.findByUsername("testuser")).thenReturn(user);
        when(userPackageService.getPackageByType(any())).thenReturn(pkg);
        User spyUser = org.mockito.Mockito.spy(user);
        when(userRepository.findByUsername("testuser")).thenReturn(spyUser);
        org.mockito.Mockito.doReturn(false).when(spyUser).canUpload(any(), eq(pkg));
        MultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg", new byte[10]);

        ResponseEntity<Object> response = controller.uploadPhoto(
                file, null, null, StorageType.LOCAL, null, null, null, null, userDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isEqualTo(
                "Package limit exceeded. Please upgrade your package or wait until tomorrow");
        verify(photoRepository, never()).save(any());
    }

    @Test
    void uploadPhoto_shouldSucceedWithoutProcessingOptions() throws Exception {
        User user = buildUser(1L, "testuser", UserRole.REGISTERED);
        when(userRepository.findByUsername("testuser")).thenReturn(user);
        when(userPackageService.getPackageByType(any())).thenReturn(null);
        when(imageProcessorService.createThumbnail(any(), anyInt(), anyInt())).thenReturn(new byte[]{1, 2});
        when(imageProcessorService.getImageDimensions(any())).thenReturn(new int[]{100, 200});
        when(storageService.getUrl(any(), any())).thenReturn("http://url");
        when(photoMapper.toDto(any())).thenReturn(buildResponse(5L));
        MultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg", "data".getBytes());

        ResponseEntity<Object> response = controller.uploadPhoto(
                file, "desc", null, StorageType.LOCAL, null, null, null, null, userDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(PhotoResponse.class);
        verify(photoRepository).save(any(Photo.class));
        verify(userRepository).save(user);
        verify(actionLogService).logActionWithTargetPhoto(eq(user), any(), any(), any(), any());
        verify(imageProcessorService, never()).processImage(any(), any());
    }

    @Test
    void uploadPhoto_shouldAppliesProcessingWhenOptionsGiven() throws Exception {
        User user = buildUser(1L, "testuser", UserRole.REGISTERED);
        when(userRepository.findByUsername("testuser")).thenReturn(user);
        when(userPackageService.getPackageByType(any())).thenReturn(null);
        when(imageProcessorService.processImage(any(), any(ImageProcessingOptions.class)))
                .thenReturn(new byte[]{9, 9, 9});
        when(imageProcessorService.createThumbnail(any(), anyInt(), anyInt())).thenReturn(new byte[]{1});
        when(imageProcessorService.getImageDimensions(any())).thenReturn(new int[]{50, 50});
        when(storageService.getUrl(any(), any())).thenReturn("http://url");
        when(photoMapper.toDto(any())).thenReturn(buildResponse(6L));
        MultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg", "data".getBytes());

        ResponseEntity<Object> response = controller.uploadPhoto(
                file, null, Set.of("nature"), StorageType.LOCAL, ImageFormat.PNG, null, 50, null, userDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(imageProcessorService).processImage(any(), any(ImageProcessingOptions.class));
        verify(photoRepository).save(any(Photo.class));
    }

    @Test
    void uploadPhoto_shouldCreateNewHashtagsAndPersistsThem() throws Exception {
        User user = buildUser(1L, "testuser", UserRole.REGISTERED);
        when(userRepository.findByUsername("testuser")).thenReturn(user);
        when(userPackageService.getPackageByType(any())).thenReturn(null);
        when(imageProcessorService.createThumbnail(any(), anyInt(), anyInt())).thenReturn(new byte[]{1});
        when(imageProcessorService.getImageDimensions(any())).thenReturn(new int[]{10, 10});
        when(storageService.getUrl(any(), any())).thenReturn("http://url");
        when(photoMapper.toDto(any())).thenReturn(buildResponse(7L));
        when(hashtagRepository.findByTag("sunset")).thenReturn(null);
        MultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg", "data".getBytes());

        ResponseEntity<Object> response = controller.uploadPhoto(
                file, null, Set.of("Sunset"), StorageType.LOCAL, null, null, null, null, userDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(hashtagRepository).save(any(Hashtag.class));
    }

    @Test
    void browsePhotos_shouldReturnMappedPageAndLogs() {
        Photo p = buildPhoto(1L, buildUser(1L, "testuser", UserRole.REGISTERED));
        when(photoRepository.findAllByOrderByUploadedAtDesc(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(p), PageRequest.of(0, 15), 1));
        when(photoMapper.toDto(p)).thenReturn(buildResponse(1L));

        ResponseEntity<Page<PhotoResponse>> response = controller.browsePhotos(0, 15, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().getFirst().getOriginalFileName()).isEqualTo("a.jpg");
        verify(actionLogService).logAnonymousAction(any(), any(), any());
    }

    @Test
    void browsePhotos_shouldPassPageRequestWithGivenPageAndSize() {
        when(photoRepository.findAllByOrderByUploadedAtDesc(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(2, 5), 0));

        controller.browsePhotos(2, 5, request);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(photoRepository).findAllByOrderByUploadedAtDesc(captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(2);
        assertThat(captor.getValue().getPageSize()).isEqualTo(5);
    }

    @Test
    void searchPhotos_shouldUseDefaultPagingWhenRequestFieldsNull() {
        PhotoSearchRequest req = new PhotoSearchRequest();
        req.setPage(null);
        req.setPageSize(null);
        Photo p = buildPhoto(1L, buildUser(1L, "testuser", UserRole.REGISTERED));
        when(photoRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(p), PageRequest.of(0, 10), 1));
        when(photoMapper.toDto(p)).thenReturn(buildResponse(1L));

        ResponseEntity<Page<PhotoResponse>> response = controller.searchPhotos(req, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(photoRepository).findAll(any(Specification.class), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(0);
        assertThat(captor.getValue().getPageSize()).isEqualTo(10);
        verify(actionLogService).logAnonymousAction(any(), any(), any());
    }

    @Test
    void searchPhotos_shouldUseProvidedPagingValues() {
        PhotoSearchRequest req = PhotoSearchRequest.builder().page(3).pageSize(7).build();
        when(photoRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(3, 7), 0));

        controller.searchPhotos(req, request);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(photoRepository).findAll(any(Specification.class), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(3);
        assertThat(captor.getValue().getPageSize()).isEqualTo(7);
    }

    @Test
    void getPhoto_shouldReturnNotFoundWhenMissing() {
        when(photoRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<PhotoResponse> response = controller.getPhoto(99L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(photoRepository, never()).save(any());
    }

    @Test
    void getPhoto_shouldIncrementViewCountSavesAndReturnsDto() {
        Photo p = buildPhoto(1L, buildUser(1L, "testuser", UserRole.REGISTERED));
        long before = p.getViewCount();
        when(photoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(photoMapper.toDto(p)).thenReturn(buildResponse(1L));

        ResponseEntity<PhotoResponse> response = controller.getPhoto(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(p.getViewCount()).isEqualTo(before + 1);
        verify(photoRepository).save(p);
        verify(actionLogService).logAnonymousAction(any(), any(), any());
    }

    @Test
    void editPhoto_shouldReturnUnauthorizedWhenUserNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(null);
        PhotoEditRequest editRequest = PhotoEditRequest.builder().description("x").build();

        ResponseEntity<Object> response = controller.editPhoto(1L, editRequest, userDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isEqualTo("User not found");
    }

    @Test
    void editPhoto_shouldReturnNotFoundWhenPhotoMissing() {
        User user = buildUser(1L, "testuser", UserRole.REGISTERED);
        when(userRepository.findByUsername("testuser")).thenReturn(user);
        when(photoRepository.findById(1L)).thenReturn(Optional.empty());
        PhotoEditRequest editRequest = PhotoEditRequest.builder().description("x").build();

        ResponseEntity<Object> response = controller.editPhoto(1L, editRequest, userDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void editPhoto_shouldReturnForbiddenWhenNotOwnerAndNotAdmin() {
        User owner = buildUser(1L, "owner", UserRole.REGISTERED);
        User other = buildUser(2L, "testuser", UserRole.REGISTERED);
        Photo p = buildPhoto(1L, owner);
        when(userRepository.findByUsername("testuser")).thenReturn(other);
        when(photoRepository.findById(1L)).thenReturn(Optional.of(p));
        PhotoEditRequest editRequest = PhotoEditRequest.builder().description("x").build();

        ResponseEntity<Object> response = controller.editPhoto(1L, editRequest, userDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isEqualTo("You can only edit your own photos");
        verify(photoRepository, never()).save(any());
    }

    @Test
    void editPhoto_shouldUpdateDescriptionAndHashtagForOwner() {
        User owner = buildUser(1L, "testuser", UserRole.REGISTERED);
        Photo photo = buildPhoto(1L, owner);

        when(userRepository.findByUsername("testuser")).thenReturn(owner);
        when(photoRepository.findById(1L)).thenReturn(Optional.of(photo));
        when(hashtagRepository.findByTag("nature")).thenReturn(null);
        when(photoMapper.toDto(photo)).thenReturn(buildResponse(1L));

        PhotoEditRequest editRequest = PhotoEditRequest.builder()
                .description("Updated description")
                .hashtags(Set.of("nature"))
                .build();
        ResponseEntity<Object> response = controller.editPhoto(1L, editRequest, userDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(photo.getDescription()).isEqualTo("Updated description");
        assertThat(photo.getEditedAt()).isNotNull();
        verify(hashtagRepository).save(any(Hashtag.class));
        verify(photoRepository).save(photo);
        verify(actionLogService).logActionWithTargetPhoto(eq(owner), any(), any(), any(), eq(1L));
    }

    @Test
    void editPhoto_shouldAllowAdminToEditOthersPhoto() {
        User owner = buildUser(1L, "owner", UserRole.REGISTERED);
        User admin = buildUser(2L, "testuser", UserRole.ADMIN);
        Photo p = buildPhoto(1L, owner);
        when(userRepository.findByUsername("testuser")).thenReturn(admin);
        when(photoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(photoMapper.toDto(p)).thenReturn(buildResponse(1L));
        PhotoEditRequest editRequest = PhotoEditRequest.builder().description("admin edit").build();

        ResponseEntity<Object> response = controller.editPhoto(1L, editRequest, userDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(p.getDescription()).isEqualTo("admin edit");
        verify(photoRepository).save(p);
    }

    @Test
    void deletePhoto_shouldReturnUnauthorizedWhenUserNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(null);

        ResponseEntity<Object> response = controller.deletePhoto(1L, userDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isEqualTo("User not found");
    }

    @Test
    void deletePhoto_shouldReturnNotFoundWhenPhotoMissing() {
        User user = buildUser(1L, "testuser", UserRole.REGISTERED);
        when(userRepository.findByUsername("testuser")).thenReturn(user);
        when(photoRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Object> response = controller.deletePhoto(1L, userDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deletePhoto_shouldReturnForbiddenWhenNotOwner() {
        User owner = buildUser(1L, "owner", UserRole.REGISTERED);
        User other = buildUser(2L, "testuser", UserRole.REGISTERED);
        Photo p = buildPhoto(1L, owner);
        when(userRepository.findByUsername("testuser")).thenReturn(other);
        when(photoRepository.findById(1L)).thenReturn(Optional.of(p));

        ResponseEntity<Object> response = controller.deletePhoto(1L, userDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isEqualTo("You can only delete your own photos");
        verify(photoRepository, never()).delete(any(Photo.class));
    }

    @Test
    void deletePhoto_shouldSucceedForOwner() throws Exception {
        User owner = buildUser(1L, "testuser", UserRole.REGISTERED);
        Photo p = buildPhoto(1L, owner);
        when(userRepository.findByUsername("testuser")).thenReturn(owner);
        when(photoRepository.findById(1L)).thenReturn(Optional.of(p));

        ResponseEntity<Object> response = controller.deletePhoto(1L, userDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Photo deleted successfully");
        verify(storageService).delete(eq("stored-a.jpg"), eq(StorageType.LOCAL));
        verify(photoRepository).delete(p);
        verify(actionLogService).logActionWithTargetPhoto(eq(owner), any(), any(), any(), eq(1L));
    }

    @Test
    void deletePhoto_shouldReturnInternalServerErrorOnIoException() throws Exception {
        User owner = buildUser(1L, "testuser", UserRole.REGISTERED);
        Photo p = buildPhoto(1L, owner);
        when(userRepository.findByUsername("testuser")).thenReturn(owner);
        when(photoRepository.findById(1L)).thenReturn(Optional.of(p));
        org.mockito.Mockito.doThrow(new IOException("io"))
                .when(storageService).delete(eq("stored-a.jpg"), eq(StorageType.LOCAL));

        ResponseEntity<Object> response = controller.deletePhoto(1L, userDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(String.valueOf(response.getBody())).contains("Error deleting photo");
        verify(photoRepository, never()).delete(any(Photo.class));
    }

    @Test
    void downloadPhoto_shouldReturnNotFoundWhenMissing() {
        when(photoRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Resource> response = controller.downloadPhoto(
                1L, true, null, null, null, null, userDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void downloadPhoto_shouldReturnOriginalForAnonymousUser() throws Exception {
        User owner = buildUser(1L, "owner", UserRole.REGISTERED);
        Photo p = buildPhoto(1L, owner);
        when(photoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(storageService.retrieve(any(), any())).thenReturn(new ByteArrayInputStream("bytes".getBytes()));

        ResponseEntity<Resource> response = controller.downloadPhoto(
                1L, true, null, null, null, null, null, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .isEqualTo("attachment;filename=\"a.jpg\"");
        verify(photoRepository).save(p);
        verify(actionLogService).logAnonymousAction(any(), any(), any());
        verify(actionLogService, never()).logActionWithTargetPhoto(any(), any(), any(), any(), any());
    }

    @Test
    void downloadPhoto_shouldLogUserActionWhenAuthenticated() throws Exception {
        User owner = buildUser(1L, "testuser", UserRole.REGISTERED);
        Photo p = buildPhoto(1L, owner);
        when(photoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(userRepository.findByUsername("testuser")).thenReturn(owner);
        when(storageService.retrieve(any(), any())).thenReturn(new ByteArrayInputStream("bytes".getBytes()));

        ResponseEntity<Resource> response = controller.downloadPhoto(
                1L, true, null, null, null, null, userDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(actionLogService).logActionWithTargetPhoto(eq(owner), any(), any(), any(), eq(1L));
    }

    @Test
    void downloadPhoto_shouldApplyProcessingWhenNotOriginal() throws Exception {
        User owner = buildUser(1L, "owner", UserRole.REGISTERED);
        Photo p = buildPhoto(1L, owner);
        when(photoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(storageService.retrieve(any(), any())).thenReturn(new ByteArrayInputStream("bytes".getBytes()));
        when(imageProcessorService.processImage(any(), any(ImageProcessingOptions.class)))
                .thenReturn(new byte[]{1, 2, 3});

        ResponseEntity<Resource> response = controller.downloadPhoto(
                1L, false, ImageFormat.PNG, null, null, null, null, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .isEqualTo("attachment;filename=\"a.png\"");
        verify(imageProcessorService).processImage(any(), any(ImageProcessingOptions.class));
    }

    @Test
    void downloadPhoto_shouldReturnInternalServerErrorOnIoException() throws Exception {
        User owner = buildUser(1L, "owner", UserRole.REGISTERED);
        Photo p = buildPhoto(1L, owner);
        when(photoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(storageService.retrieve(any(), any())).thenThrow(new IOException("io"));

        ResponseEntity<Resource> response = controller.downloadPhoto(
                1L, true, null, null, null, null, null, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void getPhotosByUser_shouldReturnNotFoundWhenUserMissing() {
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        ResponseEntity<Page<PhotoResponse>> response = controller.getPhotosByUser(5L, 0, 15);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getPhotosByUser_shouldReturnPagedPhotos() {
        User owner = buildUser(5L, "owner", UserRole.REGISTERED);
        Photo p = buildPhoto(1L, owner);
        when(userRepository.findById(5L)).thenReturn(Optional.of(owner));
        when(photoRepository.findByUserOrderByUploadedAtDesc(eq(owner), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(p), PageRequest.of(0, 15), 1));
        when(photoMapper.toDto(p)).thenReturn(buildResponse(1L));

        ResponseEntity<Page<PhotoResponse>> response = controller.getPhotosByUser(5L, 0, 15);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void serveFile_shouldReturnResourceWhenPhotoFound() throws Exception {
        User owner = buildUser(1L, "owner", UserRole.REGISTERED);
        Photo p = buildPhoto(1L, owner);
        when(photoRepository.findByStoredFileName("stored-a.jpg")).thenReturn(Optional.of(p));
        when(storageService.retrieve(eq("stored-a.jpg"), eq(StorageType.LOCAL)))
                .thenReturn(new ByteArrayInputStream("bytes".getBytes()));

        ResponseEntity<Resource> response = controller.serveFile("stored-a.jpg");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getCacheControl()).isEqualTo("public, max-age=86400");
    }

    @Test
    void serveFile_shouldResolveThumbnailToOriginalPhoto() throws Exception {
        User owner = buildUser(1L, "owner", UserRole.REGISTERED);
        Photo p = buildPhoto(1L, owner);
        when(photoRepository.findByStoredFileName("thumb_stored-a.jpg")).thenReturn(Optional.empty());
        when(photoRepository.findByStoredFileName("stored-a.jpg")).thenReturn(Optional.of(p));
        when(storageService.retrieve(eq("thumb_stored-a.jpg"), eq(StorageType.LOCAL)))
                .thenReturn(new ByteArrayInputStream("thumb".getBytes()));

        ResponseEntity<Resource> response = controller.serveFile("thumb_stored-a.jpg");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(photoRepository).findByStoredFileName("stored-a.jpg");
    }

    @Test
    void serveFile_shouldFallBackToLocalWhenPhotoNotFound() throws Exception {
        when(photoRepository.findByStoredFileName("unknown.jpg")).thenReturn(Optional.empty());
        when(storageService.retrieve(eq("unknown.jpg"), eq(StorageType.LOCAL)))
                .thenReturn(new ByteArrayInputStream("bytes".getBytes()));

        ResponseEntity<Resource> response = controller.serveFile("unknown.jpg");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void serveFile_shouldReturnNotFoundOnIoException() throws Exception {
        when(photoRepository.findByStoredFileName("missing.jpg")).thenReturn(Optional.empty());
        when(storageService.retrieve(eq("missing.jpg"), any())).thenThrow(new IOException("io"));

        ResponseEntity<Resource> response = controller.serveFile("missing.jpg");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private static <T> T mock(Class<T> type) {
        return org.mockito.Mockito.mock(type);
    }
}
