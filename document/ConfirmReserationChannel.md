# Request

## Topic

```
/controller/{physical_id}/confirm_reservation/request/_id
```

## Body
```
{
  'confirmation_no' : 8341, /* 4 digits */
}
```

# Response

## Topic

```
/controller/+/confirm_reservation/#
```

## Body

### Success

```
{
  'success': 1
  'slot_no': 1
}
```

### Failed

```
{
  'success': 0,
  'cause': 'INVALID_CONFIRMATION_NO' // or other casue
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