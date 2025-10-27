

# TicketResponse

Ticket information response

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**id** | **Long** | Ticket ID |  [optional] |
|**ticketNo** | **String** | Ticket number |  [optional] |
|**status** | [**StatusEnum**](#StatusEnum) | Ticket status |  [optional] |
|**hasImage** | **Boolean** | Whether ticket has image data |  [optional] |
|**imageSizeBytes** | **Integer** | Image data size in bytes |  [optional] |
|**createdAt** | **OffsetDateTime** | Creation timestamp |  [optional] |
|**updatedAt** | **OffsetDateTime** | Last update timestamp |  [optional] |



## Enum: StatusEnum

| Name | Value |
|---- | -----|
| AVAILABLE | &quot;AVAILABLE&quot; |
| USED | &quot;USED&quot; |
| CANCELLED | &quot;CANCELLED&quot; |


## Implemented Interfaces

* Serializable


