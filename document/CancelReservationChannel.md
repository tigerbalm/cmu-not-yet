# Description

- Request: Driver
- Respond: Business Server
- This channel is used to cancel parking reservation by driver. 

# Request

## Topic

```
/reservation/{reservation_id}/cancel
```

## Body
```
{
  'session_key' : 'session_value' // driver's session key
}
```

# Response

## Topic

```
/reservation/+/cancel/#
```

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
  'cause': 'INVALID_SESSION' // or other cause
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
