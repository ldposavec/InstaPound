package hr.algebra.nrako.instapound.service.implementations;

import java.awt.*;

import hr.algebra.nrako.instapound.enums.ImageFilter;
import hr.algebra.nrako.instapound.enums.ImageFormat;
import hr.algebra.nrako.instapound.model.valueobject.ImageProcessingOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.*;

class ImageProcessorServiceImplTest {

    private ImageProcessorServiceImpl service;

    @BeforeEach
    void setup() {
        service = new ImageProcessorServiceImpl();
    }

    private byte[] createTestImage(int w, int h) throws IOException {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, w, h);
        g.dispose();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "png", out);
        return out.toByteArray();
    }

    @Test
    void getImageDimensions_shouldReturnImageDimensions() throws IOException {
        byte[] data = createTestImage(100, 80);
        int[] dims = service.getImageDimensions(data);
        Assertions.assertArrayEquals(new int[]{100, 80}, dims);
    }

    @Test
    void createThumbnail_shouldCreateSmallerImage() throws IOException {
        byte[] data = createTestImage(400, 300);
        byte[] thumb = service.createThumbnail(new ByteArrayInputStream(data), 100, 80);
        int[] dims = service.getImageDimensions(thumb);
        Assertions.assertTrue(dims[0] <= 100 && dims[1] <= 80);
    }

    @Test
    void processImage_shouldResizeImage() throws IOException {
        byte[] data = createTestImage(200, 200);
        ImageProcessingOptions opts = ImageProcessingOptions.builder().width(50).height(50).build();
        byte[] processed = service.processImage(new ByteArrayInputStream(data), opts);
        int[] dims = service.getImageDimensions(processed);
        Assertions.assertArrayEquals(new int[]{50, 50}, dims);
    }

    @Test
    void processImage_shouldApplyGrayscale() throws IOException {
        byte[] data = createTestImage(200, 200);
        ImageProcessingOptions opts = ImageProcessingOptions.builder()
                .filters(Set.of(ImageFilter.GRAYSCALE))
                .imageFormat(ImageFormat.PNG)
                .build();

        byte[] processed = service.processImage(new ByteArrayInputStream(data), opts);

        assertNotNull(processed);
        assertTrue(processed.length > 0);
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(processed));
        assertNotNull(img);
        assertEquals(200, img.getWidth());
        assertEquals(200, img.getHeight());
        int rgb = img.getRGB(100, 100);
        Color color = new Color(rgb);
        assertEquals(color.getRed(), color.getGreen());
        assertEquals(color.getGreen(), color.getBlue());
    }

    @Test
    void processImage_shouldThrowWhenInputIsInvalid() throws IOException {
        byte[] data = "not an image".getBytes();
        ImageProcessingOptions opts = ImageProcessingOptions.builder().imageFormat(ImageFormat.PNG).build();

        assertThrows(IOException.class, () -> service.processImage(new ByteArrayInputStream(data), opts));
    }

    @Test
    void createThumbnail_shouldThrowWhenThumbnailIsInvalid() throws IOException {
        byte[] data = "not an image".getBytes();
        ImageProcessingOptions opts = ImageProcessingOptions.builder().imageFormat(ImageFormat.PNG).build();
        assertThrows(IOException.class, () -> service.createThumbnail(new ByteArrayInputStream(data), 100, 100));
    }

    @Test
    void createThumbnail_shouldProduceJpegOutput() throws IOException {
        byte[] data = createTestImage(200, 200);
        byte[] thumb = service.createThumbnail(new ByteArrayInputStream(data), 100, 100);
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(thumb));
        assertNotNull(img);
        assertEquals(100, img.getWidth());
        assertEquals(100, img.getHeight());
        assertEquals((byte) 0xFF, thumb[0]);
        assertEquals((byte) 0xD8, thumb[1]);
    }

    @Test
    void getImageDimensions_shouldThrowWhenInputIsInvalid() throws IOException {
        byte[] data = "not an image".getBytes();
        assertThrows(IOException.class, () -> service.getImageDimensions(data));
    }

    @Test
    void getImageDimensions_shouldReturnArrayOfLengthTwo() throws IOException {
        byte[] data = createTestImage(200, 200);
        int[] dims = service.getImageDimensions(data);
        assertEquals(2, dims.length);
    }

    @Test
    void processImage_shouldApplyInvertFilter() throws IOException {
        byte[] data = createTestImage(200, 200);
        ImageProcessingOptions opts = ImageProcessingOptions.builder()
                .filters(Set.of(ImageFilter.INVERT))
                .build();
        byte[] processed = service.processImage(new ByteArrayInputStream(data), opts);

        assertNotNull(processed);
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(processed));
        assertNotNull(img);
    }

//    @Test
//    void processImage_shouldApplyBlurFilter() throws IOException {
//        byte[] data = createTestImage(1200, 1200);
//        ImageProcessingOptions opts = ImageProcessingOptions.builder()
//                .filters(Set.of(ImageFilter.BLUR))
//                .imageFormat(ImageFormat.PNG)
//                .build();
//        byte[] processed = service.processImage(new ByteArrayInputStream(data), opts);
//
//        assertNotNull(processed);
//        BufferedImage img = ImageIO.read(new ByteArrayInputStream(processed));
//        assertNotNull(img);
//    }

//    @Test
//    void processImage_shouldApplySharpenFilter() throws IOException {
//        byte[] data = createTestImage(200, 200);
//        ImageProcessingOptions opts = ImageProcessingOptions.builder()
//                .filters(Set.of(ImageFilter.SHARPEN))
//                .build();
//        byte[] processed = service.processImage(new ByteArrayInputStream(data), opts);
//
//        assertNotNull(processed);
//        BufferedImage img = ImageIO.read(new ByteArrayInputStream(processed));
//        assertNotNull(img);
//    }

    @Test
    void processImage_shouldApplyVintageFilter() throws IOException {
        byte[] data = createTestImage(200, 200);
        ImageProcessingOptions opts = ImageProcessingOptions.builder()
                .filters(Set.of(ImageFilter.VINTAGE))
                .build();
        byte[] processed = service.processImage(new ByteArrayInputStream(data), opts);

        assertNotNull(processed);
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(processed));
        assertNotNull(img);
    }

    @Test
    void processImage_shouldHandleNoneFilter() throws IOException {
        byte[] data = createTestImage(200, 200);
        ImageProcessingOptions opts = ImageProcessingOptions.builder()
                .filters(Set.of(ImageFilter.NONE))
                .build();
        byte[] processed = service.processImage(new ByteArrayInputStream(data), opts);

        assertNotNull(processed);
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(processed));
        assertNotNull(img);
    }

    @Test
    void processImage_shouldResizeToWidthOnly() throws IOException {
        byte[] data = createTestImage(200, 100);
        ImageProcessingOptions opts = ImageProcessingOptions.builder().width(100).build();
        byte[] processed = service.processImage(new ByteArrayInputStream(data), opts);
        int[] dims = service.getImageDimensions(processed);
        assertEquals(100, dims[0]);
    }

    @Test
    void processImage_shouldResizeToHeightOnly() throws IOException {
        byte[] data = createTestImage(200, 100);
        ImageProcessingOptions opts = ImageProcessingOptions.builder().height(50).build();
        byte[] processed = service.processImage(new ByteArrayInputStream(data), opts);
        int[] dims = service.getImageDimensions(processed);
        assertEquals(50, dims[1]);
    }
}

