# Make Reservation Channel

## Description

- Request: Driver
- Respond: Business Server
- This channel is used to make a reservation.

## Request

### Topic

```
/facility/{facility_id}/make_reservation
```

### Body
```
{
  'session_key' : 'session_value' // driver's session key
  'reservation_ts' : {reservation epoch / 1000}
}
```

## Response

### Topic

```
/facility/{facility_id}/make_reservation/#
```

### Body

#### Success

```
{
  'success': 1
  'id' : 1,
  'reservation_ts' : 1466460, // epoch in seconds
  'confirmation_no' : 7705,
  'user_id' : 1,
  'user_email' : "reshout@gmail.com",
  'slot_id' : 3,
  'slot_no' : 3,
  'controller_id' : 6,
  'controller_physical_id' : "p1",
  'facility_id' : 1,
  'facility_name':"ShadySide",
  'fee' : 10.75
  'fee_unit' : 60
  'expiration_ts': 1466640
}
```

#### Failed

```
{
  'success': 0,
  'cause': 'INVALID_SESSION'
}
```

```
{
  "success": 0,
  "cause": "NO_AUTHORIZATION",
}
```

```
{
  "success": 0,
  "cause": "NO_AVAILABLE_SLOT",
}
```

```
{
  'success': 0,
  'cause': 'INTERNAL_SERVER_ERROR'
}
```
