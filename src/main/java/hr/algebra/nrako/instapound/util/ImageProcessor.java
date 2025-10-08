package hr.algebra.nrako.instapound.util;

import hr.algebra.nrako.instapound.domain.valueobject.ImageProcessingOptions;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Strategy Pattern (Design Pattern #3) - Interface
 * Interface for different image processing strategies
 * Adheres to Interface Segregation Principle (SOLID)
 */
public interface ImageProcessor {
    
    /**
     * Process an image according to the given options
     * 
     * @param file The image file to process
     * @param options Processing options
     * @return Processed image as byte array
     * @throws IOException if processing fails
     */
    byte[] processImage(MultipartFile file, ImageProcessingOptions options) throws IOException;
    
    /**
     * Generate a thumbnail from an image
     * 
     * @param file The image file
     * @param width Thumbnail width
     * @param height Thumbnail height
     * @return Thumbnail as byte array
     * @throws IOException if processing fails
     */
    byte[] generateThumbnail(MultipartFile file, int width, int height) throws IOException;
}
