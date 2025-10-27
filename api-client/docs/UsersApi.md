# UsersApi

All URIs are relative to *http://localhost:8080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**changePassword**](UsersApi.md#changePassword) | **PATCH** /api/users/{id}/change-password | Change password |
| [**createUser**](UsersApi.md#createUser) | **POST** /api/users | Create user |
| [**deleteUser**](UsersApi.md#deleteUser) | **DELETE** /api/users/{id} | Delete user |
| [**disableUser**](UsersApi.md#disableUser) | **PATCH** /api/users/{id}/disable | Disable user |
| [**enableUser**](UsersApi.md#enableUser) | **PATCH** /api/users/{id}/enable | Enable user |
| [**getAllUsers**](UsersApi.md#getAllUsers) | **GET** /api/users | Get all users |
| [**getUserById**](UsersApi.md#getUserById) | **GET** /api/users/{id} | Get user by ID |
| [**getUserByUsername**](UsersApi.md#getUserByUsername) | **GET** /api/users/username/{username} | Get user by username |
| [**updateUser**](UsersApi.md#updateUser) | **PUT** /api/users/{id} | Update user |
| [**usernameExists**](UsersApi.md#usernameExists) | **GET** /api/users/exists/{username} | Check username availability |



## changePassword

> changePassword(id, changePasswordRequest)

Change password

Changes user password after verifying current password

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.UsersApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        UsersApi apiInstance = new UsersApi(defaultClient);
        Long id = 56L; // Long | 
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest(); // ChangePasswordRequest | 
        try {
            apiInstance.changePassword(id, changePasswordRequest);
        } catch (ApiException e) {
            System.err.println("Exception when calling UsersApi#changePassword");
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
| **changePasswordRequest** | [**ChangePasswordRequest**](ChangePasswordRequest.md)|  | |

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
| **204** | Password changed successfully |  -  |
| **400** | Invalid password - INVALID_PASSWORD |  -  |
| **404** | User not found - USER_NOT_FOUND |  -  |
| **500** | Internal server error |  -  |


## createUser

> UserResponse createUser(createUserRequest)

Create user

Creates a new user account with encrypted password

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.UsersApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        UsersApi apiInstance = new UsersApi(defaultClient);
        CreateUserRequest createUserRequest = new CreateUserRequest(); // CreateUserRequest | 
        try {
            UserResponse result = apiInstance.createUser(createUserRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling UsersApi#createUser");
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
| **createUserRequest** | [**CreateUserRequest**](CreateUserRequest.md)|  | |

### Return type

[**UserResponse**](UserResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **201** | User created successfully |  -  |
| **400** | Invalid user data |  -  |
| **409** | Username already exists - USER_ALREADY_EXISTS |  -  |
| **500** | Internal server error |  -  |


## deleteUser

> deleteUser(id)

Delete user

Permanently deletes a user account

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.UsersApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        UsersApi apiInstance = new UsersApi(defaultClient);
        Long id = 56L; // Long | 
        try {
            apiInstance.deleteUser(id);
        } catch (ApiException e) {
            System.err.println("Exception when calling UsersApi#deleteUser");
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
| **204** | User deleted successfully |  -  |
| **404** | User not found - USER_NOT_FOUND |  -  |
| **500** | Internal server error |  -  |


## disableUser

> UserResponse disableUser(id)

Disable user

Disables a user account

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.UsersApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        UsersApi apiInstance = new UsersApi(defaultClient);
        Long id = 56L; // Long | 
        try {
            UserResponse result = apiInstance.disableUser(id);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling UsersApi#disableUser");
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

[**UserResponse**](UserResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | User disabled successfully |  -  |
| **404** | User not found - USER_NOT_FOUND |  -  |
| **500** | Internal server error |  -  |


## enableUser

> UserResponse enableUser(id)

Enable user

Enables a user account

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.UsersApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        UsersApi apiInstance = new UsersApi(defaultClient);
        Long id = 56L; // Long | 
        try {
            UserResponse result = apiInstance.enableUser(id);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling UsersApi#enableUser");
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

[**UserResponse**](UserResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | User enabled successfully |  -  |
| **404** | User not found - USER_NOT_FOUND |  -  |
| **500** | Internal server error |  -  |


## getAllUsers

> List&lt;UserResponse&gt; getAllUsers()

Get all users

Retrieves all registered users (passwords excluded)

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.UsersApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        UsersApi apiInstance = new UsersApi(defaultClient);
        try {
            List<UserResponse> result = apiInstance.getAllUsers();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling UsersApi#getAllUsers");
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

[**List&lt;UserResponse&gt;**](UserResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Successfully retrieved users |  -  |
| **500** | Internal server error |  -  |


## getUserById

> UserResponse getUserById(id)

Get user by ID

Retrieves a specific user by their ID

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.UsersApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        UsersApi apiInstance = new UsersApi(defaultClient);
        Long id = 56L; // Long | 
        try {
            UserResponse result = apiInstance.getUserById(id);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling UsersApi#getUserById");
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

[**UserResponse**](UserResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Successfully retrieved user |  -  |
| **404** | User not found - USER_NOT_FOUND |  -  |
| **500** | Internal server error |  -  |


## getUserByUsername

> UserResponse getUserByUsername(username)

Get user by username

Retrieves a specific user by their username

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.UsersApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        UsersApi apiInstance = new UsersApi(defaultClient);
        String username = "username_example"; // String | 
        try {
            UserResponse result = apiInstance.getUserByUsername(username);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling UsersApi#getUserByUsername");
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
| **username** | **String**|  | |

### Return type

[**UserResponse**](UserResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Successfully retrieved user |  -  |
| **404** | User not found - USER_NOT_FOUND |  -  |
| **500** | Internal server error |  -  |


## updateUser

> UserResponse updateUser(id, updateUserRequest)

Update user

Updates user information. All fields are optional.

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.UsersApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        UsersApi apiInstance = new UsersApi(defaultClient);
        Long id = 56L; // Long | 
        UpdateUserRequest updateUserRequest = new UpdateUserRequest(); // UpdateUserRequest | 
        try {
            UserResponse result = apiInstance.updateUser(id, updateUserRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling UsersApi#updateUser");
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
| **updateUserRequest** | [**UpdateUserRequest**](UpdateUserRequest.md)|  | |

### Return type

[**UserResponse**](UserResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | User updated successfully |  -  |
| **400** | Invalid user data |  -  |
| **404** | User not found - USER_NOT_FOUND |  -  |
| **409** | Username already exists - USER_ALREADY_EXISTS |  -  |
| **500** | Internal server error |  -  |


## usernameExists

> Boolean usernameExists(username)

Check username availability

Checks if a username is already taken

### Example

```java
// Import classes:
import com.amithfernando.qrseatreservation.client.invoker.ApiClient;
import com.amithfernando.qrseatreservation.client.invoker.ApiException;
import com.amithfernando.qrseatreservation.client.invoker.Configuration;
import com.amithfernando.qrseatreservation.client.invoker.models.*;
import com.amithfernando.qrseatreservation.client.api.UsersApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        UsersApi apiInstance = new UsersApi(defaultClient);
        String username = "username_example"; // String | 
        try {
            Boolean result = apiInstance.usernameExists(username);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling UsersApi#usernameExists");
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
| **username** | **String**|  | |

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
| **200** | Successfully checked username |  -  |
| **500** | Internal server error |  -  |

