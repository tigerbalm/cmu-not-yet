# Description

- Request: Driver
- Respond: Business Server
- This channel is used to make a new account.

# Request

## Topic

```
/user/sign_up
```

## Body
```
{
  'email': 'reshout@gmail.com',
  'password': 'xxxxxxxx',
  'card_number': '0000-0000-0000-0000',
  'card_expiration': '00/00' 
}
```

# Response

## Topic

```
/user/sign_up/#
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
  'cause': 'EXISTENT_USER'
}
```

```
{
  'success': 0,
  'cause': 'INVALID_CARD_INFORMATION'
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
