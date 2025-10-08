package hr.algebra.nrako.instapound.util;

import hr.algebra.nrako.instapound.domain.enums.ImageFilter;
import hr.algebra.nrako.instapound.domain.valueobject.ImageProcessingOptions;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Strategy Pattern (Design Pattern #3) - Concrete Implementation
 * Basic image processing implementation using Java AWT
 * Adheres to Open/Closed Principle (SOLID)
 */
@Component
public class BasicImageProcessor implements ImageProcessor {
    
    @Override
    public byte[] processImage(MultipartFile file, ImageProcessingOptions options) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        
        // Apply filters
        if (options.getFilters() != null) {
            for (ImageFilter filter : options.getFilters()) {
                image = applyFilter(image, filter);
            }
        }
        
        // Resize if needed
        if (options.getWidth() != null && options.getHeight() != null) {
            image = resize(image, options.getWidth(), options.getHeight());
        }
        
        // Convert to byte array with specified format
        String format = options.getFormat() != null ? 
            options.getFormat().name().toLowerCase() : "jpg";
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return baos.toByteArray();
    }
    
    @Override
    public byte[] generateThumbnail(MultipartFile file, int width, int height) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        BufferedImage thumbnail = resize(image, width, height);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(thumbnail, "jpg", baos);
        return baos.toByteArray();
    }
    
    private BufferedImage resize(BufferedImage originalImage, int targetWidth, int targetHeight) {
        Image tmp = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        
        return resized;
    }
    
    private BufferedImage applyFilter(BufferedImage image, ImageFilter filter) {
        switch (filter) {
            case SEPIA:
                return applySepia(image);
            case GRAYSCALE:
                return applyGrayscale(image);
            case BLUR:
                return applyBlur(image);
            case SHARPEN:
                return applySharpen(image);
            case VINTAGE:
                return applyVintage(image);
            default:
                return image;
        }
    }
    
    private BufferedImage applySepia(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage sepia = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                
                int tr = (int) (0.393 * r + 0.769 * g + 0.189 * b);
                int tg = (int) (0.349 * r + 0.686 * g + 0.168 * b);
                int tb = (int) (0.272 * r + 0.534 * g + 0.131 * b);
                
                tr = Math.min(255, tr);
                tg = Math.min(255, tg);
                tb = Math.min(255, tb);
                
                sepia.setRGB(x, y, (tr << 16) | (tg << 8) | tb);
            }
        }
        return sepia;
    }
    
    private BufferedImage applyGrayscale(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage grayscale = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                
                int gray = (r + g + b) / 3;
                grayscale.setRGB(x, y, (gray << 16) | (gray << 8) | gray);
            }
        }
        return grayscale;
    }
    
    private BufferedImage applyBlur(BufferedImage image) {
        float[] matrix = {
            1/9f, 1/9f, 1/9f,
            1/9f, 1/9f, 1/9f,
            1/9f, 1/9f, 1/9f
        };
        Kernel kernel = new Kernel(3, 3, matrix);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        return op.filter(image, null);
    }
    
    private BufferedImage applySharpen(BufferedImage image) {
        float[] matrix = {
            0, -1, 0,
            -1, 5, -1,
            0, -1, 0
        };
        Kernel kernel = new Kernel(3, 3, matrix);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        return op.filter(image, null);
    }
    
    private BufferedImage applyVintage(BufferedImage image) {
        // Vintage = Sepia + slight vignette effect
        BufferedImage vintage = applySepia(image);
        
        int width = vintage.getWidth();
        int height = vintage.getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        int maxDistance = (int) Math.sqrt(centerX * centerX + centerY * centerY);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int distance = (int) Math.sqrt((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY));
                float factor = 1 - (float) distance / maxDistance * 0.5f;
                
                int rgb = vintage.getRGB(x, y);
                int r = (int) (((rgb >> 16) & 0xff) * factor);
                int g = (int) (((rgb >> 8) & 0xff) * factor);
                int b = (int) ((rgb & 0xff) * factor);
                
                vintage.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        return vintage;
    }
}
