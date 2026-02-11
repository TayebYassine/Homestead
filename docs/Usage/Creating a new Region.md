# Creating a New Region

Creating a region is the first step to protecting your builds and land in Homestead. This guide will walk you through the entire process from start to finish.

## What is a Region?

A **region** is your protected territory made up of claimed chunks. Inside your region:

- Only you and trusted members can build
- You control who can access containers, doors, and other interactions
- You can customize permissions with flags
- Your builds are safe from griefing

## **Step 1:** Create Your Region

Before claiming any chunks, you need to create a region.

### Command

```
/region create [region-name]
```

**Example:**
```
/region create MyBase
```

### What Happens Next?

When you create a region:

1. An empty region is created with your chosen name
2. You become the region owner
3. The region starts with default flags from the server configuration
4. No chunks are claimed yet, you need to do this manually

## **Step 2:** Claim Chunks

Now that you have a region, it's time to claim the chunks where you want to build.

### What is a Chunk?

A **chunk** is a 16×16 block area that extends from bedrock to the sky. When you claim a chunk, you protect everything in that 16×16 column.

Use **F3 + G** to view chunk borders, or use `/region borders` if you want to see claimed chunks!

### How to Claim

1. **Stand in the chunk** you want to claim
2. **Make sure the region is targeted** (see below)
3. **Run the claim command:**
   ```
   /claim
   ```

### Targeting Your Region

Commands like `/claim` work on your **targeted region**. By default, your first region is automatically targeted.

**To target a specific region:**
```
/region set target [region-name]
```

**Example:**
```
/region set target MyBase
```

Now when you use `/claim`, it will claim chunks for your "MyBase" region.

### Claiming Multiple Chunks

To protect a larger area, claim adjacent chunks:

1. Stand in the first chunk
2. `/claim`
3. Move to the next chunk
4. `/claim`
5. Repeat until your area is protected

### Checking Your Claims

**View claimed chunks in a menu:**
```
/region claimlist
```

This opens a GUI showing:

- All chunks you've claimed
- Chunk coordinates
- Options to unclaim from the menu

**View region information:**
```
/region info
/region info [region name]
```

## Understanding Chunk Limits

Every player has a limit on how many chunks they can claim. This depends on:

1. **Your rank/group** (configured by server admins)
2. **Rewards** for trusted members
3. **Playtime rewards** for active players

## **Step 3:** Unclaiming Chunks

Made a mistake or need to reorganize? You can unclaim chunks.

### Unclaim Command

**Stand in the chunk and run:**
```
/unclaim
```

The chunk is immediately removed from your region.

### Unclaim from Menu

**For remote unclaiming:**
```
/region claimlist
```

1. Opens a GUI with all your chunks
2. Hover on a chunk button to view details
3. Left-click the unclaim the chunk

!!! warning "Sub-Areas Warning"

    If you unclaim a chunk that contains a sub-area (or part of one), that sub-area will be **permanently deleted**. Make sure you don't need the sub-area before unclaiming!

## **Step 4:** Verify Your Protection

After claiming chunks, test that protection is working:

1. **Exit your region** (walk outside claimed chunks)
2. **Log in with an alt account (if allowed)** or ask a friend to help
3. **Try to break a block** in your claimed area
4. **They should be denied** (if they're not trusted)

If they can build:

- Check that the chunk is actually claimed with `/region claimlist`
- Verify they're not already trusted with `/region members`
- Make sure flags are set correctly with `/region flags`

## Next Steps

Now that you've created your region and claimed chunks:

1. **Customize flags**: Control what players can do
    - See: [Editing a Region](./Editing%20a%20Region.md)

2. **Trust members**: Add friends to help build
    - Use: `/region trust [player]`

3. **Create sub-areas**: Make special zones
    - See: [Sub-Areas](./Sub-Areas.md)

Ready to customize your region? Continue to [Editing a Region](./Editing%20a%20Region.md)!
