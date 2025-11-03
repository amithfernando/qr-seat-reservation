package com.amithfernando.qrseatreservation.mcp;

import com.amithfernando.qrseatreservation.core.service.TableDetailService;
import com.amithfernando.qrseatreservation.core.dto.TableDetailResponse;
import com.amithfernando.qrseatreservation.core.dto.TableDetailSummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class TableTool {

    private final TableDetailService tableDetailService;


    public TableTool(TableDetailService tableDetailService) {
        this.tableDetailService = tableDetailService;
    }

    @Tool(name = "addTable",
            description = "Add a table to reservation system. Specify table name, number of available seats, number of unavailable seats.")
    public String createTable(String tableName, int noOfAvailableSeats,int noOfUnAvailableSeats, String description) {
        tableDetailService.createTable(tableName, noOfAvailableSeats, noOfUnAvailableSeats, description);
        return "Table created: " + tableName;
    }

    @Tool(name = "getTableDetailSummary",
            description = "Get table detail summary of the reservation system. It will return total number of tables and seats")
    public TableDetailSummary getGetTableSummary() {
        return tableDetailService.getGetTableSummary();
    }

    @Tool(name = "getAllTables",
            description = "Get all tables of the reservation system. It will return table details of the system")
    public List<TableDetailResponse> getAllTables() {
        return tableDetailService.getAllTables().stream()
                .map(table -> new TableDetailResponse(
                        table.getTableName(),
                        table.getNoOfSeats(),
                        table.getDescription()
                ))
                .toList();
    }
}
