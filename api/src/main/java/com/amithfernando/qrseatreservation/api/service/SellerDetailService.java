package com.amithfernando.qrseatreservation.api.service;

import com.amithfernando.qrseatreservation.api.dto.CreateSellerRequest;
import com.amithfernando.qrseatreservation.api.exception.SellerAlreadyExistsException;
import com.amithfernando.qrseatreservation.api.exception.SellerHasReservationsException;
import com.amithfernando.qrseatreservation.api.exception.SellerNotFoundException;
import com.amithfernando.qrseatreservation.api.model.ReservationDetail;
import com.amithfernando.qrseatreservation.api.model.SellerDetail;
import com.amithfernando.qrseatreservation.api.repsitory.ReservationDetailRepository;
import com.amithfernando.qrseatreservation.api.repsitory.SellerDetailRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class SellerDetailService {

    private final SellerDetailRepository sellerDetailRepository;
    private final ReservationDetailRepository reservationDetailRepository;

    public SellerDetailService(SellerDetailRepository sellerDetailRepository,
                              ReservationDetailRepository reservationDetailRepository) {
        this.sellerDetailRepository = sellerDetailRepository;
        this.reservationDetailRepository = reservationDetailRepository;
    }

    private SellerDetail findBySellerName(String sellerName) {
        Optional<SellerDetail> sellerDetail = sellerDetailRepository.findByName(sellerName);
        return sellerDetail.orElse(null);
    }


    public List<SellerDetail> getAllSellers() {
        return sellerDetailRepository.findAll();
    }

    public SellerDetail getSellerById(Long id) {
        return sellerDetailRepository.findById(id)
                .orElseThrow(() -> new SellerNotFoundException(String.valueOf(id)));
    }

    public SellerDetail getSellerByName(String name) {
        SellerDetail seller = findBySellerName(name);
        if (seller == null) {
            throw new SellerNotFoundException(name);
        }
        return seller;
    }

    @Transactional
    public void createSeller(String name, String address, String email, String phoneNumber, String description) {
        if (findBySellerName(name) != null) {
            throw new SellerAlreadyExistsException(name);
        }

        SellerDetail sellerDetail = SellerDetail.builder()
                .name(name)
                .address(address)
                .email(email)
                .phone(phoneNumber)
                .description(description)
                .build();
        sellerDetailRepository.save(sellerDetail);
        log.info("Seller details created: {}", sellerDetail);
    }

    @Transactional
    public SellerDetail updateSeller(Long id, CreateSellerRequest request) {
        SellerDetail existingSeller = sellerDetailRepository.findById(id)
                .orElseThrow(() -> new SellerNotFoundException(String.valueOf(id)));

        // Check if seller has any active reservations
        if (hasActiveReservations(existingSeller)) {
            log.warn("Attempted to update seller {} with active reservations", existingSeller.getName());
            throw new SellerHasReservationsException("update", existingSeller.getName());
        }

        // Check if new name already exists (if name is being changed)
        if (!existingSeller.getName().equals(request.getName())) {
            SellerDetail duplicateSeller = findBySellerName(request.getName());
            if (duplicateSeller != null) {
                throw new SellerAlreadyExistsException(request.getName());
            }
        }

        // Update seller details
        existingSeller.setName(request.getName());
        existingSeller.setAddress(request.getAddress());
        existingSeller.setEmail(request.getEmail());
        existingSeller.setPhone(request.getPhoneNumber());
        existingSeller.setDescription(request.getDescription());

        SellerDetail updatedSeller = sellerDetailRepository.save(existingSeller);
        log.info("Seller updated: {}", updatedSeller.getName());

        return updatedSeller;
    }

    @Transactional
    public void delete(Long id) {
        SellerDetail sellerDetail = sellerDetailRepository.findById(id)
                .orElseThrow(() -> new SellerNotFoundException(String.valueOf(id)));

        // Check if seller has any active reservations
        if (hasActiveReservations(sellerDetail)) {
            log.warn("Attempted to delete seller {} with active reservations", sellerDetail.getName());
            throw new SellerHasReservationsException("delete", sellerDetail.getName());
        }

        sellerDetailRepository.delete(sellerDetail);
        log.info("Seller deleted: {}", sellerDetail);
    }

    /**
     * Check if a seller has any active reservations
     * @param sellerDetail the seller to check
     * @return true if the seller has active reservations, false otherwise
     */
    private boolean hasActiveReservations(SellerDetail sellerDetail) {
        List<ReservationDetail> reservations = reservationDetailRepository.findBySellerDetailId(sellerDetail.getId());
        boolean hasReservations = !reservations.isEmpty();
        
        if (hasReservations) {
            log.debug("Seller {} has {} reservation(s)", sellerDetail.getName(), reservations.size());
        }
        
        return hasReservations;
    }

    /**
     * Check if a specific seller can be modified (has no active reservations)
     * @param sellerId the seller ID to check
     * @return true if the seller can be modified, false otherwise
     */
    public boolean canModifySeller(Long sellerId) {
        SellerDetail sellerDetail = getSellerById(sellerId);
        return !hasActiveReservations(sellerDetail);
    }
}
