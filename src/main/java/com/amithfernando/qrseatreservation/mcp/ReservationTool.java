package com.amithfernando.qrseatreservation.mcp;

import com.amithfernando.qrseatreservation.core.dto.ReservationResponse;
import com.amithfernando.qrseatreservation.core.dto.SellerDetailResponse;
import com.amithfernando.qrseatreservation.core.dto.TicketResponse;
import com.amithfernando.qrseatreservation.core.enums.TicketType;
import com.amithfernando.qrseatreservation.core.model.ReservationDetail;
import com.amithfernando.qrseatreservation.core.model.SeatDetail;
import com.amithfernando.qrseatreservation.core.model.SeatReservation;
import com.amithfernando.qrseatreservation.core.model.SellerDetail;
import com.amithfernando.qrseatreservation.core.model.TableDetail;
import com.amithfernando.qrseatreservation.core.service.ReservationService;
import com.amithfernando.qrseatreservation.core.service.SellerDetailService;
import com.amithfernando.qrseatreservation.core.service.TableDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class ReservationTool {

    private final ReservationService reservationService;
    private final SellerDetailService sellerDetailService;
    private final TableDetailService tableDetailService;

    public ReservationTool(ReservationService reservationService, SellerDetailService sellerDetailService, TableDetailService tableDetailService) {
        this.reservationService = reservationService;
        this.sellerDetailService = sellerDetailService;
        this.tableDetailService = tableDetailService;
    }

    @Tool(name = "addReservation",
            description = "Add seat reservation . Specify seller name, table name, list of selected seat numbers, number of full tickets and half tickets.")
    public ReservationResponse createReservation(String sellerName, String tableName, List<String> seatNumbers, int numberOfFullTickets, int numberOfHalfTickets) {
        SellerDetail sellerDetail =sellerDetailService.findBySellerName(sellerName);
        TableDetail tableDetail=tableDetailService.findByTableName(tableName);
        List<SeatDetail> seatDetails=new ArrayList<>();
        for(String s:seatNumbers){
            SeatDetail seatDetail=tableDetailService.findSeatDetailByTableAndSeatNumber(tableDetail,s);
            seatDetails.add(seatDetail);
        }

        var it = seatDetails.iterator();
        Set<SeatReservation> seatReservations = new HashSet<>();
        for (int i = 0; i < numberOfFullTickets && it.hasNext(); i++) {
            SeatDetail sd = it.next();
            seatReservations.add(SeatReservation.builder()
                    .seatDetail(sd)
                    .ticketType(TicketType.FULL)
                    .build());
        }
        for (int i = 0; i < numberOfHalfTickets && it.hasNext(); i++) {
            SeatDetail sd = it.next();
            seatReservations.add(SeatReservation.builder()
                    .seatDetail(sd)
                    .ticketType(TicketType.HALF)
                    .build());
        }

        ReservationDetail reservation = ReservationDetail.builder()
                .sellerDetail(sellerDetail)
                .seatReservations(seatReservations)
                .description("Reservation created by chat")
                .build();

        ReservationDetail reservationDetail = reservationService.saveReservation(reservation);
        List<TicketResponse> ticketResponses = reservationDetail.getSeatReservations().stream()
                .map(table -> new TicketResponse(
                        table.getTicketNo(),
                        table.getSeatDetail().getSeatNo()
                ))
                .toList();

        ReservationResponse reservationResponse = ReservationResponse.builder()
                .seller(sellerName)
                .tableNumber(tableName)
                .tickets(ticketResponses)
                .build();
return reservationResponse;

    }


}
