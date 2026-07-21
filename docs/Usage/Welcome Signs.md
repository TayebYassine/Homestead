# Welcome Signs

Welcome signs let players visit regions by interacting with a sign instead of typing commands.

## Setup

### Step 1: Place a Sign

Place any sign type inside your region.

### Step 2: Format the Sign

```
Line 1: [Welcome]
Line 2: (Region name)
Line 3: (Leave empty)
Line 4: (Leave empty)
```

**Example:**
```
[Welcome]
MyBase
```

One welcome sign per region.

## Usage

Right-click the sign to teleport to the region's spawn point.

```
/region visit [player] [index]
```

**Examples:**
```
/region visit Steve
/region visit Alex 2
```

The index selects which welcome sign to use for players with multiple regions.

## Configuration

```yaml
# In regions.yml
welcome-signs:
  enabled: false
```

!!! info "Requires Spawn"

    Works best with a region spawn set via `/region setspawn`.
