package com.qrseatreservation.core.service;

import com.qrseatreservation.core.enums.EventStatus;
import com.qrseatreservation.core.enums.SeatStatus;
import com.qrseatreservation.core.event.EventAccessException;
import com.qrseatreservation.core.event.EventContext;
import com.qrseatreservation.core.model.Event;
import com.qrseatreservation.core.model.SeatDetail;
import com.qrseatreservation.core.model.TableDetail;
import com.qrseatreservation.core.repository.SeatDetailRepository;
import com.qrseatreservation.core.repository.TableDetailRepository;
import com.qrseatreservation.ui.dto.TableDetailSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TableDetailServiceTest {

    @Mock
    private TableDetailRepository tableRepo;

    @Mock
    private SeatDetailRepository seatRepo;

    @Mock
    private EventContext eventContext;

    private TableDetailService service;

    private final Event event = Event.builder().id(1L).name("E").status(EventStatus.ACTIVE).build();

    @BeforeEach
    void setUp() {
        service = new TableDetailService(tableRepo, seatRepo, eventContext);
        lenient().when(eventContext.currentId()).thenReturn(1L);
        lenient().when(eventContext.requireEditable()).thenReturn(event);
    }

    @Test
    void createTable_withEntity_createsAllSeats_andSavesTable() {
        // Arrange
        TableDetail table = TableDetail.builder()
                .tableName("A01")
                .noOfSeats(3)
                .description("Front row")
                .build();

        ArgumentCaptor<SeatDetail> seatCaptor = ArgumentCaptor.forClass(SeatDetail.class);
        ArgumentCaptor<TableDetail> tableCaptor = ArgumentCaptor.forClass(TableDetail.class);

        // Act
        service.createTable(table);

        // Assert: 3 seat saves with S1..S3 as AVAILABLE and linked to same table ref
        verify(seatRepo, times(3)).save(seatCaptor.capture());
        List<SeatDetail> savedSeats = seatCaptor.getAllValues();
        assertThat(savedSeats).hasSize(3);
        assertThat(savedSeats.get(0).getSeatNo()).isEqualTo("S1");
        assertThat(savedSeats.get(1).getSeatNo()).isEqualTo("S2");
        assertThat(savedSeats.get(2).getSeatNo()).isEqualTo("S3");
        assertThat(savedSeats).allMatch(s -> s.getSeatStatus() == SeatStatus.AVAILABLE);
        assertThat(savedSeats).allMatch(s -> s.getTableDetail() == table);

        // Table saved with seatDetails populated
        verify(tableRepo, times(1)).save(tableCaptor.capture());
        TableDetail savedTable = tableCaptor.getValue();
        assertThat(savedTable.getSeatDetails()).hasSize(3);
        assertThat(savedTable.getSeatDetails().stream().map(SeatDetail::getSeatNo).toList())
                .containsExactly("S1", "S2", "S3");
        assertThat(savedTable.getEvent()).isSameAs(event);
    }

    @Test
    void createTable_withArgs_createsAvailable_thenUnavailableSeats_inOrder_andSaves() {
        // Arrange
        String name = "B02";
        int available = 2;
        int unavailable = 1;
        String desc = "Side";

        ArgumentCaptor<SeatDetail> seatCaptor = ArgumentCaptor.forClass(SeatDetail.class);
        ArgumentCaptor<TableDetail> tableCaptor = ArgumentCaptor.forClass(TableDetail.class);

        // Act
        service.createTable(name, available, unavailable, desc);

        // Assert available+unavailable saves
        verify(seatRepo, times(available + unavailable)).save(seatCaptor.capture());
        List<SeatDetail> seats = seatCaptor.getAllValues();
        assertThat(seats).hasSize(3);

        // Seat numbering increments across both batches
        assertThat(seats.get(0).getSeatNo()).isEqualTo("S1");
        assertThat(seats.get(1).getSeatNo()).isEqualTo("S2");
        assertThat(seats.get(2).getSeatNo()).isEqualTo("S3");

        // First 2 AVAILABLE, last 1 UNAVAILABLE
        assertThat(seats.get(0).getSeatStatus()).isEqualTo(SeatStatus.AVAILABLE);
        assertThat(seats.get(1).getSeatStatus()).isEqualTo(SeatStatus.AVAILABLE);
        assertThat(seats.get(2).getSeatStatus()).isEqualTo(SeatStatus.UNAVAILABLE);

        // Table saved with correct total seats and description
        verify(tableRepo, times(1)).save(tableCaptor.capture());
        TableDetail savedTable = tableCaptor.getValue();
        assertThat(savedTable.getTableName()).isEqualTo(name);
        assertThat(savedTable.getDescription()).isEqualTo(desc);
        assertThat(savedTable.getNoOfSeats()).isEqualTo(available + unavailable);
        assertThat(savedTable.getSeatDetails()).hasSize(3);
        assertThat(savedTable.getEvent()).isSameAs(event);
    }

    @Test
    void getAllTables_delegatesToRepository() {
        List<TableDetail> data = List.of(
                TableDetail.builder().tableName("A01").noOfSeats(4).build(),
                TableDetail.builder().tableName("A02").noOfSeats(6).build()
        );
        when(tableRepo.findByEventIdOrderByIdAsc(1L)).thenReturn(data);

        List<TableDetail> out = service.getAllTables();

        assertThat(out).isSameAs(data);
        verify(tableRepo, times(1)).findByEventIdOrderByIdAsc(1L);
    }

    @Test
    void getGetTableSummary_computesTotals_fromRepositoryData() {
        List<TableDetail> data = new ArrayList<>();
        data.add(TableDetail.builder().tableName("T1").noOfSeats(5).build());
        data.add(TableDetail.builder().tableName("T2").noOfSeats(7).build());
        when(tableRepo.findByEventIdOrderByIdAsc(1L)).thenReturn(List.copyOf(data));

        TableDetailSummary summary = service.getGetTableSummary();

        assertThat(summary.getTotalNoOfTables()).isEqualTo(2);
        assertThat(summary.getTotalNoOfTotalSeats()).isEqualTo(12);
        verify(tableRepo, times(1)).findByEventIdOrderByIdAsc(1L);
    }

    @Test
    void delete_delegatesToRepository() {
        TableDetail table = TableDetail.builder().id(4L).tableName("X").noOfSeats(2).event(event).build();
        when(tableRepo.findByIdAndEventId(4L, 1L)).thenReturn(Optional.of(table));

        service.delete(table);

        verify(tableRepo, times(1)).delete(table);
    }

    @Test
    void delete_throws_whenTableNotInCurrentEvent() {
        Event other = Event.builder().id(2L).name("Other").status(EventStatus.ACTIVE).build();
        TableDetail table = TableDetail.builder().id(4L).tableName("X").noOfSeats(2).event(other).build();
        when(tableRepo.findByIdAndEventId(4L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(table)).isInstanceOf(EventAccessException.class);
        verify(tableRepo, never()).delete(any());
    }
}
