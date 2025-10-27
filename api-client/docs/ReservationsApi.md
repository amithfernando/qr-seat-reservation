# ReservationsApi

All URIs are relative to *http://localhost:8080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**createReservation**](ReservationsApi.md#createReservation) | **POST** /api/reservations | Create reservation |
| [**deleteReservation**](ReservationsApi.md#deleteReservation) | **DELETE** /api/reservations/{id} | Delete reservation |
| [**getAllReservations**](ReservationsApi.md#getAllReservations) | **GET** /api/reservations | Get all reservations |
| [**getReservationById**](ReservationsApi.md#getReservationById) | **GET** /api/reservations/{id} | Get reservation by ID |
| [**markReservationAsPaid**](ReservationsApi.md#markReservationAsPaid) | **PATCH** /api/reservations/{id}/mark-paid | Mark reservation as paid |



## createReservation

> ReservationResponse createReservation(createReservationRequest)

Create reservation

Creates a new seat reservation with specified seats and ticket types

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.ReservationsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        ReservationsApi apiInstance = new ReservationsApi(defaultClient);
        CreateReservationRequest createReservationRequest = new CreateReservationRequest(); // CreateReservationRequest | 
        try {
            ReservationResponse result = apiInstance.createReservation(createReservationRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ReservationsApi#createReservation");
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
| **createReservationRequest** | [**CreateReservationRequest**](CreateReservationRequest.md)|  | |

### Return type

[**ReservationResponse**](ReservationResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **201** | Reservation created successfully |  -  |
| **400** | Invalid reservation data |  -  |
| **404** | Seller or seat not found |  -  |
| **409** | Seat already reserved - SEAT_ALREADY_RESERVED |  -  |
| **500** | Internal server error |  -  |


## deleteReservation

> deleteReservation(id)

Delete reservation

Deletes a reservation and releases associated seats

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.ReservationsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        ReservationsApi apiInstance = new ReservationsApi(defaultClient);
        Long id = 56L; // Long | 
        try {
            apiInstance.deleteReservation(id);
        } catch (ApiException e) {
            System.err.println("Exception when calling ReservationsApi#deleteReservation");
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
| **id** | **Long**|  | |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Reservation deleted successfully |  -  |
| **404** | Reservation not found - RESERVATION_NOT_FOUND |  -  |
| **500** | Internal server error |  -  |


## getAllReservations

> List&lt;ReservationResponse&gt; getAllReservations()

Get all reservations

Retrieves all seat reservations

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.ReservationsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        ReservationsApi apiInstance = new ReservationsApi(defaultClient);
        try {
            List<ReservationResponse> result = apiInstance.getAllReservations();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ReservationsApi#getAllReservations");
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

[**List&lt;ReservationResponse&gt;**](ReservationResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Successfully retrieved reservations |  -  |
| **500** | Internal server error |  -  |


## getReservationById

> ReservationResponse getReservationById(id)

Get reservation by ID

Retrieves a specific reservation by its ID

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.ReservationsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        ReservationsApi apiInstance = new ReservationsApi(defaultClient);
        Long id = 56L; // Long | 
        try {
            ReservationResponse result = apiInstance.getReservationById(id);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ReservationsApi#getReservationById");
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
| **id** | **Long**|  | |

### Return type

[**ReservationResponse**](ReservationResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Successfully retrieved reservation |  -  |
| **404** | Reservation not found - RESERVATION_NOT_FOUND |  -  |
| **500** | Internal server error |  -  |


## markReservationAsPaid

> ReservationResponse markReservationAsPaid(id)

Mark reservation as paid

Updates reservation status to PAID

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.ReservationsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        ReservationsApi apiInstance = new ReservationsApi(defaultClient);
        Long id = 56L; // Long | 
        try {
            ReservationResponse result = apiInstance.markReservationAsPaid(id);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling ReservationsApi#markReservationAsPaid");
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
| **id** | **Long**|  | |

### Return type

[**ReservationResponse**](ReservationResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Reservation marked as paid successfully |  -  |
| **404** | Reservation not found - RESERVATION_NOT_FOUND |  -  |
| **500** | Internal server error |  -  |

