# SellersApi

All URIs are relative to *http://localhost:8080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**canModifySeller**](SellersApi.md#canModifySeller) | **GET** /api/sellers/{id}/can-modify | Check if seller can be modified |
| [**createCustomSeller**](SellersApi.md#createCustomSeller) | **POST** /api/sellers | Create custom seller |
| [**deleteSeller**](SellersApi.md#deleteSeller) | **DELETE** /api/sellers/{id} | Delete seller |
| [**getAllSellers**](SellersApi.md#getAllSellers) | **GET** /api/sellers | Get all sellers |
| [**getSellerById**](SellersApi.md#getSellerById) | **GET** /api/sellers/{id} | Get seller by ID |
| [**getSellerByName**](SellersApi.md#getSellerByName) | **GET** /api/sellers/search | Get seller by name |
| [**updateSeller**](SellersApi.md#updateSeller) | **PUT** /api/sellers/{id} | Update seller |



## canModifySeller

> Boolean canModifySeller(id)

Check if seller can be modified

Checks if a seller has any active reservations

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.SellersApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        SellersApi apiInstance = new SellersApi(defaultClient);
        Long id = 56L; // Long | 
        try {
            Boolean result = apiInstance.canModifySeller(id);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling SellersApi#canModifySeller");
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

**Boolean**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Successfully checked seller status |  -  |
| **404** | Seller not found - SELLER_NOT_FOUND |  -  |


## createCustomSeller

> createCustomSeller(createSellerRequest)

Create custom seller

Creates a seller with individual parameters

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.SellersApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        SellersApi apiInstance = new SellersApi(defaultClient);
        CreateSellerRequest createSellerRequest = new CreateSellerRequest(); // CreateSellerRequest | 
        try {
            apiInstance.createCustomSeller(createSellerRequest);
        } catch (ApiException e) {
            System.err.println("Exception when calling SellersApi#createCustomSeller");
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
| **createSellerRequest** | [**CreateSellerRequest**](CreateSellerRequest.md)|  | |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **201** | Seller created successfully |  -  |
| **400** | Invalid seller data |  -  |
| **409** | Seller already exists - SELLER_ALREADY_EXISTS |  -  |
| **500** | Internal server error |  -  |


## deleteSeller

> deleteSeller(id)

Delete seller

Deletes a seller. Cannot delete sellers with active reservations.

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.SellersApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        SellersApi apiInstance = new SellersApi(defaultClient);
        Long id = 56L; // Long | 
        try {
            apiInstance.deleteSeller(id);
        } catch (ApiException e) {
            System.err.println("Exception when calling SellersApi#deleteSeller");
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
| **204** | Seller deleted successfully |  -  |
| **404** | Seller not found - SELLER_NOT_FOUND |  -  |
| **409** | Seller has active reservations - SELLER_HAS_RESERVATIONS |  -  |
| **500** | Internal server error |  -  |


## getAllSellers

> List&lt;SellerDetail&gt; getAllSellers()

Get all sellers

Retrieves all registered sellers

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.SellersApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        SellersApi apiInstance = new SellersApi(defaultClient);
        try {
            List<SellerDetail> result = apiInstance.getAllSellers();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling SellersApi#getAllSellers");
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

[**List&lt;SellerDetail&gt;**](SellerDetail.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Successfully retrieved sellers |  -  |
| **500** | Internal server error |  -  |


## getSellerById

> SellerDetail getSellerById(id)

Get seller by ID

Retrieves a specific seller by their ID

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.SellersApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        SellersApi apiInstance = new SellersApi(defaultClient);
        Long id = 56L; // Long | 
        try {
            SellerDetail result = apiInstance.getSellerById(id);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling SellersApi#getSellerById");
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

[**SellerDetail**](SellerDetail.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Successfully retrieved seller |  -  |
| **404** | Seller not found - SELLER_NOT_FOUND |  -  |
| **500** | Internal server error |  -  |


## getSellerByName

> SellerDetail getSellerByName(name)

Get seller by name

Retrieves a specific seller by their name

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.SellersApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        SellersApi apiInstance = new SellersApi(defaultClient);
        String name = "name_example"; // String | 
        try {
            SellerDetail result = apiInstance.getSellerByName(name);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling SellersApi#getSellerByName");
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
| **name** | **String**|  | |

### Return type

[**SellerDetail**](SellerDetail.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Successfully retrieved seller |  -  |
| **404** | Seller not found - SELLER_NOT_FOUND |  -  |
| **500** | Internal server error |  -  |


## updateSeller

> SellerDetail updateSeller(id, createSellerRequest)

Update seller

Updates an existing seller&#39;s details. Cannot update sellers with active reservations.

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.SellersApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        SellersApi apiInstance = new SellersApi(defaultClient);
        Long id = 56L; // Long | 
        CreateSellerRequest createSellerRequest = new CreateSellerRequest(); // CreateSellerRequest | 
        try {
            SellerDetail result = apiInstance.updateSeller(id, createSellerRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling SellersApi#updateSeller");
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
| **createSellerRequest** | [**CreateSellerRequest**](CreateSellerRequest.md)|  | |

### Return type

[**SellerDetail**](SellerDetail.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Seller updated successfully |  -  |
| **400** | Invalid seller data |  -  |
| **404** | Seller not found - SELLER_NOT_FOUND |  -  |
| **409** | Seller name already exists (SELLER_ALREADY_EXISTS) or seller has active reservations (SELLER_HAS_RESERVATIONS) |  -  |
| **500** | Internal server error |  -  |

