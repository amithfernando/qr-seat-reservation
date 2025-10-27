# TicketsApi

All URIs are relative to *http://localhost:8080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**deleteAllTickets**](TicketsApi.md#deleteAllTickets) | **DELETE** /api/tickets/all | Delete all tickets |
| [**deleteTicketsByStatus**](TicketsApi.md#deleteTicketsByStatus) | **DELETE** /api/tickets/status/{status} | Delete tickets by status |
| [**downloadTicketImage**](TicketsApi.md#downloadTicketImage) | **GET** /api/tickets/{ticketNo}/image | Download ticket image |
| [**generateTickets**](TicketsApi.md#generateTickets) | **POST** /api/tickets/generate | Generate tickets |
| [**generateTicketsDefault**](TicketsApi.md#generateTicketsDefault) | **POST** /api/tickets/generate/default | Generate tickets (simple) |
| [**getAllTickets**](TicketsApi.md#getAllTickets) | **GET** /api/tickets | Get all tickets |
| [**getTicketByNumber**](TicketsApi.md#getTicketByNumber) | **GET** /api/tickets/{ticketNo} | Get ticket by number |
| [**getTicketStats**](TicketsApi.md#getTicketStats) | **GET** /api/tickets/stats | Get ticket statistics |
| [**getTicketsByStatus**](TicketsApi.md#getTicketsByStatus) | **GET** /api/tickets/status/{status} | Get tickets by status |



## deleteAllTickets

> Map&lt;String, Object&gt; deleteAllTickets()

Delete all tickets

Deletes all tickets from the system (use with caution)

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.TicketsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        TicketsApi apiInstance = new TicketsApi(defaultClient);
        try {
            Map<String, Object> result = apiInstance.deleteAllTickets();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling TicketsApi#deleteAllTickets");
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

**Map&lt;String, Object&gt;**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Tickets deleted successfully |  -  |
| **500** | Internal server error |  -  |


## deleteTicketsByStatus

> Map&lt;String, Object&gt; deleteTicketsByStatus(status)

Delete tickets by status

Deletes all tickets with a specific status

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.TicketsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        TicketsApi apiInstance = new TicketsApi(defaultClient);
        String status = "AVAILABLE"; // String | 
        try {
            Map<String, Object> result = apiInstance.deleteTicketsByStatus(status);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling TicketsApi#deleteTicketsByStatus");
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
| **status** | **String**|  | [enum: AVAILABLE, USED, CANCELLED] |

### Return type

**Map&lt;String, Object&gt;**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Tickets deleted successfully |  -  |
| **500** | Internal server error |  -  |


## downloadTicketImage

> byte[] downloadTicketImage(ticketNo)

Download ticket image

Downloads the QR ticket image

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.TicketsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        TicketsApi apiInstance = new TicketsApi(defaultClient);
        String ticketNo = "ticketNo_example"; // String | 
        try {
            byte[] result = apiInstance.downloadTicketImage(ticketNo);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling TicketsApi#downloadTicketImage");
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

**byte[]**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Successfully retrieved image |  -  |
| **404** | Ticket not found - TICKET_NOT_FOUND |  -  |
| **500** | Internal server error |  -  |


## generateTickets

> Map&lt;String, Object&gt; generateTickets(generateTicketsRequest)

Generate tickets

Generates new tickets. Uses count from request or settings if not provided.

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.TicketsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        TicketsApi apiInstance = new TicketsApi(defaultClient);
        GenerateTicketsRequest generateTicketsRequest = new GenerateTicketsRequest(); // GenerateTicketsRequest | 
        try {
            Map<String, Object> result = apiInstance.generateTickets(generateTicketsRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling TicketsApi#generateTickets");
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
| **generateTicketsRequest** | [**GenerateTicketsRequest**](GenerateTicketsRequest.md)|  | [optional] |

### Return type

**Map&lt;String, Object&gt;**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **201** | Tickets generated successfully |  -  |
| **400** | Invalid request data |  -  |
| **500** | Internal server error |  -  |


## generateTicketsDefault

> Map&lt;String, Object&gt; generateTicketsDefault()

Generate tickets (simple)

Generates tickets using settings configuration

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.TicketsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        TicketsApi apiInstance = new TicketsApi(defaultClient);
        try {
            Map<String, Object> result = apiInstance.generateTicketsDefault();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling TicketsApi#generateTicketsDefault");
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

**Map&lt;String, Object&gt;**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **201** | Tickets generated successfully |  -  |
| **500** | Internal server error |  -  |


## getAllTickets

> List&lt;TicketResponse&gt; getAllTickets()

Get all tickets

Retrieves all tickets with metadata (image data excluded)

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.TicketsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        TicketsApi apiInstance = new TicketsApi(defaultClient);
        try {
            List<TicketResponse> result = apiInstance.getAllTickets();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling TicketsApi#getAllTickets");
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

[**List&lt;TicketResponse&gt;**](TicketResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Successfully retrieved tickets |  -  |
| **500** | Internal server error |  -  |


## getTicketByNumber

> TicketResponse getTicketByNumber(ticketNo)

Get ticket by number

Retrieves ticket metadata by ticket number

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.TicketsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        TicketsApi apiInstance = new TicketsApi(defaultClient);
        String ticketNo = "ticketNo_example"; // String | 
        try {
            TicketResponse result = apiInstance.getTicketByNumber(ticketNo);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling TicketsApi#getTicketByNumber");
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

[**TicketResponse**](TicketResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Successfully retrieved ticket |  -  |
| **404** | Ticket not found - TICKET_NOT_FOUND |  -  |
| **500** | Internal server error |  -  |


## getTicketStats

> TicketStatsResponse getTicketStats()

Get ticket statistics

Retrieves overall ticket statistics

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.TicketsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        TicketsApi apiInstance = new TicketsApi(defaultClient);
        try {
            TicketStatsResponse result = apiInstance.getTicketStats();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling TicketsApi#getTicketStats");
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

[**TicketStatsResponse**](TicketStatsResponse.md)

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


## getTicketsByStatus

> List&lt;TicketResponse&gt; getTicketsByStatus(status)

Get tickets by status

Retrieves all tickets with a specific status

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.TicketsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        TicketsApi apiInstance = new TicketsApi(defaultClient);
        String status = "AVAILABLE"; // String | 
        try {
            List<TicketResponse> result = apiInstance.getTicketsByStatus(status);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling TicketsApi#getTicketsByStatus");
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
| **status** | **String**|  | [enum: AVAILABLE, USED, CANCELLED] |

### Return type

[**List&lt;TicketResponse&gt;**](TicketResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Successfully retrieved tickets |  -  |
| **500** | Internal server error |  -  |

