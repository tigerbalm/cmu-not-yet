# Get Facilities Channel

## Description

- Request: Attendant or Owner
- Respond: Business Server
- This channel is used to request facility list which is assigned to attendant or belong to owner. 

## Request

### Topic

```
/facilities/get
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
/facilities/get/#
```

### Body

#### Success

```
{
  'success': 1
  'facilities' : [
    {
      "id":1,
      "name":"Shadyside Parking Lot",
      "fee":5.75,
      "fee_unit":3600,
      "grace_period":1800
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
