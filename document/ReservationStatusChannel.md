# Reservation Status Channel

## Description

- Publish: Business Server
- Subscriber: Driver
- This channel is used to notify reservation status when driver is arrived at parking facility, leaving or grace period is timed out. 

## Publish

### Topic

```
/reservation/%d
```

### Body

### When car is arrived and passed the entry gate #

```
{
  "id":68,
  "slot_id":2,
  "slot_number":2,
  "fee":10.75,
  "fee_unit":60,
  "expiration_ts":1466628600,
  "begin_ts":1466628560,
  "end_ts":null,
  "revenue":null,
  "controller_physical_id":"1",
  "transaction":1
}
```

### When car is leaving and passed the exit gate #

```
{
  "id":68,
  "slot_id":2,
  "slot_number":2,
  "fee":10.75,
  "fee_unit":60,
  "expiration_ts":1466628600,
  "begin_ts":1466628560,
  "end_ts":1466628792,
  "revenue":43.00,
  "controller_physical_id":"1",
  "transaction":0
}
```

### When grace period is timed out #

```
{
 'expired': 1
}
```
