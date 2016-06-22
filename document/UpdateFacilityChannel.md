# Update Facility Channel

## Description

- Request: Owner
- Respond: Business Server
- This channel is used to configure parking fee and grace period time.

## Request

### Topic

```
/facility/{facility_id}/update
```

### Body

```
{
  "name": "Shadyside Parking Lot",
  "fee": 3.50,
  "fee_unit": 3600, // second
  "grace_period": 1800 // second
}
```

## Response

### Topic

```
/facility/{facility_id}/update/#
```

### Body

#### Success

```
{
  "success": 1
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
