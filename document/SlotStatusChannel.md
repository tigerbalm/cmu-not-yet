# Description

- Request: Controller
- Respond: Business Server, Attendant
- This channel is used to notify slot status when is occupied or empty. 

# Publish

## Topic

```
/controller/{physical_id}/slot/{slot_number}
```

## Body

### When car is parked at slot #

```
{
  'parked': 1,
  'confirmation_no': 5123
}
```

### When car is leaving from slot #

```
{
  'parked': 0,
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
