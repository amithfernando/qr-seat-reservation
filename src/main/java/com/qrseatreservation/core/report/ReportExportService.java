package com.qrseatreservation.core.report;

import com.qrseatreservation.core.event.EventContext;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qrseatreservation.core.model.ReservationDetail;
import com.qrseatreservation.core.model.SeatReservation;
import com.qrseatreservation.core.repository.ReservationDetailRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Builds report data from ticket-level statuses and exports it as XLSX, CSV or PDF. */
@Service
@Slf4j
public class ReportExportService {

    public enum Format {
        XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
        CSV("csv", "text/csv"),
        PDF("pdf", "application/pdf");

        public final String extension;
        public final String mimeType;

        Format(String extension, String mimeType) {
            this.extension = extension;
            this.mimeType = mimeType;
        }
    }

    public enum ReportType {
        TICKET_LIST("Ticket List", "ticket-list"),
        SELLER_SALES("Seller Sales", "seller-sales"),
        CHECK_INS("Check-in List", "check-in-list"),
        SELLER_CHECK_INS("Seller Check-ins", "seller-check-ins");

        public final String title;
        public final String fileBase;

        ReportType(String title, String fileBase) {
            this.title = title;
            this.fileBase = fileBase;
        }
    }

    /** A titled table of display values (String or Number). */
    public record Table(String title, List<String> headers, List<List<Object>> rows) {}

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Comparator<TicketReportRow> BY_SELLER_TABLE_SEAT = Comparator
            .comparing(TicketReportRow::seller, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(TicketReportRow::table, ReportExportService::naturalCompare)
            .thenComparing(TicketReportRow::seat, ReportExportService::naturalCompare);

    private final ReservationDetailRepository reservationDetailRepository;
    private final EventContext eventContext;

    public ReportExportService(ReservationDetailRepository reservationDetailRepository, EventContext eventContext) {
        this.reservationDetailRepository = reservationDetailRepository;
        this.eventContext = eventContext;
    }

    // ── Data ────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TicketReportRow> ticketRows() {
        List<TicketReportRow> rows = new ArrayList<>();
        for (ReservationDetail rd : reservationDetailRepository.findAllWithDetails(eventContext.currentId())) {
            if (rd.getSeatReservations() == null) continue;
            String seller = rd.getSellerDetail() != null && rd.getSellerDetail().getName() != null
                    ? rd.getSellerDetail().getName() : "Unknown";
            for (SeatReservation sr : rd.getSeatReservations()) {
                if (sr == null || sr.getTicketNo() == null) continue;
                rows.add(new TicketReportRow(
                        seller,
                        nz(rd.getReferenceNo()),
                        sr.getTicketNo(),
                        sr.getSeatDetail() != null && sr.getSeatDetail().getTableDetail() != null
                                ? nz(sr.getSeatDetail().getTableDetail().getTableName()) : "",
                        sr.getSeatDetail() != null ? nz(sr.getSeatDetail().getSeatNo()) : "",
                        categoryName(sr),
                        price(sr),
                        sr.getReservationStatus(),
                        nz(sr.getBuyerName()),
                        sr.getCreatedAt(),
                        sr.getUpdatedAt()));
            }
        }
        rows.sort(BY_SELLER_TABLE_SEAT);
        return rows;
    }

    public static List<SellerReport> bySeller(List<TicketReportRow> rows) {
        Map<String, List<TicketReportRow>> grouped = new LinkedHashMap<>();
        for (TicketReportRow r : rows) grouped.computeIfAbsent(r.seller(), k -> new ArrayList<>()).add(r);
        return grouped.entrySet().stream().map(e -> new SellerReport(e.getKey(), e.getValue())).toList();
    }

    public static List<TicketReportRow> checkedIn(List<TicketReportRow> rows) {
        return rows.stream().filter(TicketReportRow::isCheckedIn)
                .sorted(Comparator.comparing(TicketReportRow::checkedInAt,
                        Comparator.nullsLast(Comparator.<LocalDateTime>naturalOrder())).reversed())
                .toList();
    }

    // ── Report definitions ──────────────────────────────────────────────────

