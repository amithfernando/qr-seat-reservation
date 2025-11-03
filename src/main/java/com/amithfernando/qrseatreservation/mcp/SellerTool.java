package com.amithfernando.qrseatreservation.mcp;

import com.amithfernando.qrseatreservation.core.dto.SellerDetailResponse;
import com.amithfernando.qrseatreservation.core.dto.TableDetailResponse;
import com.amithfernando.qrseatreservation.core.dto.TableDetailSummary;
import com.amithfernando.qrseatreservation.core.service.SellerDetailService;
import com.amithfernando.qrseatreservation.core.service.TableDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SellerTool {

    private final SellerDetailService sellerDetailService;

    public SellerTool(SellerDetailService sellerDetailService) {
        this.sellerDetailService = sellerDetailService;
    }

    @Tool(name = "addSeller",
            description = "Add a seller to reservation system. Specify seller name, address, email, phoneNumber and description.")
    public String createSeller(String name, String address, String email, String phoneNumber, String description) {
        sellerDetailService.createSeller(name, address, email, phoneNumber, description);
        return "Seller created: " + name;
    }


    @Tool(name = "getAllSellers",
            description = "Get all sellers of the reservation system. It will return seller details of the system")
    public List<SellerDetailResponse> getAllSellers() {
        return sellerDetailService.getAllSellers().stream()
                .map(table -> new SellerDetailResponse(
                        table.getName(),
                        table.getAddress(),
                        table.getEmail(),
                        table.getPhone(),
                        table.getDescription()
                ))
                .toList();
    }
}
