package com.amithfernando.qrseatreservation.api.controller.impl;

import com.amithfernando.qrseatreservation.api.controller.SellerController;
import com.amithfernando.qrseatreservation.api.model.SellerDetail;
import com.amithfernando.qrseatreservation.api.service.SellerDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
public class SellerControllerImpl implements SellerController {

    private final SellerDetailService sellerDetailService;

    public SellerControllerImpl(SellerDetailService sellerDetailService) {
        this.sellerDetailService = sellerDetailService;
    }

    public ResponseEntity<List<SellerDetail>> getAllSellers() {
        log.info("Fetching all sellers");
        List<SellerDetail> sellers = sellerDetailService.getAllSellers();
        return ResponseEntity.ok(sellers);
    }


    public ResponseEntity<Void> createSeller(@RequestBody SellerDetail sellerDetail) {
        log.info("Creating new seller: {}", sellerDetail.getName());
        sellerDetailService.saveSeller(sellerDetail);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    public ResponseEntity<Void> createCustomSeller(@RequestBody CreateSellerRequest request) {
        log.info("Creating custom seller: {}", request.getName());
        sellerDetailService.createSeller(
                request.getName(),
                request.getAddress(),
                request.getEmail(),
                request.getPhoneNumber(),
                request.getDescription()
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    public ResponseEntity<Void> deleteSeller(@RequestBody SellerDetail sellerDetail) {
        log.info("Deleting seller: {}", sellerDetail.getName());
        sellerDetailService.delete(sellerDetail);
        return ResponseEntity.noContent().build();
    }


}
