package com.amithfernando.qrseatreservation.api.service;

import com.amithfernando.qrseatreservation.api.dto.CreateTableRequest;
import com.amithfernando.qrseatreservation.api.dto.TableDetailSummary;
import com.amithfernando.qrseatreservation.api.enums.SeatStatus;
import com.amithfernando.qrseatreservation.api.exception.TableAlreadyExistsException;
import com.amithfernando.qrseatreservation.api.exception.TableHasReservationsException;
import com.amithfernando.qrseatreservation.api.exception.TableNotFoundException;
import com.amithfernando.qrseatreservation.api.model.SeatDetail;
import com.amithfernando.qrseatreservation.api.model.TableDetail;
import com.amithfernando.qrseatreservation.api.repsitory.ReservationDetailRepository;
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
    private final ReservationDetailRepository reservationDetailRepository;

    public TableDetailService(TableDetailRepository tableDetailRepository, 
                            SeatDetailRepository seatDetailRepository,
                            ReservationDetailRepository reservationDetailRepository) {
        this.tableDetailRepository = tableDetailRepository;
        this.seatDetailRepository = seatDetailRepository;
        this.reservationDetailRepository = reservationDetailRepository;
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

    @Transactional
    public void delete(Long id) {
        TableDetail tableDetail = tableDetailRepository.findById(id)
                .orElseThrow(() -> new TableNotFoundException(String.valueOf(id)));
        
        // Check if table has any reserved or checked-in seats
        if (hasActiveReservations(tableDetail)) {
            log.warn("Attempted to delete table {} with active reservations", tableDetail.getTableName());
            throw new TableHasReservationsException("delete", tableDetail.getTableName());
        }
        
        tableDetailRepository.delete(tableDetail);
        log.info("Table deleted: {}", tableDetail.getTableName());
    }

    public TableDetail getTableById(Long id) {
        return tableDetailRepository.findById(id)
                .orElseThrow(() -> new TableNotFoundException(String.valueOf(id)));
    }

    public TableDetail getTableByName(String name) {
        return tableDetailRepository.findByTableName(name)
                .orElseThrow(() -> new TableNotFoundException(name));
    }

    @Transactional
    public TableDetail updateTable(Long id, CreateTableRequest request) {
        TableDetail existingTable = tableDetailRepository.findById(id)
                .orElseThrow(() -> new TableNotFoundException(String.valueOf(id)));
        
        // Check if table has any reserved or checked-in seats
        if (hasActiveReservations(existingTable)) {
            log.warn("Attempted to update table {} with active reservations", existingTable.getTableName());
            throw new TableHasReservationsException("update", existingTable.getTableName());
        }
        
        // Check if new name already exists (if name is being changed)
        if (!existingTable.getTableName().equals(request.getTableName())) {
            TableDetail duplicateTable = findByTableName(request.getTableName());
            if (duplicateTable != null) {
                throw new TableAlreadyExistsException(request.getTableName());
            }
        }
        
        // Update basic table info
        existingTable.setTableName(request.getTableName());
        existingTable.setDescription(request.getDescription());
        existingTable.setNoOfSeats(request.getNoOfAvailableSeats() + request.getNoOfUnavailableSeats());
        
        // Delete old seats
        seatDetailRepository.deleteAll(existingTable.getSeatDetails());
        
        // Create new seats
        List<SeatDetail> newSeatDetails = new ArrayList<>();
        int seatNo = 1;
        
        for (int i = 0; i < request.getNoOfAvailableSeats(); i++) {
            SeatDetail seatDetail = SeatDetail.builder()
                    .seatNo("S" + seatNo)
                    .seatStatus(SeatStatus.AVAILABLE)
                    .tableDetail(existingTable)
                    .build();
            seatDetailRepository.save(seatDetail);
            newSeatDetails.add(seatDetail);
            seatNo++;
        }
        
        for (int i = 0; i < request.getNoOfUnavailableSeats(); i++) {
            SeatDetail seatDetail = SeatDetail.builder()
                    .seatNo("S" + seatNo)
                    .seatStatus(SeatStatus.UNAVAILABLE)
                    .tableDetail(existingTable)
                    .build();
            seatDetailRepository.save(seatDetail);
            newSeatDetails.add(seatDetail);
            seatNo++;
        }
        
        existingTable.setSeatDetails(newSeatDetails);
        TableDetail updatedTable = tableDetailRepository.save(existingTable);
        log.info("Table updated: {}", updatedTable.getTableName());
        
        return updatedTable;
    }

    /**
     * Check if a table has any active reservations (reserved or checked-in seats)
     * @param tableDetail the table to check
     * @return true if the table has active reservations, false otherwise
     */
    private boolean hasActiveReservations(TableDetail tableDetail) {
        // Check if any seat is reserved or checked-in
        boolean hasReservedSeats = tableDetail.getSeatDetails().stream()
                .anyMatch(seat -> seat.isReserved() || seat.isCheckedIn());
        
        if (hasReservedSeats) {
            log.debug("Table {} has reserved/checked-in seats", tableDetail.getTableName());
            return true;
        }
        
        // Double-check with reservation repository
        List<?> reservations = reservationDetailRepository.findByTableDetailId(tableDetail.getId());
        boolean hasReservationRecords = !reservations.isEmpty();
        
        if (hasReservationRecords) {
            log.debug("Table {} has reservation records in database", tableDetail.getTableName());
        }
        
        return hasReservationRecords;
    }

    /**
     * Check if a specific table can be modified (has no active reservations)
     * @param tableId the table ID to check
     * @return true if the table can be modified, false otherwise
     */
    public boolean canModifyTable(Long tableId) {
        TableDetail tableDetail = getTableById(tableId);
        return !hasActiveReservations(tableDetail);
    }
}
