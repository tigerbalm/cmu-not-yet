# Get Slots Channel

## Description

- Request: Attendant
- Respond: Business Server
- This channel is used to get slot lists which is belong to a facility managed by the attendant.

## Request

### Topic

```
/facility/{facility_id}/slots/get
```

### Body
```
{
  'session_key' : 'xxxxxx' // attendant's session key
}
```

## Response

### Topic

```
/facility/+/slots/get
```

### Body

#### Success

```
{
  "success": 1
  "slots" : [
    {
      "activated" : null
      "controller_id": 2,
      "controller_physical_id": "arduino2",
      "available" : 1
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
