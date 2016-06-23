# Cancel Reservation Channel

## Description
- Request: Driver
- Respond: Business Server
- This channel is used to cancel parking reservation by driver. 

## Request

### Topic

```
/reservation/{reservation_id}/cancel
```

### Body
```
{
  'session_key': 'f4e7229b-2ce5-4834-86c9-ae39f1'
}
```

## Response

### Topic

```
/reservation/+/cancel/#
```

### Body

#### Success

```
{
  'success': 1
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
  'success': 0,
  'cause': 'INTERNAL_SERVER_ERROR'
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
  'cause': 'NO_RESERVATION_EXIST'
}
```
