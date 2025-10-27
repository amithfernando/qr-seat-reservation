

# ReservationResponse

Reservation response with all details

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**id** | **Long** | Reservation ID |  [optional] |
|**referenceNo** | **String** | Unique reference number |  [optional] |
|**seller** | [**SellerInfo**](SellerInfo.md) | Seller information |  [optional] |
|**seatReservations** | [**List&lt;SeatReservationInfo&gt;**](SeatReservationInfo.md) | List of reserved seats |  [optional] |
|**description** | **String** | Reservation description |  [optional] |
|**status** | [**StatusEnum**](#StatusEnum) | Reservation status |  [optional] |
|**createdAt** | **OffsetDateTime** | Creation timestamp |  [optional] |
|**updatedAt** | **OffsetDateTime** | Last update timestamp |  [optional] |



## Enum: StatusEnum

| Name | Value |
|---- | -----|
| PAYMENT_PENDING | &quot;PAYMENT_PENDING&quot; |
| PAID | &quot;PAID&quot; |
| CHECKED_IN | &quot;CHECKED_IN&quot; |


## Implemented Interfaces

* Serializable


