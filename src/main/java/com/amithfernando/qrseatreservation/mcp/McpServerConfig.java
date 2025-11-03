package com.amithfernando.qrseatreservation.mcp;


import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpServerConfig {


    @Bean
    public ToolCallbackProvider seatReservationTools(TableTool tableTool,SellerTool sellerTool,ReservationTool reservationTool) {
        return MethodToolCallbackProvider.builder().toolObjects(tableTool,sellerTool,reservationTool).build();
    }
}


