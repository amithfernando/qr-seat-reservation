package com.qrseatreservation.core.repository;

import com.qrseatreservation.core.model.Setting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettingRepository extends JpaRepository<Setting, Long> {
    Optional<Setting> findByEventId(Long eventId);
}
