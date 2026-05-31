package vn.icktmeanz.trafficViolation.service;

import vn.icktmeanz.trafficViolation.dto.response.AIProcessingResultDTO;

import java.io.File;
import java.util.List;

public interface AIService {
    
    /**
     * Process image file using AI model service (Flask)
     * @param imagePath absolute path to image file
     * @return AIProcessingResultDTO containing detected violations and processed image URL
     */
    AIProcessingResultDTO processImage(String imagePath);

    /**
     * Process multiple images (folder) using AI model service
     * @param imageFiles array of image files
     * @return List<AIProcessingResultDTO> containing detected violations for each image
     */
    List<AIProcessingResultDTO> processFolder(File[] imageFiles);

    /**
     * Process video file using AI model service
     * @param videoFile video file to process
     * @return AIProcessingResultDTO containing detected violations from first detected frame
     */
    AIProcessingResultDTO processVideo(File videoFile);
}
