# SettingsApi

All URIs are relative to *http://localhost:8080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getSettings**](SettingsApi.md#getSettings) | **GET** /api/settings | Get current settings |
| [**updateSettings**](SettingsApi.md#updateSettings) | **PUT** /api/settings | Update settings |



## getSettings

> Setting getSettings()

Get current settings

Retrieves the current application settings

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.SettingsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        SettingsApi apiInstance = new SettingsApi(defaultClient);
        try {
            Setting result = apiInstance.getSettings();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling SettingsApi#getSettings");
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

[**Setting**](Setting.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Successfully retrieved settings |  -  |
| **500** | Internal server error |  -  |


## updateSettings

> Setting updateSettings(updateSettingRequest)

Update settings

Updates the application settings. All fields are optional - only provided fields will be updated.

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.SettingsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        SettingsApi apiInstance = new SettingsApi(defaultClient);
        UpdateSettingRequest updateSettingRequest = new UpdateSettingRequest(); // UpdateSettingRequest | 
        try {
            Setting result = apiInstance.updateSettings(updateSettingRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling SettingsApi#updateSettings");
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
| **updateSettingRequest** | [**UpdateSettingRequest**](UpdateSettingRequest.md)|  | |

### Return type

[**Setting**](Setting.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Successfully updated settings |  -  |
| **400** | Invalid settings data |  -  |
| **500** | Internal server error |  -  |

