# Welcome Signs

Welcome signs let players visit regions by interacting with a sign instead of typing commands.

---

## Setup

### Step 1: Enable the Feature

Welcome signs are disabled by default. Enable them in `regions.yml`:

```yaml
# In regions.yml
welcome-signs:
  enabled: true
```

### Step 2: Place a Sign

Place any sign type **inside a region you own**. Only the region owner can create a welcome sign; if you don't own the region, the sign breaks immediately.

### Step 3: Format the Sign

Type `[Welcome]` on the **first line** and leave the bottom two lines empty:

```
Line 1: [Welcome]
Line 2: (leave empty; filled in automatically)
Line 3: (leave empty)
Line 4: (leave empty)
```

The region name is written onto line 2 automatically. If lines 3 or 4 contain text, the sign breaks.

**Example of the finished sign:**

```
[Welcome]
MyBase
```

One welcome sign per region. Placing a new one replaces the previous registration. Breaking the welcome sign clears it, so the region no longer appears in visit menus until a new sign is placed.

---

## Usage

Right-click the sign to teleport to the region's spawn point.

You can also visit by command:

```
/region visit [player] (index)
```

**Examples:**

```
/region visit Steve
/region visit Alex 2
```

- With no arguments, `/region visit` opens a menu of every region that has a welcome sign; click an entry to teleport.
- The optional `index` is **0-based** and selects which of that player's welcome-sign regions to use when they own several (e.g. `0` for the first, `1` for the second).

---

## Configuration

```yaml
# In regions.yml
welcome-signs:
  enabled: false
```

!!! info "Requires Spawn"

    Works best with a region spawn set via `/region setspawn`. Visiting teleports you to the welcome sign's location, so make sure the sign is placed somewhere sensible.

See [Teleportation](Teleportation.md) for delayed-teleport and cooldown settings that also apply to visits.

