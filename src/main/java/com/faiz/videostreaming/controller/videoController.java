package com.faiz.videostreaming.controller;

import com.faiz.videostreaming.entities.video;
import com.faiz.videostreaming.response.apiResponse;
import com.faiz.videostreaming.service.videoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/video")
public class videoController {
    private final videoService videoService;

    // Upload a video
    // http://localhost:9091/api/v1/video/upload
    @PostMapping("/upload")
    public ResponseEntity<apiResponse<video>> uploadVideo(@RequestParam MultipartFile file,
                                                          @RequestParam String videoName,
                                                          @RequestParam String videoDescription) {
        video video = new video();
        video.setTitle(videoName);
        video.setDescription(videoDescription);
        video.setVideoId(UUID.randomUUID().toString());
        video.setContentType(file.getContentType());

        video savedVideo = videoService.saveVideo(video, file);

        if (savedVideo != null) {
            return ResponseEntity.ok(
                    apiResponse.success("Video uploaded successfully", savedVideo, HttpStatus.OK.value())
            );
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    apiResponse.error("Video upload failed", HttpStatus.INTERNAL_SERVER_ERROR.value())
            );
        }
    }

    // Get all videos
    // http://localhost:9091/api/v1/video/all
    @GetMapping("/all")
    public ResponseEntity<apiResponse<List<video>>> getVideos() {
        try {
            List<video> videoList = videoService.getAllVideos();
            return ResponseEntity.ok(
                    apiResponse.success("All videos retrieved successfully", videoList, HttpStatus.OK.value())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    apiResponse.error("Failed to retrieve videos", HttpStatus.INTERNAL_SERVER_ERROR.value())
            );
        }
    }

    // Get and stream a video
    // http://localhost:9091/api/v1/video/stream/{videoId}
    @GetMapping("/stream/{videoId}")
    public ResponseEntity<Resource> streamVideo(@PathVariable String videoId,
                                                @RequestHeader(value = "Range", required = false) String rangeHeader) {
        Optional<video> videoOptional = videoService.getVideoById(videoId);
        if (videoOptional.isEmpty()) {
            return ResponseEntity.status(NOT_FOUND).build();
        }

        video video = videoOptional.get();
        String contentType = video.getContentType();
        String filePath = video.getFilePath();
        Path path = Paths.get(filePath);
        long fileLength;

        try {
            fileLength = Files.size(path);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        try {
            if (rangeHeader == null) {
                // Full video response
                Resource resource = new FileSystemResource(filePath);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .contentLength(fileLength)
                        .header(HttpHeaders.ACCEPT_RANGES, "bytes") // Include Accept-Ranges header
                        .body(resource);
            } else {
                // Handle partial content (byte-range requests)
                String[] ranges = rangeHeader.replace("bytes=", "").split("-");
                long rangeStart = Long.parseLong(ranges[0]);
                long rangeEnd = (ranges.length > 1) ? Long.parseLong(ranges[1]) : fileLength - 1;

                if (rangeEnd >= fileLength) {
                    rangeEnd = fileLength - 1;
                }

                long contentLength = rangeEnd - rangeStart + 1;
                String contentRange = "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength;

                try (InputStream inputStream = Files.newInputStream(path)) {
                    inputStream.skip(rangeStart);
                    byte[] buffer = inputStream.readNBytes((int) contentLength);

                    HttpHeaders headers = new HttpHeaders();
                    headers.add(HttpHeaders.CONTENT_RANGE, contentRange);
                    headers.add(HttpHeaders.ACCEPT_RANGES, "bytes");

                    return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                            .headers(headers)
                            .contentLength(contentLength)
                            .contentType(MediaType.parseMediaType(contentType))
                            .body(new ByteArrayResource(buffer));
                }
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/delete/{videoId}")
    public ResponseEntity<apiResponse<String>> deleteVideo(@PathVariable String videoId) {
        try {
            // Attempt to delete the video
            videoService.deleteVideoById(videoId);
            return ResponseEntity.ok(
                    apiResponse.success("Video deleted successfully", null, HttpStatus.OK.value())
            );
        } catch (Exception e) {
            // Log the exception if you have a logging framework in place
            // logger.error("Failed to delete videoId: " + videoId, e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    apiResponse.error("Failed to delete video", HttpStatus.INTERNAL_SERVER_ERROR.value())
            );
        }
    }

    @PutMapping("/update/{videoId}")
    public ResponseEntity<apiResponse<video>> updateVideo(@PathVariable String videoId,
                                                            @RequestBody video updatedVideo) {
            try {
                videoService.updateVideo(videoId, updatedVideo);
                return ResponseEntity.ok(
                        apiResponse.success("Video updated successfully", updatedVideo, HttpStatus.OK.value())
                );
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        apiResponse.error("Failed to update video", HttpStatus.INTERNAL_SERVER_ERROR.value())
                );
            }
        }
}
