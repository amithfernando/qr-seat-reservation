package com.qrseatreservation.core.repository;

import com.qrseatreservation.core.model.SellerDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SellerDetailRepository extends JpaRepository<SellerDetail, Long> {
    /** Access tokens are random UUIDs, unique across all events; the seller's own event is checked by the caller. */
    Optional<SellerDetail> findByAccessToken(String accessToken);

    List<SellerDetail> findByEventIdOrderByIdAsc(Long eventId);

    Optional<SellerDetail> findByIdAndEventId(Long id, Long eventId);
}
