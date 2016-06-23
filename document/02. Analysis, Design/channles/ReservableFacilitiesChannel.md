# Get Reservable Facilities Channel

## Description

- Request: Driver
- Respond: Business Server
- This channel is used to get all facility list which is available to make a reservation.

## Request

### Topic

```
/facilities/reservable_list
```

### Body
```
{
   'session_key' : 'xxxxxx' // driver's session key
}
```

## Response

### Topic

```
/facilities/reservable_list/#
```

### Body

#### Success

```
{
  'success': 1
  'facilities' : [
    {
      'id' : 1,
      'name' : 'Shady Side Parking Lot'
    },
    {
      ...
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
