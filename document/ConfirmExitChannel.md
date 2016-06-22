# Description

- Request: Facility
- Respond: Business Server
- This channel is used to request to open exit gate and charge on driver's credit card. 

# Request

## Topic

```
/controller/{physical_id}/confirm_exit/request/{req_id}
```

## Body
```
{
  'slot_no' : 1
}
```

# Response

## Topic

```
/controller/{physical_id}/confirm_exit/response/{req_id}
```
- Subscribe : 
   (Server side) /controller/+/confirm_exit/#
   (Client side) /controller/{physical_id}/confirm_exit/response/#
   
## Body

### Success

```
{
  'success': 1
}
```

### Failed

```
{
  'success': 0,
  'cause': 'INVALID_CARD_INFORMATION'
}
```

```
{
  'success': 0,
  'cause': 'INTERNAL_SERVER_ERROR' // internal server error
}
```

# Note

If you use MQTT connection, following key/value is added in body automatically while exchanging data.
But when you received the message from channel, it does not exist.
Hence, you have to add and remove following pair to communicate with other library or entity. i.e. Arduino MQTT library.
If there is no this pair, the received element regards it as notification.


## Additional body for MQTT

### Notification (Publish/Subscribe) type message
```
{
  '_msg_type_' : 0 // Notification for publish message
}
```

### Request/Response type message
```
{
  '_msg_type_' : 1, or 2 // 1 : Request, 2 : Response.
}
```
