# Sign Up Channel

## Description

- Request: Driver
- Respond: Business Server
- This channel is used to make a new account.

## Request

### Topic

```
/user/sign_up
```

### Body
```
{
  'email': 'reshout@gmail.com',
  'password': 'xxxxxxxx',
  'card_number': '0000-0000-0000-0000',
  'card_expiration': '00/00' 
}
```

## Response

### Topic

```
/user/sign_up/#
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
  'cause': 'EXISTENT_USER'
}
```

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
