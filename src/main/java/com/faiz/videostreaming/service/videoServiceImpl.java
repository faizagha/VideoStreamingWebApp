package com.faiz.videostreaming.service;

import com.faiz.videostreaming.entities.video;
import com.faiz.videostreaming.repository.videoRepo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class videoServiceImpl implements videoService {

    private static final Logger logger = LoggerFactory.getLogger(videoServiceImpl.class);

    private final videoRepo videoRepo;

    @Value("${file.video}")
    private String DIR;

    @PostConstruct
    public void init() {
        try {
            Path path = Paths.get(DIR).toAbsolutePath().normalize();
            if (!Files.exists(path)) {
                Files.createDirectory(path);
                logger.info("Directory created: {}", DIR);
            } else {
                logger.info("Directory already exists: {}", DIR);
            }
        } catch (IOException e) {
            logger.error("Error initializing video directory", e);
        }
    }

    @Override
    @Transactional
    public video saveVideo(video video, MultipartFile file) {
        try {
            String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            Path targetLocation = Paths.get(DIR).toAbsolutePath().normalize().resolve(fileName);
            logger.info("Saving video to path: {}", targetLocation);

            // Copy file to the target location (replace existing file if it exists)
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            // Set video attributes
            video.setContentType(file.getContentType());
            video.setFilePath(targetLocation.toString());

            // Save video metadata to the database
            return videoRepo.save(video);
        } catch (IOException e) {
            logger.error("Error saving video file", e);
            throw new RuntimeException("Could not store the file. Please try again!", e);
        }
    }

    @Override
    public List<video> getAllVideos() {
        return videoRepo.findAll();
    }

    @Override
    public Optional<video> getVideoById(String id) {
        return videoRepo.findById(id);
    }

    @Override
    @Transactional
    public void deleteVideoById(String id) {
        videoRepo.deleteById(id);
    }

    @Override
    @Transactional
    public void updateVideo(String id, video updatedVideo) {
        videoRepo.findById(id).ifPresent(video -> {
            video.setTitle(updatedVideo.getTitle());
            video.setDescription(updatedVideo.getDescription());
            videoRepo.save(video);
        });
    }
}
