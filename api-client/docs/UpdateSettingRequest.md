

# UpdateSettingRequest

Request object for updating application settings

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**eventName** | **String** | Event name |  |
|**venue** | **String** | Venue location |  [optional] |
|**tableSize** | **Integer** | Table display size in pixels |  [optional] |
|**seatSize** | **Integer** | Seat display size in pixels |  [optional] |
|**noOfColumns** | **Integer** | Number of columns in seating layout |  [optional] |
|**fontSize** | **Integer** | Font size for ticket text |  [optional] |
|**qrX** | **Integer** | QR code X position on ticket |  [optional] |
|**qrY** | **Integer** | QR code Y position on ticket |  [optional] |
|**textX** | **Integer** | Text X position on ticket |  [optional] |
|**textY** | **Integer** | Text Y position on ticket |  [optional] |
|**ticketPrefix** | **String** | Ticket number prefix |  [optional] |
|**noOfDigits** | **Integer** | Number of digits in ticket number |  [optional] |
|**maxNoOfTickets** | **Integer** | Maximum number of tickets to generate |  [optional] |
|**baseImageBase64** | **String** | Base image for ticket generation (Base64 encoded) |  [optional] |


## Implemented Interfaces

* Serializable


