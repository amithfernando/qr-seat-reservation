

# CreateUserRequest

Request object for creating a new user

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**username** | **String** | Username (must be unique) |  |
|**password** | **String** | Password (will be encrypted) |  |
|**role** | [**RoleEnum**](#RoleEnum) | User role |  |
|**enabled** | **Boolean** | Whether the user account is enabled |  [optional] |



## Enum: RoleEnum

| Name | Value |
|---- | -----|
| ADMIN | &quot;ADMIN&quot; |
| ENTRANCE | &quot;ENTRANCE&quot; |


## Implemented Interfaces

* Serializable


