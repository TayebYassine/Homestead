# Rewards

The rewards system gives players bonus chunks and sub-areas based on their activity and community engagement. This encourages longer playtime and cooperation while expanding claiming capabilities.

## How Rewards Work

Rewards are **added on top** of base limits from [Ranks and Limits](./Ranks%20and%20Limits.md).

**Example:**

- Base limit: 8 chunks
- Reward for 3 members: +6 chunks (2 per member)
- Reward for 10 hours playtime: +4 chunks
- **Total available**: 18 chunks

Rewards help active players expand their regions without requiring rank purchases.

## Types of Rewards

### Rewards for Trusted Members

Reward region owners for building a community by trusting other players.

### Rewards by Playtime

Reward players for staying active on the server with increasing bonuses over time.

## Member Rewards

### How It Works

Every time someone joins your region as a trusted member, you receive bonus chunks and/or sub-areas.

**Example:**

- You trust PlayerA → +2 chunks
- You trust PlayerB → +2 chunks (total: +4)
- You trust PlayerC → +2 chunks (total: +6)

### Configuration

```yaml
rewards:
  for-each-member:
    chunks: 2      # Chunks gained per member (0 = disabled)
    subareas: 1    # Sub-areas gained per member (0 = disabled)
```

### Example Scenarios

**Disabled:**
```yaml
for-each-member:
  chunks: 0
  subareas: 0
```
No rewards for trusting members.

**Moderate Rewards:**
```yaml
for-each-member:
  chunks: 2
  subareas: 1
```

- 3 members = +6 chunks, +3 sub-areas
- 5 members = +10 chunks, +5 sub-areas

**Generous Rewards:**
```yaml
for-each-member:
  chunks: 5
  subareas: 2
```

- 3 members = +15 chunks, +6 sub-areas
- 5 members = +25 chunks, +10 sub-areas

### Important Notes

**Rewards go to the region owner**, not the members themselves. This encourages owners to build active communities.

**Members must be currently trusted.** If you untrust someone, you lose those reward chunks.

**Respects maximum member limits.** You can't trust more members than your rank allows, even for rewards.

## Playtime Rewards

### How It Works

The longer a player has been active on the server, the more bonus chunks they earn. Rewards are based on **total playtime**, not session time.

**Example progression:**

- Play for 30 minutes → +1 chunk
- Play for 3 hours total → +2 chunks
- Play for 1 day total → +5 chunks
- And so on...

### Configuration

```yaml
rewards:
  by-playtime:
    - minutes: 30
      hours: 0
      days: 0
      chunks: 1
      subareas: 1
      
    - minutes: 0
      hours: 3
      days: 0
      chunks: 2
      subareas: 2
      
    - minutes: 0
      hours: 12
      days: 0
      chunks: 4
      subareas: 3
      
    - minutes: 0
      hours: 0
      days: 1
      chunks: 6
      subareas: 4
      
    - minutes: 0
      hours: 0
      days: 3
      chunks: 10
      subareas: 6
      
    - minutes: 0
      hours: 0
      days: 7
      chunks: 15
      subareas: 8
```

### Understanding Time Format

Each tier has three time components that **add together**:

```yaml
- minutes: 30  # 30 minutes
  hours: 2     # + 2 hours
  days: 1      # + 1 day
  # Total: 1 day, 2 hours, 30 minutes
```

**Common time conversions:**

- 1 hour = 60 minutes
- 1 day = 24 hours = 1440 minutes
- 1 week = 7 days = 168 hours

### Designing Progression

**Short-term Rewards (First Hour):**
```yaml
# Immediate gratification for new players
- minutes: 15
  hours: 0
  days: 0
  chunks: 1
  subareas: 0
  
- minutes: 30
  hours: 0
  days: 0
  chunks: 2
  subareas: 1
  
- minutes: 0
  hours: 1
  days: 0
  chunks: 3
  subareas: 1
```

**Medium-term Rewards (First Week):**
```yaml
# Regular player milestones
- hours: 5
  chunks: 5
  
- hours: 12
  chunks: 8
  
- days: 1
  chunks: 12
  
- days: 3
  chunks: 16
  
- days: 7
  chunks: 20
```

**Long-term Rewards (Weeks/Months):**
```yaml
# Veteran player rewards
- days: 14
  chunks: 25
  
- days: 30
  chunks: 35
  
- days: 60
  chunks: 50
  
- days: 90
  chunks: 75
```

### Progression Rules

**Highest tier wins:**  
Homestead checks **all** tiers and gives you rewards from the **highest** tier you qualify for, not cumulative.

**Example:**
```yaml
- hours: 1
  chunks: 3
  
- hours: 5
  chunks: 8
  
- hours: 10
  chunks: 15
```

If you have 7 hours playtime:

- ✓ Qualified for 1 hour tier
- ✓ Qualified for 5 hour tier
- ✗ Not qualified for 10 hour tier yet

**You get: 8 chunks** (from the 5-hour tier)

**Not: 3 + 8 = 11 chunks** (they don't stack)

!!! warning "Design Your Tiers Carefully"

    Make sure each tier gives **more** rewards than the previous one. Don't decrease rewards as time increases!

**Bad progression:**
```yaml
- hours: 1
  chunks: 10  # More than next tier!
  
- hours: 5
  chunks: 5   # Less than previous! Bad!
```

**Good progression:**
```yaml
- hours: 1
  chunks: 5
  
- hours: 5
  chunks: 10  # Always increasing
  
- hours: 10
  chunks: 20  # Keeps going up
```

## Balancing Rewards

### Finding the Right Balance

**Consider:**

1. **Base limits**: How many chunks do players start with?
2. **Server size**: More players = more conservative rewards
3. **World size**: Larger worlds can afford generous rewards
4. **Server type**: Creative servers can be more generous
5. **Player retention goals**: Higher rewards = more reason to stay

### Calculating Total Possible

**Example calculation:**

- Base limit (from rank): 8 chunks
- Maximum members: 5 players
- Member reward: 2 chunks per member
- Highest playtime tier: +20 chunks

**Maximum total:**

- Base: 8
- Members: 5 × 2 = +10
- Playtime: +20
- **Total**: 38 chunks possible

### Preventing Overflow

Make sure the maximum possible chunks doesn't:
- Let players claim unreasonably large areas
- Cause lag from too many protected chunks
- Make the world fill up too quickly

**Recommended maximum totals:**

- Small servers: 30-50 chunks
- Medium servers: 20-40 chunks
- Large servers: 15-30 chunks
