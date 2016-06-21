# Description

- Publish: Business Server
- Subscriber: Driver
- This channel is used to notify reservation status when driver is arrived at parking facility, leaving or grace period is timed out. 

# Publish

## Topic

```
/reservation/%d
```

## Body

### When car is arrived and passed the entry gate #

```
{
  'transaction': 1
}
```

### When car is leaving and passed the exit gate #

```
{
  'transaction': 0
}
```

### When grace period is timed out #

```
{
 'expired': 1
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
