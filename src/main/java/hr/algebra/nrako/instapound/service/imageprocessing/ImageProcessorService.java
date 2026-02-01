package hr.algebra.nrako.instapound.service.imageprocessing;

import hr.algebra.nrako.instapound.enums.ImageFilter;
import hr.algebra.nrako.instapound.enums.ImageFormat;
import hr.algebra.nrako.instapound.model.valueobject.ImageProcessingOptions;
import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Image Processor Service - Template Method Pattern
 * Provides various image processing operations (resize, filters, format conversion)
 */
@Service
@Slf4j
public class ImageProcessorService {

    /**
     * Process an image with the given options - Template Method
     */
    public byte[] processImage(InputStream inputStream, ImageProcessingOptions options) throws IOException {
        BufferedImage image = ImageIO.read(inputStream);
        if (image == null) {
            throw new IOException("Could not read image");
        }

        // Apply resize if specified
        image = applyResize(image, options);

        // Apply filters
        if (options.getFilters() != null) {
            for (ImageFilter filter : options.getFilters()) {
                image = applyFilter(image, filter);
            }
        }

        // Convert to specified format
        return convertToFormat(image, options.getImageFormat(), options.getQuality());
    }

    /**
     * Create a thumbnail
     */
    public byte[] createThumbnail(InputStream inputStream, int maxWidth, int maxHeight) throws IOException {
        BufferedImage image = ImageIO.read(inputStream);
        if (image == null) {
            throw new IOException("Could not read image");
        }

        BufferedImage thumbnail = Scalr.resize(image, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, 
                maxWidth, maxHeight, Scalr.OP_ANTIALIAS);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(thumbnail, "jpg", baos);
        return baos.toByteArray();
    }

    private BufferedImage applyResize(BufferedImage image, ImageProcessingOptions options) {
        if (options.getWidth() != null && options.getHeight() != null) {
            return Scalr.resize(image, Scalr.Method.QUALITY, Scalr.Mode.FIT_EXACT,
                    options.getWidth(), options.getHeight(), Scalr.OP_ANTIALIAS);
        } else if (options.getWidth() != null) {
            return Scalr.resize(image, Scalr.Method.QUALITY, Scalr.Mode.FIT_TO_WIDTH,
                    options.getWidth(), Scalr.OP_ANTIALIAS);
        } else if (options.getHeight() != null) {
            return Scalr.resize(image, Scalr.Method.QUALITY, Scalr.Mode.FIT_TO_HEIGHT,
                    options.getHeight(), Scalr.OP_ANTIALIAS);
        }
        return image;
    }

    private BufferedImage applyFilter(BufferedImage image, ImageFilter filter) {
        return switch (filter) {
            case GRAYSCALE -> applyGrayscale(image);
            case SEPIA -> applySepia(image);
            case INVERT -> applyInvert(image);
            case BLUR -> applyBlur(image);
            case SHARPEN -> applySharpen(image);
            case VINTAGE -> applyVintage(image);
            case NONE -> image;
        };
    }

    private BufferedImage applyGrayscale(BufferedImage image) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = new Color(image.getRGB(x, y));
                int gray = (int) (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue());
                result.setRGB(x, y, new Color(gray, gray, gray).getRGB());
            }
        }
        return result;
    }

    private BufferedImage applySepia(BufferedImage image) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = new Color(image.getRGB(x, y));
                int r = (int) Math.min(255, 0.393 * color.getRed() + 0.769 * color.getGreen() + 0.189 * color.getBlue());
                int g = (int) Math.min(255, 0.349 * color.getRed() + 0.686 * color.getGreen() + 0.168 * color.getBlue());
                int b = (int) Math.min(255, 0.272 * color.getRed() + 0.534 * color.getGreen() + 0.131 * color.getBlue());
                result.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }
        return result;
    }

    private BufferedImage applyInvert(BufferedImage image) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = new Color(image.getRGB(x, y));
                result.setRGB(x, y, new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue()).getRGB());
            }
        }
        return result;
    }

    private BufferedImage applyBlur(BufferedImage image) {
        float[] blurKernel = {
                1/9f, 1/9f, 1/9f,
                1/9f, 1/9f, 1/9f,
                1/9f, 1/9f, 1/9f
        };
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        new ConvolveOp(new Kernel(3, 3, blurKernel), ConvolveOp.EDGE_NO_OP, null).filter(image, result);
        return result;
    }

    private BufferedImage applySharpen(BufferedImage image) {
        float[] sharpenKernel = {
                0, -1, 0,
                -1, 5, -1,
                0, -1, 0
        };
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        new ConvolveOp(new Kernel(3, 3, sharpenKernel), ConvolveOp.EDGE_NO_OP, null).filter(image, result);
        return result;
    }

    private BufferedImage applyVintage(BufferedImage image) {
        // Apply sepia first, then add some noise
        BufferedImage sepia = applySepia(image);
        // Add slight vignette effect
        BufferedImage result = new BufferedImage(sepia.getWidth(), sepia.getHeight(), BufferedImage.TYPE_INT_RGB);
        int centerX = sepia.getWidth() / 2;
        int centerY = sepia.getHeight() / 2;
        double maxDist = Math.sqrt(centerX * centerX + centerY * centerY);

        for (int y = 0; y < sepia.getHeight(); y++) {
            for (int x = 0; x < sepia.getWidth(); x++) {
                Color color = new Color(sepia.getRGB(x, y));
                double dist = Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2));
                double factor = 1.0 - (dist / maxDist) * 0.3;
                int r = (int) Math.max(0, Math.min(255, color.getRed() * factor));
                int g = (int) Math.max(0, Math.min(255, color.getGreen() * factor));
                int b = (int) Math.max(0, Math.min(255, color.getBlue() * factor));
                result.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }
        return result;
    }

    private byte[] convertToFormat(BufferedImage image, ImageFormat format, Integer quality) throws IOException {
        String formatName = format != null ? format.name().toLowerCase() : "jpeg";
        if (formatName.equals("jpg")) formatName = "jpeg";
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, formatName, baos);
        return baos.toByteArray();
    }

    /**
     * Get image dimensions
     */
    public int[] getImageDimensions(byte[] imageData) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
        if (image == null) {
            throw new IOException("Could not read image");
        }
        return new int[]{image.getWidth(), image.getHeight()};
    }
}
