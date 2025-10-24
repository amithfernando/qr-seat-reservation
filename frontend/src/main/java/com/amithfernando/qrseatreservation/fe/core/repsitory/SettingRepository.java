package com.amithfernando.qrseatreservation.fe.core.repsitory;

import com.amithfernando.qrseatreservation.fe.core.model.Setting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingRepository extends JpaRepository<Setting, Long> {

}
