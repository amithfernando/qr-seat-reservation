

# UpdateUserRequest

Request object for updating user information

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**username** | **String** | New username (optional) |  [optional] |
|**password** | **String** | New password (optional, will be encrypted) |  [optional] |
|**role** | [**RoleEnum**](#RoleEnum) | New role (optional) |  [optional] |
|**enabled** | **Boolean** | Whether the user account is enabled (optional) |  [optional] |



## Enum: RoleEnum

| Name | Value |
|---- | -----|
| ADMIN | &quot;ADMIN&quot; |
| ENTRANCE | &quot;ENTRANCE&quot; |


## Implemented Interfaces

* Serializable


