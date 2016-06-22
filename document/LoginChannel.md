# Description

- Request: Driver, Attendant and Owner
- Respond: Business Server
- This channel is used to login into the system.

# Request

## Topic

```
/login
```

## Body

```
{
  'email' : 'email@adress.com'
  'password' : 'password'
}
```

# Response

## Topic

```
/login/#
```

## Body

### Success

```
{
  'success': 1
  'id' : 1,
  'card_number' : '1234567890123456', // driver only
  'card_expiration' : '01/20' // driver only
  'session_key' : 'session Key'
}
```

### Failed

```
{
  'success': 0,
  'cause': 'INVALID_EMAIL_PASSWORD'
}
```

```
{
  'success': 0,
  'cause': 'ALREADY_LOGIN'
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
