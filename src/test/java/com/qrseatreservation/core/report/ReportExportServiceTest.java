package com.qrseatreservation.core.report;

import com.qrseatreservation.core.enums.ReservationStatus;
import com.qrseatreservation.core.enums.TicketType;
import com.qrseatreservation.core.event.EventContext;
import com.qrseatreservation.core.model.ReservationDetail;
import com.qrseatreservation.core.model.SeatDetail;
import com.qrseatreservation.core.model.SeatReservation;
import com.qrseatreservation.core.model.SellerDetail;
import com.qrseatreservation.core.model.TableDetail;
import com.qrseatreservation.core.report.ReportExportService.Format;
import com.qrseatreservation.core.report.ReportExportService.ReportType;
import com.qrseatreservation.core.repository.ReservationDetailRepository;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReportExportServiceTest {

    private ReportExportService service;

    @BeforeEach
    void setUp() {
        ReservationDetailRepository repo = mock(ReservationDetailRepository.class);
        TableDetail t1 = TableDetail.builder().tableName("T1").build();
        SellerDetail alice = SellerDetail.builder().name("Alice").build();
        SellerDetail bob = SellerDetail.builder().name("Bob").build();

        // Reservation-level status is deliberately PAYMENT_PENDING: reports must use per-ticket status
        ReservationDetail r1 = ReservationDetail.builder().referenceNo("R1").sellerDetail(alice)
                .reservationStatus(ReservationStatus.PAYMENT_PENDING)
                .seatReservations(new LinkedHashSet<>(List.of(
                        ticket("DJ0001", t1, "S1", 40.0, ReservationStatus.CHECKED_IN),
                        ticket("DJ0002", t1, "S2", 25.0, ReservationStatus.PAID),
                        ticket("DJ0003", t1, "S3", 40.0, ReservationStatus.PAYMENT_PENDING))))
                .build();
        ReservationDetail r2 = ReservationDetail.builder().referenceNo("R2").sellerDetail(bob)
                .reservationStatus(ReservationStatus.PAID)
                .seatReservations(Set.of(ticket("DJ0010", t1, "S10", 40.0, ReservationStatus.CHECKED_IN)))
                .build();
        EventContext eventContext = mock(EventContext.class);
        when(eventContext.currentId()).thenReturn(1L);
        when(repo.findAllWithDetails(1L)).thenReturn(List.of(r1, r2));
        service = new ReportExportService(repo, eventContext);
    }

    private static SeatReservation ticket(String no, TableDetail table, String seat, double price, ReservationStatus st) {
        SeatReservation sr = SeatReservation.builder()
                .ticketNo(no)
                .seatDetail(SeatDetail.builder().seatNo(seat).tableDetail(table).build())
                .ticketType(TicketType.FULL)
                .ticketPrice(price)
                .reservationStatus(st)
                .build();
        sr.setCreatedAt(LocalDateTime.of(2026, 10, 1, 10, 0));
        sr.setUpdatedAt(LocalDateTime.of(2026, 10, 27, 20, 15));
        return sr;
    }

    @Test
    void sellerAggregatesUseTicketLevelStatus() {
        List<SellerReport> sellers = ReportExportService.bySeller(service.ticketRows());
        SellerReport alice = sellers.stream().filter(s -> s.seller().equals("Alice")).findFirst().orElseThrow();
        assertThat(alice.reserved()).isEqualTo(3);
        assertThat(alice.paid()).isEqualTo(2);
        assertThat(alice.unpaid()).isEqualTo(1);
        assertThat(alice.checkedIn()).isEqualTo(1);
        assertThat(alice.revenue()).isEqualTo(65.0);
        assertThat(ReportExportService.checkedIn(service.ticketRows())).extracting(TicketReportRow::ticketNo)
                .containsExactlyInAnyOrder("DJ0001", "DJ0010");
    }

    @Test
    void ticketsAreOrderedNaturallyBySeat() {
        assertThat(service.ticketRows()).extracting(TicketReportRow::ticketNo)
                .containsExactly("DJ0001", "DJ0002", "DJ0003", "DJ0010");
    }

    @Test
    void everyReportExportsDataInEveryFormat() throws Exception {
        for (ReportType type : ReportType.values()) {
            byte[] xlsx = service.export(type, Format.XLSX);
            try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(xlsx))) {
                Sheet first = wb.getSheetAt(0);
                assertThat(first.getLastRowNum()).as(type + " xlsx data rows").isGreaterThanOrEqualTo(1);
            }

            String csv = new String(service.export(type, Format.CSV), StandardCharsets.UTF_8);
            assertThat(csv.lines().count()).as(type + " csv lines").isGreaterThanOrEqualTo(2);
            assertThat(csv).contains("DJ00");

            byte[] pdf = service.export(type, Format.PDF);
            assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
            assertThat(pdf.length).isGreaterThan(1000);
        }
    }

    @Test
    void checkInExportContainsOnlyCheckedInTickets() throws Exception {
        String csv = new String(service.export(ReportType.CHECK_INS, Format.CSV), StandardCharsets.UTF_8);
        assertThat(csv).contains("DJ0001", "DJ0010").doesNotContain("DJ0002", "DJ0003");
    }

    @Test
    void csvEscapesCommasAndQuotes() {
        byte[] out = ReportExportService.toCsv(new ReportExportService.Table("t", List.of("a", "b"),
                List.of(List.of("x, y", "say \"hi\""))));
        assertThat(new String(out, StandardCharsets.UTF_8)).contains("\"x, y\",\"say \"\"hi\"\"\"");
    }
}
