package hr.algebra.nrako.instapound.service;

import hr.algebra.nrako.instapound.model.valueobject.ImageProcessingOptions;
import hr.algebra.nrako.instapound.service.implementations.ImageProcessorServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageProcessorServiceImplTest {

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
        ImageIO.write(img, "jpeg", out);
        return out.toByteArray();
    }

    @Test
    void testGetImageDimensions() throws IOException {
        byte[] data = createTestImage(100, 80);
        int[] dims = service.getImageDimensions(data);
        Assertions.assertArrayEquals(new int[]{100, 80}, dims);
    }

    @Test
    void testCreateThumbnailSmaller() throws IOException {
        byte[] data = createTestImage(400, 300);
        byte[] thumb = service.createThumbnail(new ByteArrayInputStream(data), 100, 80);
        int[] dims = service.getImageDimensions(thumb);
        Assertions.assertTrue(dims[0] <= 100 && dims[1] <= 80);
    }

    @Test
    void testProcessImageResize() throws IOException {
        byte[] data = createTestImage(200, 200);
        ImageProcessingOptions opts = ImageProcessingOptions.builder().width(50).height(50).build();
        byte[] processed = service.processImage(new ByteArrayInputStream(data), opts);
        int[] dims = service.getImageDimensions(processed);
        Assertions.assertArrayEquals(new int[]{50, 50}, dims);
    }
}

