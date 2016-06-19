# Request

## Topic

```
/reservation/get
```

## Body
```
{
  'session_key' : 'xxxxx' // driver's session_key
}
```

# Response

## Topic

```
/reservation/get/#
```

## Body

### Success

```
{
  'success': 1
  'id' : 1,
  'reservation_ts' : 9999,
  'confirmation_no' : 9999,
  'user_id' : 1,
  'user_email' : "reshout@gmail.com",
  'slot_id' : 3,
  'slot_no' : 3,
  'controller_id' : 6,
  'controller_physical_id' : "p1",
  'facility_id' : 1,
  'facility_name':"ShadySide"
}
```

### Failed

```
{
  'success': 0,
  'cause': 'INVALID_SESSION' // or other cause, NO_RESERVATION_EXIST
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
