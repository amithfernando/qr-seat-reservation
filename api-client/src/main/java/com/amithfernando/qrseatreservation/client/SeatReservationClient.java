package com.amithfernando.qrseatreservation.client;

import com.amithfernando.qrseatreservation.client.api.CheckInApi;
import com.amithfernando.qrseatreservation.client.api.ReservationsApi;
import com.amithfernando.qrseatreservation.client.api.SellersApi;
import com.amithfernando.qrseatreservation.client.api.SettingsApi;
import com.amithfernando.qrseatreservation.client.api.TablesApi;
import com.amithfernando.qrseatreservation.client.api.TicketsApi;
import com.amithfernando.qrseatreservation.client.api.UsersApi;
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;

/**
 * Main facade for QR Seat Reservation API Client
 * Provides convenient access to all API endpoints
 */
public class SeatReservationClient {
    
    private final CheckInApi checkinApi;
    private final ReservationsApi reservationApi;
    private final SellersApi sellerApi;
    private final SettingsApi settingApi;
    private final TablesApi tableApi;
    private final TicketsApi ticketApi;
    private final UsersApi userApi;
    
    private final ApiClient apiClient;
    
    /**
     * Create client with default configuration (localhost:8080)
     */
    public SeatReservationClient() {
        this("http://localhost:8080");
    }
    
    /**
     * Create client with custom base URL
     */
    public SeatReservationClient(String basePath) {
        this.apiClient = new ApiClient();
        this.apiClient.setBasePath(basePath);
        
        this.checkinApi = new CheckInApi(apiClient);
        this.reservationApi = new ReservationsApi(apiClient);
        this.sellerApi = new SellersApi(apiClient);
        this.settingApi = new SettingsApi(apiClient);
        this.tableApi = new TablesApi(apiClient);
        this.ticketApi = new TicketsApi(apiClient);
        this.userApi = new UsersApi(apiClient);
    }
    
    /**
     * Create client with custom ApiClient configuration
     */
    public SeatReservationClient(ApiClient apiClient) {
        this.apiClient = apiClient;

        this.checkinApi = new CheckInApi(apiClient);
        this.reservationApi = new ReservationsApi(apiClient);
        this.sellerApi = new SellersApi(apiClient);
        this.settingApi = new SettingsApi(apiClient);
        this.tableApi = new TablesApi(apiClient);
        this.ticketApi = new TicketsApi(apiClient);
        this.userApi = new UsersApi(apiClient);
    }
    
    /**
     * Set authentication token
     */
    public void setAuthToken(String token) {
        apiClient.setBearerToken(token);
    }
    
    /**
     * Set API key for authentication
     */
    public void setApiKey(String apiKey) {
        apiClient.setApiKey(apiKey);
    }

    public CheckInApi getCheckinApi() {
        return checkinApi;
    }

    public ReservationsApi getReservationApi() {
        return reservationApi;
    }

    public SellersApi getSellerApi() {
        return sellerApi;
    }

    public SettingsApi getSettingApi() {
        return settingApi;
    }

    public TablesApi getTableApi() {
        return tableApi;
    }

    public TicketsApi getTicketApi() {
        return ticketApi;
    }

    public UsersApi getUserApi() {
        return userApi;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }
}
