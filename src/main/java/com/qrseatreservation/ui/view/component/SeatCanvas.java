package com.qrseatreservation.ui.view.component;

import com.qrseatreservation.core.model.SeatDetail;
import com.qrseatreservation.core.model.TableDetail;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.html.Div;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;


@Tag("canvas")
@Slf4j
public class SeatCanvas extends Div {
    private final List<TableDetail> allTables;
    private final int tableSize;
    private final int seatSize;
    private final int noOfColumns;


    public SeatCanvas(List<TableDetail> allTables, int tableSize, int seatSize, int noOfColumns) {
        this.allTables = allTables;
        this.tableSize = tableSize;
        this.seatSize = seatSize;
        this.noOfColumns = noOfColumns;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        draw();
    }

    public void draw() {
        // Fixed visual constants — the DB values (table_size=10, seat_size=20) are
        // too small for pixel rendering; use fixed sizes that look good on screen.
        int tR   = 40;  // table circle radius in pixels
        int sR   = 12;  // seat circle radius in pixels
        int cols = noOfColumns > 0 ? noOfColumns : 2;

        // Calculate the maximum seat count across all tables so the cell is big enough
        int maxSeats = allTables.stream()
                .mapToInt(t -> t.getSeatDetails() == null ? 0 : t.getSeatDetails().size())
                .max().orElse(8);

        // Orbit radius: enough that maxSeats circles of diameter 2*sR don't overlap
        // circumference >= maxSeats * 2*sR → orbitR >= maxSeats * sR / π
        int minOrbitR = (int) Math.ceil(maxSeats * sR / Math.PI);
        int orbitR    = Math.max(tR + sR + 12, minOrbitR);

        int cellPad = 30;
        int cellW   = (orbitR + sR + cellPad) * 2;
        int cellH   = (orbitR + sR + cellPad) * 2 + 20; // +20 for table label

        // Reserve space at top for STAGE + legend
        int topOffset = 160;

        StringBuilder js = new StringBuilder("""
            const ctx = this.getContext('2d');
            ctx.canvas.width  = Math.max(window.innerWidth - 30, 600);
            ctx.canvas.height = Math.max(window.innerHeight, 600);
            ctx.clearRect(0, 0, this.width, this.height);
            """);

        // Stage centred on canvas
        js.append("""
            // Draw Stage
            const stageW = Math.min(300, ctx.canvas.width * 0.4);
            const stageX = (ctx.canvas.width - stageW) / 2;
            ctx.fillStyle = '#444';
            ctx.fillRect(stageX, 10, stageW, 40);
            ctx.fillStyle = 'white';
            ctx.font = 'bold 18px Arial';
            ctx.textAlign = 'center';
            ctx.textBaseline = 'middle';
            ctx.fillText('STAGE', stageX + stageW / 2, 30);

            // Legend
            const legend = [
              { label: 'Available',   color: '#4CAF50' },
              { label: 'Reserved',    color: '#F44336' },
              { label: 'Checked-in',  color: '#2196F3' },
              { label: 'Unavailable', color: '#bdbdbd' }
            ];
            const lx = 14, ly = 60, bs = 13, lh = 22;
            ctx.font = '13px Arial';
            ctx.textAlign = 'left';
            legend.forEach((item, i) => {
              const y = ly + i * lh;
              ctx.fillStyle = item.color;
              ctx.fillRect(lx, y, bs, bs);
              ctx.strokeStyle = '#555';
              ctx.lineWidth = 0.5;
              ctx.strokeRect(lx, y, bs, bs);
              ctx.fillStyle = '#111';
              ctx.fillText(item.label, lx + bs + 6, y + bs / 2 + 1);
            });
            """);

        PositionHelper positionHelper = new PositionHelper(cols, cellW, cellH, topOffset, allTables);
        List<TablePosition> tablePositions = positionHelper.getTablePositions();

        for (TablePosition tp : tablePositions) {
            int cx = tp.getX();
            int cy = tp.getY();

            // Draw table circle
            js.append(String.format("""
                ctx.beginPath();
                ctx.arc(%d, %d, %d, 0, 2 * Math.PI);
                ctx.fillStyle = '#FF9800';
                ctx.fill();
                ctx.strokeStyle = '#555';
                ctx.lineWidth = 1.5;
                ctx.stroke();
                ctx.fillStyle = '#000';
                ctx.font = 'bold 12px Arial';
                ctx.textAlign = 'center';
                ctx.textBaseline = 'middle';
                ctx.fillText('%s', %d, %d);
                """, cx, cy, tR,
                    escapeJs(tp.getTable().getTableName()), cx, cy));

            List<SeatDetail> seats = tp.getTable().getSeatDetails();
            int seatCount = seats.size();
            for (int s = 0; s < seatCount; s++) {
                SeatDetail sd = seats.get(s);
                double angle = 2 * Math.PI * s / Math.max(seatCount, 1) - Math.PI / 2;
                int sx = (int) (cx + Math.cos(angle) * orbitR);
                int sy = (int) (cy + Math.sin(angle) * orbitR);

                String color = switch (sd.getSeatStatus()) {
                    case UNAVAILABLE -> "#bdbdbd";
                    case RESERVED    -> "#F44336";
                    case CHECKED_IN  -> "#2196F3";
                    default          -> "#4CAF50";
                };
                String textColor = sd.getSeatStatus() == com.qrseatreservation.core.enums.SeatStatus.UNAVAILABLE ? "#555" : "white";

                js.append(String.format("""
                    ctx.beginPath();
                    ctx.arc(%d, %d, %d, 0, 2 * Math.PI);
                    ctx.fillStyle = '%s';
                    ctx.fill();
                    ctx.strokeStyle = '#444';
                    ctx.lineWidth = 1;
                    ctx.stroke();
                    ctx.fillStyle = '%s';
                    ctx.font = 'bold 10px Arial';
                    ctx.textAlign = 'center';
                    ctx.textBaseline = 'middle';
                    ctx.fillText('%s', %d, %d);
                    """,
                    sx, sy, sR,
                    color, textColor,
                    escapeJs(sd.getSeatNo()), sx, sy));
            }
        }

        getElement().executeJs(js.toString());
    }

    private static String escapeJs(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("'", "\\'");
    }

}

@Slf4j
class PositionHelper {

    private final int noOfColumns;
    private final int cellW;
    private final int cellH;
    private final int topOffset;
    private final List<TableDetail> allTables;

    public PositionHelper(int noOfColumns, int cellW, int cellH, int topOffset, List<TableDetail> allTables) {
        this.noOfColumns = noOfColumns;
        this.cellW = cellW;
        this.cellH = cellH;
        this.topOffset = topOffset;
        this.allTables = allTables;
    }

    public List<TablePosition> getTablePositions() {
        List<TablePosition> tablePositions = new ArrayList<>();
        int currentColumn = 0;
        int currentRow = 0;

        for (TableDetail table : allTables) {
            // Center of the cell
            int x = cellW / 2 + currentColumn * cellW + 20; // 20px left margin
            int y = topOffset + cellH / 2 + currentRow * cellH;

            tablePositions.add(new TablePosition(table, x, y));

            currentColumn++;
            if (currentColumn >= noOfColumns) {
                currentColumn = 0;
                currentRow++;
            }
        }
        return tablePositions;
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class TablePosition {
    private TableDetail table;
    private  int x;
    private  int y;

    @Override
    public String toString() {
        return "TablePosition{" +
                "table=" + table.getTableName() +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}

