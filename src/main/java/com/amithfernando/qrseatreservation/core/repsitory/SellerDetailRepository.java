package com.amithfernando.qrseatreservation.core.repsitory;

import com.amithfernando.qrseatreservation.core.model.SellerDetail;
import com.amithfernando.qrseatreservation.core.model.TableDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerDetailRepository extends JpaRepository<SellerDetail, Long> {
}
