

# SeatDetail


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**id** | **Long** |  |  [optional] |
|**seatNo** | **String** |  |  [optional] |
|**seatStatus** | [**SeatStatusEnum**](#SeatStatusEnum) |  |  [optional] |
|**tableDetail** | [**TableDetail**](TableDetail.md) |  |  [optional] |
|**createdAt** | **OffsetDateTime** |  |  [optional] |
|**updatedAt** | **OffsetDateTime** |  |  [optional] |
|**createdBy** | **String** |  |  [optional] |
|**updatedBy** | **String** |  |  [optional] |
|**available** | **Boolean** |  |  [optional] |
|**noWithStatus** | **String** |  |  [optional] |
|**unavailable** | **Boolean** |  |  [optional] |
|**checkedIn** | **Boolean** |  |  [optional] |
|**reserved** | **Boolean** |  |  [optional] |



## Enum: SeatStatusEnum

| Name | Value |
|---- | -----|
| AVAILABLE | &quot;AVAILABLE&quot; |
| RESERVED | &quot;RESERVED&quot; |
| UNAVAILABLE | &quot;UNAVAILABLE&quot; |
| CHECKED_IN | &quot;CHECKED_IN&quot; |


## Implemented Interfaces

* Serializable


