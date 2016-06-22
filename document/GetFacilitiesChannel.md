# Description

- Request: Attendant or Owner
- Respond: Business Server
- This channel is used to request facility list which is assigned to attendant or belong to owner. 

# Request

## Topic

```
/facilities/get
```

## Body
```
{
   'session_key' : 'xxxxxx' // owner's or attendant's session key
}
```

# Response

## Topic

```
/facilities/get/#
```

## Body

### Success

```
{
  'success': 1
  'facilities' : [
    {
      "id":1,
      "name":"Shadyside Parking Lot",
      "fee":5.75,
      "fee_unit":3600,
      "grace_period":1800
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
