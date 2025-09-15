package com.amithfernando.qrseatreservation.ui.view.component;

import com.amithfernando.qrseatreservation.core.model.SeatDetail;
import com.amithfernando.qrseatreservation.core.model.TableDetail;
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
    private List<TableDetail> allTables;
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

        // Handle clicks
        getElement().executeJs("""
            const canvas = this;
            canvas.addEventListener('click', function(evt) {
                const rect = canvas.getBoundingClientRect();
                const x = evt.clientX - rect.left;
                const y = evt.clientY - rect.top;
                this.$server.toggleSeatAt(x, y);
            });
        """);
    }

    public void draw() {
        StringBuilder js = new StringBuilder("""
            const ctx = this.getContext('2d');
            ctx.canvas.width  = window.innerWidth;
            ctx.canvas.height = window.innerHeight;
            ctx.clearRect(0, 0, this.width, this.height);

            // Draw Stage at the top
            ctx.fillStyle = '#444';
            ctx.fillRect(250, 10, 300, 40);
            ctx.fillStyle = 'white';
            ctx.font = '20px Arial';
            ctx.fillText('STAGE', 380, 35);

            // Legend (top-left)
            const legend = [
              { label: 'Available',   color: 'lightgreen' },
              { label: 'Reserved',    color: 'red' },
              { label: 'Checked-in',  color: 'blue' },
              { label: 'Unavailable', color: 'lightgray' }
            ];
            const legendX = 20;
            const legendY = 70;
            const boxSize = 12;
            const lineHeight = 20;

            ctx.font = '14px Arial';
            ctx.textBaseline = 'middle';
            legend.forEach((item, idx) => {
              const y = legendY + idx * lineHeight;
              // color box
              ctx.fillStyle = item.color;
              ctx.fillRect(legendX, y - boxSize/2, boxSize, boxSize);
              ctx.strokeStyle = '#333';
              ctx.strokeRect(legendX, y - boxSize/2, boxSize, boxSize);
              // label
              ctx.fillStyle = '#000';
              ctx.fillText(item.label, legendX + boxSize + 8, y);
            });
        """);

        PositionHelper positionHelper = new PositionHelper(noOfColumns, tableSize, allTables);
        List<TablePosition> tablePositions = positionHelper.getTablePositions();


        for (TablePosition tablePosition : tablePositions) {
            int cx = tablePosition.getX();
            int cy = tablePosition.getY();

            // Draw table circle
            js.append(String.format("""
                ctx.beginPath();
                ctx.arc(%d, %d, %d, 0, 2 * Math.PI);
                ctx.fillStyle = 'orange';
                ctx.fill();
                ctx.stroke();

                // Centered table name
                ctx.fillStyle = 'black';
                ctx.font = '16px Arial';
                ctx.textAlign = 'center';
                ctx.textBaseline = 'middle';
                ctx.fillText('%s', %d, %d);
            """, cx, cy, tableSize/2, tablePosition.getTable().getTableName(), cx, cy));

            int seatCount = tablePosition.getTable().getSeatDetails().size(); // seatCount
            int s = 0;
            for (SeatDetail seatDetail : tablePosition.getTable().getSeatDetails()) {
                double angle = 2 * Math.PI * s / seatCount;
                int sx = (int) (cx + Math.cos(angle) * (tableSize/2 + 25));
                int sy = (int) (cy + Math.sin(angle) * (tableSize/2 + 25));

                log.info("seatId: {}, seat: {}", seatDetail.getId(), seatDetail);
                String color = switch (seatDetail.getSeatStatus()) {
                    case UNAVAILABLE -> "lightgray";
                    case RESERVED-> "red";
                    case CHECKED_IN-> "blue";
                    default -> "lightgreen";
                };

                js.append(String.format("""
                    ctx.fillStyle = '%s';
                    ctx.beginPath();
                    ctx.arc(%d, %d, %d, 0, 2 * Math.PI);
                    ctx.fill();
                    ctx.stroke();
                """, color, sx, sy, seatSize/2));

                s++;
            }

        }

        getElement().executeJs(js.toString());
    }

}

@Slf4j
class PositionHelper {

    private int noOfColumns;
    private int tableSize;
    private List<TableDetail> allTables;

    public PositionHelper(int noOfColumns, int tableSize, List<TableDetail> allTables) {
        this.noOfColumns = noOfColumns;
        this.tableSize = tableSize;
        this.allTables = allTables;
    }

    public List<TablePosition> getTablePositions() {
        List<TablePosition> tablePositions = new ArrayList<>();
        int spaceRatio = 2 * tableSize;
        int currentColumn = 1;
        int currentRow = 1;

        for (TableDetail table : allTables) {
            int x = currentColumn * spaceRatio;
            int y = currentRow * spaceRatio;

            TablePosition tablePosition = new TablePosition(table, x, y);
            log.info(tablePosition.toString());
            tablePositions.add(tablePosition);

            if (currentColumn == noOfColumns) {
                // reset column, move to next row
                currentColumn = 1;
                currentRow++;
            } else {
                currentColumn++;
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

