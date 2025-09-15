package com.amithfernando.qrseatreservation.core.service;

import com.amithfernando.qrseatreservation.core.model.SellerDetail;
import com.amithfernando.qrseatreservation.core.repsitory.SellerDetailRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SellerDetailService {

    private final SellerDetailRepository sellerDetailRepository;

    public SellerDetailService(SellerDetailRepository sellerDetailRepository) {
        this.sellerDetailRepository = sellerDetailRepository;
    }

    public void saveSeller(SellerDetail sellerDetail) {
        sellerDetailRepository.save(sellerDetail);
        log.info("Seller details created: {}", sellerDetail);
    }

    public List<SellerDetail> getAllSellers() {
        return sellerDetailRepository.findAll();
    }

    @Transactional
    public void createSeller(String name, String address, String email, String phoneNumber, String description) {
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

    public void delete(SellerDetail item) {
        sellerDetailRepository.delete(item);
        log.info("Seller deleted: {}", item);
    }
}
