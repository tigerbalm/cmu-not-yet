# Publish

## Topic

```
/controller/{controller_physical_id}
```

## Body

### When connected

```
{
  'available': 1,
  'slots': [ { 'number': 1, 'occpuied': 0 },
             { 'number': 2, 'occupied': 1 }, ... ]
}
```

### When disconnected (Will Message)

```
{
  'available': 0
}
```
