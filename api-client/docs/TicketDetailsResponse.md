

# TicketDetailsResponse

Detailed information about a ticket

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**ticketNo** | **String** | Ticket number |  [optional] |
|**ticketType** | [**TicketTypeEnum**](#TicketTypeEnum) | Ticket type (FULL or HALF) |  [optional] |
|**reservationStatus** | [**ReservationStatusEnum**](#ReservationStatusEnum) | Current reservation status |  [optional] |
|**canCheckIn** | **Boolean** | Whether ticket can be checked in |  [optional] |
|**checkInMessage** | **String** | Reason if check-in is not allowed |  [optional] |
|**seller** | [**SellerInfo**](SellerInfo.md) | Seller information |  [optional] |
|**seat** | [**SeatInfo**](SeatInfo.md) | Seat information |  [optional] |
|**reservationReferenceNo** | **String** | Reservation reference number |  [optional] |
|**reservationCreatedAt** | **OffsetDateTime** | Reservation creation time |  [optional] |



## Enum: TicketTypeEnum

| Name | Value |
|---- | -----|
| FULL | &quot;FULL&quot; |
| HALF | &quot;HALF&quot; |



## Enum: ReservationStatusEnum

| Name | Value |
|---- | -----|
| PAYMENT_PENDING | &quot;PAYMENT_PENDING&quot; |
| PAID | &quot;PAID&quot; |
| CHECKED_IN | &quot;CHECKED_IN&quot; |


## Implemented Interfaces

* Serializable


