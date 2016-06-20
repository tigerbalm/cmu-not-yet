# Request

## Topic

```
/facility/{facility_id}/request
```

## Body

```
{
  "name": "Shadyside Parking Lot",
  "fee": 3.50,
  "fee_unit": 3600, // second
  "grace_period": 1800 // second
}
```

# Response

## Topic

```
/facility/{facility_id}/response/#
```

## Body

### Success

```
{
  "success": 1
}
```

### Failed

```
{
  "success": 0,
  "cause": "NO_AUTHORIZATION",
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
