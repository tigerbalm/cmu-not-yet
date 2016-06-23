# Confirm Reservation Channel

## Description

- Request: Facility
- Respond: Business Server
- This channel is used to request to validate reservation number and open entry gate when a car is arrived at entry gate. 

## Request

### Topic

```
/controller/{physical_id}/confirm_reservation/request/{req_id}
```

### Body
```
{
  'confirmation_no' : 8341, /* 4 digits */
}
```

## Response

### Topic

```
/controller/{physical_id}/confirm_reservation/response/{req_id}
```

- Subscribe : 
   (Server side) /controller/+/confirm_reservation/#
   (Client side) /controller/{physical_id}/confirm_reservation/response/#
   
### Body

#### Success

```
{
  'success': 1
  'slot_no': 1
}
```

#### Failed

```
{
  'success': 0,
  'cause': 'INVALID_CONFIRMATION_NO'
}
```

```
{
  'success': 0,
  'cause': 'INTERNAL_SERVER_ERROR'
}
```
