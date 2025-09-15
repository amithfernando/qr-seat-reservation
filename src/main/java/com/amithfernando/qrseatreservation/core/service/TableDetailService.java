package com.amithfernando.qrseatreservation.core.service;

import com.amithfernando.qrseatreservation.core.enums.SeatStatus;
import com.amithfernando.qrseatreservation.core.model.SeatDetail;
import com.amithfernando.qrseatreservation.core.model.TableDetail;
import com.amithfernando.qrseatreservation.core.repsitory.SeatDetailRepository;
import com.amithfernando.qrseatreservation.core.repsitory.TableDetailRepository;
import com.amithfernando.qrseatreservation.ui.dto.TableDetailSummary;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class TableDetailService {

    private final TableDetailRepository tableDetailRepository;
    private final SeatDetailRepository seatDetailRepository;

    public TableDetailService(TableDetailRepository tableDetailRepository, SeatDetailRepository seatDetailRepository) {
        this.tableDetailRepository = tableDetailRepository;
        this.seatDetailRepository = seatDetailRepository;
    }

    @Transactional
    public void createTable(TableDetail tableDetail) {
        //save seat details
        List<SeatDetail> seatDetails = new ArrayList<>();
        for(int i = 0; i < tableDetail.getNoOfSeats(); i++) {
            SeatDetail seatDetail = SeatDetail.builder()
                    .seatNo("S" + (i + 1))
                    .seatStatus(SeatStatus.AVAILABLE)
                    .tableDetail(tableDetail)
                    .build();
            seatDetailRepository.save(seatDetail);
            seatDetails.add(seatDetail);
        }
        tableDetail.setSeatDetails(seatDetails);
        tableDetailRepository.save(tableDetail);
        log.info("ReservationTable created: {}", tableDetail);

    }

    @Transactional
    public void createTable(String tableName, int noOfAvailableSeats,int noOfUnAvailableSeats, String description) {
        TableDetail tableDetail = TableDetail.builder()
                .tableName(tableName)
                .noOfSeats(noOfAvailableSeats+noOfUnAvailableSeats)
                .description(description)
                .build();
        //save seat details
        List<SeatDetail> seatDetails = new ArrayList<>();
        int seatNo = 1;
        for(int i = 0; i < noOfAvailableSeats; i++) { //Available
            SeatDetail seatDetail = SeatDetail.builder()
                    .seatNo("S" + seatNo)
                    .seatStatus(SeatStatus.AVAILABLE)
                    .tableDetail(tableDetail)
                    .build();
            seatDetailRepository.save(seatDetail);
            seatDetails.add(seatDetail);
            seatNo++;
        }
        for(int i = 0; i < noOfUnAvailableSeats; i++) { //UnAvailable
            SeatDetail seatDetail = SeatDetail.builder()
                    .seatNo("S" + seatNo)
                    .seatStatus(SeatStatus.UNAVAILABLE)
                    .tableDetail(tableDetail)
                    .build();
            seatDetailRepository.save(seatDetail);
            seatDetails.add(seatDetail);
            seatNo++;
        }
        tableDetail.setSeatDetails(seatDetails);
        tableDetailRepository.save(tableDetail);
        log.info("Table detail created: {}", tableDetail);

    }

    @Transactional
    public List<TableDetail> getAllTables() {
        return tableDetailRepository.findAll();
    }

    public TableDetailSummary getGetTableSummary() {
        List<TableDetail> all = tableDetailRepository.findAll();
        TableDetailSummary tableDetailSummary = new TableDetailSummary();
        tableDetailSummary.setTotalNoOfTables(all.size());
        tableDetailSummary.setTotalNoOfTotalSeats(all.stream().mapToInt(TableDetail::getNoOfSeats).sum());
        return tableDetailSummary;
    }

    public void delete(TableDetail item) {
        tableDetailRepository.delete(item);
    }
}
