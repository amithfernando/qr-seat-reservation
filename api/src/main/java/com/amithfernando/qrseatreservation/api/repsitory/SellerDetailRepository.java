package com.amithfernando.qrseatreservation.api.repsitory;


import com.amithfernando.qrseatreservation.api.model.SellerDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerDetailRepository extends JpaRepository<SellerDetail, Long> {
    Optional<SellerDetail> findByName(String name);
}
