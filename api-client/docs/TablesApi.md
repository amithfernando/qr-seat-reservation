# TablesApi

All URIs are relative to *http://localhost:8080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**canModifyTable**](TablesApi.md#canModifyTable) | **GET** /api/tables/{id}/can-modify | Check if table can be modified |
| [**createTable**](TablesApi.md#createTable) | **POST** /api/tables | Create custom table |
| [**deleteTable**](TablesApi.md#deleteTable) | **DELETE** /api/tables/{id} | Delete table |
| [**getAllTables**](TablesApi.md#getAllTables) | **GET** /api/tables | Get all tables |
| [**getTableById**](TablesApi.md#getTableById) | **GET** /api/tables/{id} | Get table by ID |
| [**getTableByName**](TablesApi.md#getTableByName) | **GET** /api/tables/search | Get table by name |
| [**getTableSummary**](TablesApi.md#getTableSummary) | **GET** /api/tables/summary | Get table summary |
| [**updateTable**](TablesApi.md#updateTable) | **PUT** /api/tables/{id} | Update table |



## canModifyTable

> Boolean canModifyTable(id)

Check if table can be modified

Checks if a table has any active reservations

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.TablesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        TablesApi apiInstance = new TablesApi(defaultClient);
        Long id = 56L; // Long | 
        try {
            Boolean result = apiInstance.canModifyTable(id);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling TablesApi#canModifyTable");
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
| **200** | Successfully checked table status |  -  |
| **404** | Table not found - TABLE_NOT_FOUND |  -  |


## createTable

> createTable(createTableRequest)

Create custom table

Creates a table with specified available and unavailable seats

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.TablesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        TablesApi apiInstance = new TablesApi(defaultClient);
        CreateTableRequest createTableRequest = new CreateTableRequest(); // CreateTableRequest | 
        try {
            apiInstance.createTable(createTableRequest);
        } catch (ApiException e) {
            System.err.println("Exception when calling TablesApi#createTable");
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
| **createTableRequest** | [**CreateTableRequest**](CreateTableRequest.md)|  | |

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
| **201** | Table created successfully |  -  |
| **400** | Invalid table data |  -  |
| **409** | Table already exists - TABLE_ALREADY_EXISTS |  -  |
| **500** | Internal server error |  -  |


## deleteTable

> deleteTable(id)

Delete table

Deletes a table and its associated seats. Cannot delete tables with active reservations.

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.TablesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        TablesApi apiInstance = new TablesApi(defaultClient);
        Long id = 56L; // Long | 
        try {
            apiInstance.deleteTable(id);
        } catch (ApiException e) {
            System.err.println("Exception when calling TablesApi#deleteTable");
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
| **204** | Table deleted successfully |  -  |
| **404** | Table not found - TABLE_NOT_FOUND |  -  |
| **409** | Table has active reservations - TABLE_HAS_RESERVATIONS |  -  |
| **500** | Internal server error |  -  |


## getAllTables

> List&lt;TableDetail&gt; getAllTables()

Get all tables

Retrieves all tables with their seat details

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.TablesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        TablesApi apiInstance = new TablesApi(defaultClient);
        try {
            List<TableDetail> result = apiInstance.getAllTables();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling TablesApi#getAllTables");
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

[**List&lt;TableDetail&gt;**](TableDetail.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Successfully retrieved tables |  -  |
| **500** | Internal server error |  -  |


## getTableById

> TableDetail getTableById(id)

Get table by ID

Retrieves a specific table by its ID

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.TablesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        TablesApi apiInstance = new TablesApi(defaultClient);
        Long id = 56L; // Long | 
        try {
            TableDetail result = apiInstance.getTableById(id);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling TablesApi#getTableById");
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

[**TableDetail**](TableDetail.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Successfully retrieved table |  -  |
| **404** | Table not found - TABLE_NOT_FOUND |  -  |
| **500** | Internal server error |  -  |


## getTableByName

> TableDetail getTableByName(name)

Get table by name

Retrieves a specific table by its name

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.TablesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        TablesApi apiInstance = new TablesApi(defaultClient);
        String name = "name_example"; // String | 
        try {
            TableDetail result = apiInstance.getTableByName(name);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling TablesApi#getTableByName");
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

[**TableDetail**](TableDetail.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Successfully retrieved table |  -  |
| **404** | Table not found - TABLE_NOT_FOUND |  -  |
| **500** | Internal server error |  -  |


## getTableSummary

> TableDetailSummary getTableSummary()

Get table summary

Retrieves summary statistics of all tables

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.TablesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        TablesApi apiInstance = new TablesApi(defaultClient);
        try {
            TableDetailSummary result = apiInstance.getTableSummary();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling TablesApi#getTableSummary");
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

[**TableDetailSummary**](TableDetailSummary.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Successfully retrieved summary |  -  |
| **500** | Internal server error |  -  |


## updateTable

> TableDetail updateTable(id, createTableRequest)

Update table

Updates an existing table&#39;s details. Cannot update tables with active reservations.

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.TablesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        TablesApi apiInstance = new TablesApi(defaultClient);
        Long id = 56L; // Long | 
        CreateTableRequest createTableRequest = new CreateTableRequest(); // CreateTableRequest | 
        try {
            TableDetail result = apiInstance.updateTable(id, createTableRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling TablesApi#updateTable");
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
| **createTableRequest** | [**CreateTableRequest**](CreateTableRequest.md)|  | |

### Return type

[**TableDetail**](TableDetail.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Table updated successfully |  -  |
| **400** | Invalid table data |  -  |
| **404** | Table not found - TABLE_NOT_FOUND |  -  |
| **409** | Table name already exists (TABLE_ALREADY_EXISTS) or table has active reservations (TABLE_HAS_RESERVATIONS) |  -  |
| **500** | Internal server error |  -  |

