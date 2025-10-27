

# ErrorResponse

Standard error response

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**errorCode** | **String** | Error code |  [optional] |
|**message** | **String** | Error message |  [optional] |
|**status** | **Integer** | HTTP status code |  [optional] |
|**timestamp** | **OffsetDateTime** | Timestamp of the error |  [optional] |
|**path** | **String** | Request path where the error occurred |  [optional] |
|**validationErrors** | [**List&lt;ValidationError&gt;**](ValidationError.md) | Validation errors (if applicable) |  [optional] |


## Implemented Interfaces

* Serializable


