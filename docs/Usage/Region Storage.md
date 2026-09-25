# Region Storage

Region storage provides a shared chest that all trusted members can access. Unlike personal chests, this storage is tied to the region. Items stay with the region even if members leave.

---

## How It Works

1. Open the shared storage GUI with `/region storage`.
2. Deposit or withdraw items like a normal chest.
3. The region owner, trusted members, and operators all share full access — no extra permissions are required per item.
4. The storage is created automatically the first time it is opened, at the size configured in `regions.yml`.

---

## Command

```
/region storage
```

Opens the shared storage GUI for your targeted region.

!!! warning "Membership Required"

    Only the region owner, trusted members, and operators can open the storage. If you are not a member, you'll be denied access.

---

## Configuration

```yaml
# In regions.yml
storage:
  enabled: false   # Feature is off by default
  size: 27         # Options: 9, 18, 27, 36, 45, or 54
```

| Size | Rows |
|:----:|:----:|
| 9 | 1 |
| 18 | 2 |
| 27 | 3 |
| 36 | 4 |
| 45 | 5 |
| 54 | 6 |

Set `enabled: true` to turn the feature on for the whole server. The `size` value must be one of the six options above (any other value is corrected automatically).

!!! tip "Use Cases"

    - Shared building materials for group projects
    - Public resources for all members
    - Emergency supplies accessible by anyone

The storage contents persist with the region and are not lost when members change.

