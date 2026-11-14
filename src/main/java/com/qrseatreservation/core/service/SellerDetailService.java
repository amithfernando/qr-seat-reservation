package com.qrseatreservation.core.service;

import com.qrseatreservation.core.event.EventAccessException;
import com.qrseatreservation.core.event.EventContext;
import com.qrseatreservation.core.model.Event;

import com.qrseatreservation.core.model.SellerDetail;
import com.qrseatreservation.core.repository.SellerDetailRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class SellerDetailService {

    private final SellerDetailRepository sellerDetailRepository;
    private final PasswordEncoder passwordEncoder;

    private final EventContext eventContext;

    public SellerDetailService(SellerDetailRepository sellerDetailRepository,
                               PasswordEncoder passwordEncoder,
                               EventContext eventContext) {
        this.eventContext = eventContext;
        this.sellerDetailRepository = sellerDetailRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void saveSeller(SellerDetail sellerDetail) {
        Event event = eventContext.requireEditable();
        if (sellerDetail.getId() == null) {
            sellerDetail.setEvent(event);
        } else {
            EventContext.assertBelongsTo(sellerDetail.getEvent(), event.getId());
        }
        sellerDetailRepository.save(sellerDetail);
        log.info("Seller details created: {}", sellerDetail);
    }

    public List<SellerDetail> getAllSellers() {
        return sellerDetailRepository.findByEventIdOrderByIdAsc(eventContext.currentId());
    }

    @Transactional
    public void createSeller(String name, String address, String email, String phoneNumber, String description) {
        SellerDetail sellerDetail = SellerDetail.builder()
                .name(name)
                .address(address)
                .email(email)
                .phone(phoneNumber)
                .description(description)
                .event(eventContext.requireEditable())
                .build();
        sellerDetailRepository.save(sellerDetail);
        log.info("Seller details created: {}", sellerDetail);
    }

    public void delete(SellerDetail item) {
        SellerDetail managed = sellerDetailRepository.findByIdAndEventId(item.getId(), eventContext.requireEditable().getId())
                .orElseThrow(() -> new EventAccessException("Seller does not belong to the current event"));
        sellerDetailRepository.delete(managed);
        log.info("Seller deleted: {}", item);
    }

    @Transactional
    public void updateSeller(SellerDetail existing, String name, String address, String email, String phone, String description) {
        EventContext.assertBelongsTo(existing.getEvent(), eventContext.requireEditable().getId());
        existing.setName(name);
        existing.setAddress(address);
        existing.setEmail(email);
        existing.setPhone(phone);
        existing.setDescription(description);
        sellerDetailRepository.save(existing);
    }

    /** Sets a new portal password and (re)generates the access token; returns the new token. */
    @Transactional
    public String setPortalPassword(SellerDetail seller, String rawPassword) {
        EventContext.assertBelongsTo(seller.getEvent(), eventContext.requireEditable().getId());
        if (seller.getAccessToken() == null) {
            seller.setAccessToken(UUID.randomUUID().toString());
        }
        seller.setPasswordHash(passwordEncoder.encode(rawPassword));
        sellerDetailRepository.save(seller);
        log.info("Portal password set for seller id={}", seller.getId());
        return seller.getAccessToken();
    }

    /** Removes portal access from a seller. */
    @Transactional
    public void revokePortalAccess(SellerDetail seller) {
        EventContext.assertBelongsTo(seller.getEvent(), eventContext.requireEditable().getId());
        seller.setAccessToken(null);
        seller.setPasswordHash(null);
        sellerDetailRepository.save(seller);
    }

    /**
     * Seller-portal lookup. Portal links only work while the seller's event is not archived,
     * so links of past events stop working automatically.
     */
    public Optional<SellerDetail> findByAccessToken(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        return sellerDetailRepository.findByAccessToken(token)
                .filter(s -> s.getEvent() != null && !s.getEvent().isArchived());
    }

    public boolean verifyPassword(SellerDetail seller, String rawPassword) {
        return seller.getPasswordHash() != null
                && passwordEncoder.matches(rawPassword, seller.getPasswordHash());
    }
}
