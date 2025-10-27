

# UserResponse

User information response (password excluded)

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**id** | **Long** | User ID |  [optional] |
|**username** | **String** | Username |  [optional] |
|**role** | [**RoleEnum**](#RoleEnum) | User role |  [optional] |
|**enabled** | **Boolean** | Whether the user account is enabled |  [optional] |
|**createdAt** | **OffsetDateTime** | Account creation timestamp |  [optional] |



## Enum: RoleEnum

| Name | Value |
|---- | -----|
| ADMIN | &quot;ADMIN&quot; |
| ENTRANCE | &quot;ENTRANCE&quot; |


## Implemented Interfaces

* Serializable


