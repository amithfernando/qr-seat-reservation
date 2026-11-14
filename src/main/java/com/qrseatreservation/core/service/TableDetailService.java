package com.qrseatreservation.core.service;

import com.qrseatreservation.core.event.EventAccessException;
import com.qrseatreservation.core.event.EventContext;

import com.qrseatreservation.core.enums.SeatStatus;
import com.qrseatreservation.core.model.SeatDetail;
import com.qrseatreservation.core.model.TableDetail;
import com.qrseatreservation.core.repository.SeatDetailRepository;
import com.qrseatreservation.core.repository.TableDetailRepository;
import com.qrseatreservation.ui.dto.TableDetailSummary;
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

    private final EventContext eventContext;

    public TableDetailService(TableDetailRepository tableDetailRepository, SeatDetailRepository seatDetailRepository,
                              EventContext eventContext) {
        this.eventContext = eventContext;
        this.tableDetailRepository = tableDetailRepository;
        this.seatDetailRepository = seatDetailRepository;
    }

    @Transactional
    public void createTable(TableDetail tableDetail) {
        tableDetail.setEvent(eventContext.requireEditable());
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
                .event(eventContext.requireEditable())
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
        return tableDetailRepository.findByEventIdOrderByIdAsc(eventContext.currentId());
    }

    public TableDetailSummary getGetTableSummary() {
        List<TableDetail> all = tableDetailRepository.findByEventIdOrderByIdAsc(eventContext.currentId());
        TableDetailSummary tableDetailSummary = new TableDetailSummary();
        tableDetailSummary.setTotalNoOfTables(all.size());
        tableDetailSummary.setTotalNoOfTotalSeats(all.stream().mapToInt(TableDetail::getNoOfSeats).sum());
        return tableDetailSummary;
    }

    @Transactional
    public void delete(TableDetail item) {
        Long eventId = eventContext.requireEditable().getId();
        TableDetail managed = tableDetailRepository.findByIdAndEventId(item.getId(), eventId)
                .orElseThrow(() -> new EventAccessException("Table does not belong to the current event"));
        tableDetailRepository.delete(managed);
    }

    @Transactional
    public void updateTable(TableDetail existing, String newName, int availableSeats, int unavailableSeats, String description) {
        EventContext.assertBelongsTo(existing.getEvent(), eventContext.requireEditable().getId());
        existing.setTableName(newName);
        existing.setDescription(description);
        tableDetailRepository.save(existing);
    }

    @Transactional
    public void updatePosition(Long id, int x, int y) {
        tableDetailRepository.findByIdAndEventId(id, eventContext.requireEditable().getId()).ifPresent(t -> {
            t.setPosX(x);
            t.setPosY(y);
            tableDetailRepository.save(t);
        });
    }
}
