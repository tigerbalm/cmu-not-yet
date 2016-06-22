# Description

- Request: Attendant
- Respond: Business Server
- This channel is used to get slot lists which is belong to a facility managed by the attendant.

# Request

## Topic

```
/facility/{facility_id}/slots/get
```

## Body
```
{
  'session_key' : 'xxxxxx' // attendant's session key
}
```

# Response

## Topic

```
/facility/+/slots/get
```

## Body

### Success

```
{
  "success": 1
  "slots" : [
    {
      "controller_id": 2,
      "controller_physical_id": "arduino2",
      "id": 5,
      "number": 1,
      "parked": 0,
      "parked_ts": null,
      "reserved": 1,
      "reservation_id": 31,
      "reservation_ts": 1466349,
      "email":"reshout@naver.com"
    },
    ...
  ]
}
```

### Failed

```
{
  'success': 0,
  'cause': 'INVALID_SESSION' // or other cause, NO_AUTHORIZATION
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
