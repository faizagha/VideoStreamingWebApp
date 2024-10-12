package com.faiz.videostreaming.service;

import com.faiz.videostreaming.entities.video;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface videoService {
    video saveVideo(video video, MultipartFile file);
    List<video> getAllVideos();
    Optional<video> getVideoById(String id);
    void deleteVideoById(String id);
    void updateVideo(String id, video video);
}
