# Get Reservation Channel

## Description

- Request: Driver
- Respond: Business Server
- This channel is used to get reservation information. 

## Request

### Topic

```
/reservation/get
```

### Body
```
{
  'session_key' : 'xxxxx' // driver's session_key
}
```

## Response

### Topic

```
/reservation/get/#
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
  'begin_ts' :'null'
  'end_ts':'null'
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
  'success': 0,
  'cause': 'INTERNAL_SERVER_ERROR'
}
```

```
{
  'success': 0,
  'cause': 'NO_RESERVATION_EXIST'
}
```