    /** The tables making up a report; the first is the main table (used for CSV). */
    public List<Table> tables(ReportType type, List<TicketReportRow> rows) {
        return switch (type) {
            case TICKET_LIST -> List.of(new Table("Tickets",
                    List.of("Ticket No", "Table", "Seat", "Category", "Price (€)", "Status", "Seller", "Buyer", "Reserved At", "Checked In At"),
                    rows.stream().map(r -> List.<Object>of(r.ticketNo(), r.table(), r.seat(), r.category(), r.price(),
                            r.statusLabel(), r.seller(), r.buyerName(), fmt(r.reservedAt()), fmt(r.checkedInAt()))).toList()));
            case SELLER_SALES -> {
                List<SellerReport> sellers = bySeller(rows).stream()
                        .sorted(Comparator.comparingDouble(SellerReport::revenue).reversed()).toList();
                List<List<Object>> summary = new ArrayList<>();
                for (SellerReport s : sellers) {
                    summary.add(List.of(s.seller(), s.reserved(), s.paid(), s.unpaid(), s.checkedIn(), s.revenue(),
                            String.join(", ", s.paidTicketNos())));
                }
                summary.add(List.of("TOTAL", sum(sellers, SellerReport::reserved), sum(sellers, SellerReport::paid),
                        sum(sellers, SellerReport::unpaid), sum(sellers, SellerReport::checkedIn),
                        sellers.stream().mapToDouble(SellerReport::revenue).sum(), ""));
                yield List.of(
                        new Table("Seller Summary",
                                List.of("Seller", "Reserved", "Paid", "Unpaid", "Checked In", "Revenue (€)", "Paid Ticket Nos"),
                                summary),
                        new Table("Ticket Sales",
                                List.of("Seller", "Ticket No", "Table", "Seat", "Category", "Price (€)", "Status", "Buyer", "Reserved At"),
                                rows.stream().map(r -> List.<Object>of(r.seller(), r.ticketNo(), r.table(), r.seat(), r.category(),
                                        r.price(), r.statusLabel(), r.buyerName(), fmt(r.reservedAt()))).toList()));
            }
            case CHECK_INS -> List.of(new Table("Check-ins",
                    List.of("Checked In At", "Ticket No", "Table", "Seat", "Category", "Seller", "Buyer"),
                    checkedIn(rows).stream().map(r -> List.<Object>of(fmt(r.checkedInAt()), r.ticketNo(), r.table(), r.seat(),
                            r.category(), r.seller(), r.buyerName())).toList()));
            case SELLER_CHECK_INS -> {
                List<SellerReport> sellers = bySeller(rows).stream()
                        .sorted(Comparator.comparingLong(SellerReport::checkedIn).reversed()).toList();
                List<List<Object>> summary = new ArrayList<>();
                for (SellerReport s : sellers) {
                    summary.add(List.of(s.seller(), s.paid(), s.checkedIn(), s.notArrived(), String.join(", ", s.checkedInTicketNos())));
                }
                summary.add(List.of("TOTAL", sum(sellers, SellerReport::paid), sum(sellers, SellerReport::checkedIn),
                        sum(sellers, SellerReport::notArrived), ""));
                yield List.of(
                        new Table("Seller Check-in Summary",
                                List.of("Seller", "Paid Tickets", "Checked In", "Not Arrived", "Checked-in Ticket Nos"),
                                summary),
                        new Table("Checked-in Tickets",
                                List.of("Seller", "Ticket No", "Table", "Seat", "Category", "Checked In At"),
                                rows.stream().filter(TicketReportRow::isCheckedIn)
                                        .map(r -> List.<Object>of(r.seller(), r.ticketNo(), r.table(), r.seat(), r.category(),
                                                fmt(r.checkedInAt()))).toList()));
            }
        };
    }

    public String fileName(ReportType type, Format format) {
        return type.fileBase + "-" + LocalDate.now() + "." + format.extension;
    }

    public byte[] export(ReportType type, Format format) throws IOException {
        List<TicketReportRow> rows = ticketRows();
        List<Table> tables = tables(type, rows);
        byte[] bytes = switch (format) {
            case XLSX -> toXlsx(tables);
            case CSV -> toCsv(tables.get(0));
            case PDF -> toPdf(type.title, tables);
        };
        log.info("Exported {} as {} ({} ticket rows, {} bytes)", type, format, rows.size(), bytes.length);
        return bytes;
    }

    // ── Writers ─────────────────────────────────────────────────────────────

