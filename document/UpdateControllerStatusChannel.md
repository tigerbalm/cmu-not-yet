# Publish

## Topic

```
/controller/{controller_physical_id}
```

## Body

### When connected

```
{
  'available': 1,
  'slots': [ { 'number': 1, 'occupied': 0 },
             { 'number': 2, 'occupied': 1 }, ... ]
}
```

### When disconnected (Will Message)

```
{
  'available': 0
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
