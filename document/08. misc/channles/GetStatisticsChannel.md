# Get Statistics Channel

## Description

- Request: Owner
- Respond: Business Server
- This channel is used to request to statistics about the owner's facilities. 

## Request

### Topic

```
/facility/statistics/get
```

### Body
```
{
  'session_key' : 'session_value' // owner's session key
  'dbquery_key' : 'sql_query // SQL Query statement to be executed
}
```

## Response

### Topic

```
/facility/statistics/get/#
```

### Body

#### Success

```
{
  'success': 1
  'columnnames': [ 'col1', 'col2', ... ],
  'values': [ [ 'value11', 'value12', ... ], [ ... ] ] 
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
