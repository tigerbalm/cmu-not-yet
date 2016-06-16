# Request

## Topic

```
/controller/{physical_id}/confirm_exit
```

## Body
```
{
  ‘slot_no’: 1
}
```

# Response

## Topic

```
/controller/+/confirm_exit/#
```

## Body

### Success

```
{
  'success': 1
}
```

### Failed

```
{
  'success': 0,
  'cause': 'INVALID_CARD_INFORMATION'
}
```
