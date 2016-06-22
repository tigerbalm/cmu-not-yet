# Logout Channel

## Description

- Request: Driver, Attendant and Owner
- Respond: Business Server
- This channel is used to logout from the system.

## Request

### Topic

```
/logout
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
/login/#
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
  'cause': 'INTERNAL_SERVER_ERROR'
}
```
