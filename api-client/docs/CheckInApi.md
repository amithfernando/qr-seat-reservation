# CheckInApi

All URIs are relative to *http://localhost:8080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**checkInTicket**](CheckInApi.md#checkInTicket) | **POST** /api/checkin | Check in ticket |
| [**checkInTicketByNumber**](CheckInApi.md#checkInTicketByNumber) | **POST** /api/checkin/{ticketNo} | Check in ticket by number |
| [**getCheckInStats**](CheckInApi.md#getCheckInStats) | **GET** /api/checkin/stats | Get check-in statistics |
| [**getTicketDetails**](CheckInApi.md#getTicketDetails) | **GET** /api/checkin/ticket/{ticketNo} | Get ticket details |



## checkInTicket

> CheckInResponse checkInTicket(checkInRequest)

Check in ticket

Checks in a ticket. Only allowed for PAID reservations that haven&#39;t been checked in yet.

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.CheckInApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        CheckInApi apiInstance = new CheckInApi(defaultClient);
        CheckInRequest checkInRequest = new CheckInRequest(); // CheckInRequest | 
        try {
            CheckInResponse result = apiInstance.checkInTicket(checkInRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling CheckInApi#checkInTicket");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **checkInRequest** | [**CheckInRequest**](CheckInRequest.md)|  | |

### Return type

[**CheckInResponse**](CheckInResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Check-in processed (check success field for result) |  -  |
| **400** | Invalid request data |  -  |
| **500** | Internal server error |  -  |


## checkInTicketByNumber

> CheckInResponse checkInTicketByNumber(ticketNo)

Check in ticket by number

Simplified endpoint to check in a ticket using path parameter

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.CheckInApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        CheckInApi apiInstance = new CheckInApi(defaultClient);
        String ticketNo = "ticketNo_example"; // String | 
        try {
            CheckInResponse result = apiInstance.checkInTicketByNumber(ticketNo);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling CheckInApi#checkInTicketByNumber");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **ticketNo** | **String**|  | |

### Return type

[**CheckInResponse**](CheckInResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Check-in processed (check success field for result) |  -  |
| **500** | Internal server error |  -  |


## getCheckInStats

> CheckInStats getCheckInStats()

Get check-in statistics

Retrieves overall check-in statistics

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.CheckInApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        CheckInApi apiInstance = new CheckInApi(defaultClient);
        try {
            CheckInStats result = apiInstance.getCheckInStats();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling CheckInApi#getCheckInStats");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters

This endpoint does not need any parameter.

### Return type

[**CheckInStats**](CheckInStats.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Successfully retrieved statistics |  -  |
| **500** | Internal server error |  -  |


## getTicketDetails

> TicketDetailsResponse getTicketDetails(ticketNo)

Get ticket details

Retrieves detailed information about a ticket including whether it can be checked in

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.CheckInApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        CheckInApi apiInstance = new CheckInApi(defaultClient);
        String ticketNo = "ticketNo_example"; // String | 
        try {
            TicketDetailsResponse result = apiInstance.getTicketDetails(ticketNo);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling CheckInApi#getTicketDetails");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **ticketNo** | **String**|  | |

### Return type

[**TicketDetailsResponse**](TicketDetailsResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Successfully retrieved ticket details |  -  |
| **404** | Ticket not found - TICKET_NOT_FOUND |  -  |
| **500** | Internal server error |  -  |

