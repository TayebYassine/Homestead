# Region Storage

Region storage provides a shared chest that all trusted members can access. Unlike personal chests, this storage is tied to the region — members don't need any special permissions to use it.

## How It Works

1. Open the shared storage GUI
2. Deposit or withdraw items
3. All trusted members have full access
4. Items are safe — they stay with the region

## Command

```
/region storage
```

Opens the shared storage GUI for your targeted region.

## Configuration

```yaml
# In regions.yml
storage:
  enabled: false
  size: 27       # Options: 9, 18, 27, 36, 45, or 54
```

| Size | Rows |
|:----:|:----:|
| 9 | 1 |
| 18 | 2 |
| 27 | 3 |
| 36 | 4 |
| 45 | 5 |
| 54 | 6 |

!!! tip "Use Cases"

    - Shared building materials for group projects
    - Public resources for all members
    - Emergency supplies accessible by anyone