    static byte[] toXlsx(List<Table> tables) throws IOException {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            CellStyle head = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font bold = wb.createFont();
            bold.setBold(true);
            head.setFont(bold);
            head.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            head.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            CellStyle money = wb.createCellStyle();
            money.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));

            for (Table t : tables) {
                Sheet sheet = wb.createSheet(t.title().length() > 31 ? t.title().substring(0, 31) : t.title());
                Row hr = sheet.createRow(0);
                for (int c = 0; c < t.headers().size(); c++) {
                    var cell = hr.createCell(c);
                    cell.setCellValue(t.headers().get(c));
                    cell.setCellStyle(head);
                }
                int r = 1;
                for (List<Object> values : t.rows()) {
                    Row row = sheet.createRow(r++);
                    for (int c = 0; c < values.size(); c++) {
                        Object v = values.get(c);
                        var cell = row.createCell(c);
                        if (v instanceof Double d) {
                            cell.setCellValue(d);
                            cell.setCellStyle(money);
                        } else if (v instanceof Number n) {
                            cell.setCellValue(n.doubleValue());
                        } else {
                            cell.setCellValue(v == null ? "" : v.toString());
                        }
                    }
                }
                sheet.createFreezePane(0, 1);
                if (!t.rows().isEmpty()) sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, r - 1, 0, t.headers().size() - 1));
                for (int c = 0; c < t.headers().size(); c++) {
                    sheet.autoSizeColumn(c);
                    // Keep long ticket-number lists readable instead of extremely wide
                    sheet.setColumnWidth(c, Math.min(sheet.getColumnWidth(c) + 512, 60 * 256));
                }
            }
            wb.write(out);
            return out.toByteArray();
        }
    }

    static byte[] toCsv(Table t) {
        StringBuilder sb = new StringBuilder("\uFEFF"); // BOM so Excel opens UTF-8 (€, names) correctly
        appendCsvLine(sb, new ArrayList<>(t.headers()));
        for (List<Object> row : t.rows()) appendCsvLine(sb, row);
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void appendCsvLine(StringBuilder sb, List<?> values) {
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(',');
            Object v = values.get(i);
            String s = v instanceof Double d ? String.format(java.util.Locale.ROOT, "%.2f", d) : Objects.toString(v, "");
            if (s.contains(",") || s.contains("\"") || s.contains("\n")) s = '"' + s.replace("\"", "\"\"") + '"';
            sb.append(s);
        }
        sb.append("\r\n");
    }

    static byte[] toPdf(String title, List<Table> tables) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);
            PdfWriter.getInstance(doc, out);
            doc.open();
            BaseFont base = pdfBaseFont();
            Font titleFont = new Font(base, 16, Font.BOLD);
            Font h2 = new Font(base, 12, Font.BOLD);
            Font headFont = new Font(base, 8, Font.BOLD, Color.WHITE);
            Font cellFont = new Font(base, 8);
            Font small = new Font(base, 8, Font.NORMAL, Color.GRAY);

            doc.add(new Paragraph(title, titleFont));
            doc.add(new Paragraph("Generated " + LocalDateTime.now().format(DATE_TIME), small));

            for (Table t : tables) {
                Paragraph p = new Paragraph(t.title() + " (" + t.rows().size() + ")", h2);
                p.setSpacingBefore(12);
                p.setSpacingAfter(6);
                doc.add(p);
                if (t.rows().isEmpty()) {
                    doc.add(new Paragraph("No data.", cellFont));
                    continue;
                }
                PdfPTable pt = new PdfPTable(t.headers().size());
                pt.setWidthPercentage(100);
                pt.setHeaderRows(1);
                for (String h : t.headers()) {
                    PdfPCell c = new PdfPCell(new Phrase(h, headFont));
                    c.setBackgroundColor(new Color(0x4f, 0x46, 0xe5));
                    c.setPadding(4);
                    pt.addCell(c);
                }
                boolean odd = false;
                for (List<Object> row : t.rows()) {
                    for (Object v : row) {
                        String s = v instanceof Double d ? String.format("%.2f", d) : Objects.toString(v, "");
                        PdfPCell c = new PdfPCell(new Phrase(s, cellFont));
                        c.setPadding(3);
                        if (v instanceof Number) c.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        if (odd) c.setBackgroundColor(new Color(0xf1, 0xf5, 0xf9));
                        pt.addCell(c);
                    }
                    odd = !odd;
                }
                doc.add(pt);
            }
            doc.close();
            return out.toByteArray();
        }
    }

    /** Unicode font when available (names, €), otherwise the built-in Helvetica. */
    private static BaseFont pdfBaseFont() {
        for (String path : List.of("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", "C:/Windows/Fonts/arial.ttf")) {
            if (Files.exists(Path.of(path))) {
                try {
                    return BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                } catch (Exception e) {
                    log.debug("Could not load PDF font {}", path, e);
                }
            }
        }
        try {
            return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
        } catch (Exception e) {
            throw new IllegalStateException("No PDF font available", e);
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private static String categoryName(SeatReservation sr) {
        if (sr.getTicketCategory() != null && sr.getTicketCategory().getName() != null) return sr.getTicketCategory().getName();
        return sr.getTicketType() != null ? sr.getTicketType().name() : "";
    }

    private static double price(SeatReservation sr) {
        if (sr.getTicketPrice() != null) return sr.getTicketPrice();
        if (sr.getTicketCategory() != null && sr.getTicketCategory().getPrice() != null) return sr.getTicketCategory().getPrice();
        return 0.0;
    }

    private static long sum(List<SellerReport> list, java.util.function.ToLongFunction<SellerReport> f) {
        return list.stream().mapToLong(f).sum();
    }

    private static String fmt(LocalDateTime t) {
        return t == null ? "" : t.format(DATE_TIME);
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    /** Orders "T2" before "T10" and "S9" before "S12". */
    static int naturalCompare(String a, String b) {
        String x = a == null ? "" : a, y = b == null ? "" : b;
        String px = x.replaceAll("\\d+$", ""), py = y.replaceAll("\\d+$", "");
        int c = px.compareToIgnoreCase(py);
        if (c != 0) return x.compareToIgnoreCase(y);
        String nx = x.substring(px.length()), ny = y.substring(py.length());
        if (nx.isEmpty() || ny.isEmpty()) return x.compareToIgnoreCase(y);
        return Long.compare(Long.parseLong(nx.length() > 18 ? nx.substring(0, 18) : nx),
                Long.parseLong(ny.length() > 18 ? ny.substring(0, 18) : ny));
    }
}
