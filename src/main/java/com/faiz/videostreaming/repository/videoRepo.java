package com.faiz.videostreaming.repository;


import com.faiz.videostreaming.entities.video;
import org.springframework.data.jpa.repository.JpaRepository;

public interface videoRepo extends JpaRepository<video, String> {
}
