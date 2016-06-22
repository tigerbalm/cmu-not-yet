# Confirm Exit Channel

## Description

- Request: Facility
- Respond: Business Server
- This channel is used to request to open exit gate and charge on driver's credit card. 

## Request

### Topic

```
/controller/{physical_id}/confirm_exit/request/{req_id}
```

### Body
```
{
  'slot_no' : 1
}
```

## Response

### Topic

```
/controller/{physical_id}/confirm_exit/response/{req_id}
```

#### Subscribe
- (Server side) `/controller/+/confirm_exit/#`
- (Client side) `/controller/{physical_id}/confirm_exit/response/#`
   
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
  'cause': 'INVALID_CARD_INFORMATION'
}
```

```
{
  'success': 0,
  'cause': 'INTERNAL_SERVER_ERROR'
}
```
