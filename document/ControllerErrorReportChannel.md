# Controller Error Report Channel

## Description

- Publish: Facility
- Subscribe: Attendant
- This channel is used to notify controller's error status to attendant

## Publish

### Topic

```
/controller/{controller_physical_id}/error
```

### Body

```
{
  'message': error message
}
```
