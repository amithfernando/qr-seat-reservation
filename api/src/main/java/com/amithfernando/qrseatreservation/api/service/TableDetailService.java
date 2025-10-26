package com.amithfernando.qrseatreservation.api.service;

import com.amithfernando.qrseatreservation.api.dto.TableDetailSummary;
import com.amithfernando.qrseatreservation.api.enums.SeatStatus;
import com.amithfernando.qrseatreservation.api.exception.TableAlreadyExistsException;
import com.amithfernando.qrseatreservation.api.exception.TableNotFoundException;
import com.amithfernando.qrseatreservation.api.model.SeatDetail;
import com.amithfernando.qrseatreservation.api.model.TableDetail;
import com.amithfernando.qrseatreservation.api.repsitory.SeatDetailRepository;
import com.amithfernando.qrseatreservation.api.repsitory.TableDetailRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public void createTable(String tableName, int noOfAvailableSeats,int noOfUnAvailableSeats, String description) {
        if(null!=findByTableName(tableName)){//check existing table
            log.debug("Table already exists: {}", tableName);
            throw new TableAlreadyExistsException(tableName);
        }

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

    private TableDetail findByTableName(String tableName) {
        Optional<TableDetail> tableDetail=tableDetailRepository.findByTableName(tableName);
        return tableDetail.orElse(null);
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

    public void delete(Long id) {
        TableDetail tableDetail = tableDetailRepository.findById(id)
                .orElseThrow(() -> new TableNotFoundException(String.valueOf(id)));
        tableDetailRepository.delete(tableDetail);
        log.info("Table deleted: {}", tableDetail.getTableName());
    }
}
