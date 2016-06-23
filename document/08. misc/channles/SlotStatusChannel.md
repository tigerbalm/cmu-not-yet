# Slot Status Channel

## Description

- Publish: Controller
- Subscriber: Business Server, Attendant
- This channel is used to notify slot status when is occupied or empty. 

## Publish

### Topic

```
/controller/{physical_id}/slot/{slot_number}
```

### Body

### When car is parked at slot #

```
{
  'parked': 1,
  'confirmation_no': 5123
}
```

### When car is leaving from slot #

```
{
  'parked': 0,
}
```
