# Login Channel

## Description

- Request: Driver, Attendant and Owner
- Respond: Business Server
- This channel is used to login into the system.

## Request

### Topic

```
/login
```

### Body

```
{
  'email' : 'email@adress.com'
  'password' : 'password'
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
  'id' : 39,
  'card_number' : 'D33E764D6937B8A2DB6FFB46BB8A35C8C65BFAFCD68D35CF43955297A8831EAD', // driver only
  'card_expiration' : '6C9F8F63B25F1596E8570910B7847ECA // driver only
  'session_key' : 'c3767482-e3a3-448e-9659-6049fb'
}
```

#### Failed

```
{
  'success': 0,
  'cause': 'INVALID_EMAIL_PASSWORD'
}
```

```
{
  'success': 0,
  'cause': 'INTERNAL_SERVER_ERROR'
}
```
