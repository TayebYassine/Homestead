# Frequently Asked Questions

## Quick Answers

??? question "I can't use any commands!"

    You haven't set up permissions correctly. Check that you've added the recommended permissions to your default group in LuckPerms:

    ```
    homestead.commands.region
    homestead.commands.region.*
    homestead.commands.claim
    homestead.commands.unclaim
    ```

    Make sure groups inherit permissions properly — VIP must inherit from default.

??? question "WorldGuard is overriding `/region` command"

    Use the aliases instead: `/homestead`, `/hs`, or `/rg`.

??? question "I always hit the claim limit"

    Check your limits method in `limits.yml`:

    - **static**: Non-OP players all share the same limits; OP players get different limits
    - **groups**: Requires a permissions plugin. Any group not defined gets **zero** limits
    - **permissions**: Uses permission nodes instead of group names

    Read more: [Ranks & Limits](../Configuration/Ranks and Limits.md)

??? question "How do I reload the config after changes?"

    ```
    /hsadmin reload
    ```

    Some settings (upkeep, taxes) require a full server restart.

??? question "Can I migrate from another claiming plugin?"

    Yes! Homestead supports importing from GriefPrevention, ClaimChunk, LandLord4, Lands, and HuskClaims.

    ```
    /hsadmin import [plugin-name]
    ```

    Read more: [Database Migration](../Configuration/Database Migration.md)

??? question "Players can't see borders"

    Check:

    1. The border display type in `regions.yml` — try switching from `particles` to `blocks`
    2. Some resource packs disable particles
    3. Minecraft accessibility settings may have particles disabled

??? question "Can I translate Homestead to another language?"

    Yes! Copy `en-US.yml` from `plugins/Homestead/languages/`, translate the messages, and update the `language` setting in `config.yml`.

    Read more: [Language](../Advanced/Language.md)

??? question "How do I change the database?"

    1. Fill in the new provider's connection details in `config.yml`
    2. Run `/hsadmin reload`
    3. Run `/hsadmin export [provider]`
    4. Stop the server
    5. Update `config.yml` with the new provider name
    6. Start the server

??? question "Can I disable claiming in certain worlds?"

    Yes. In `regions.yml`, use:

    ```yaml
    disabled-worlds-exact:
      - "world_the_end"
    disabled-worlds-pattern:
      - "em_*"
    ```

??? question "The plugin updated and my config broke!"

    Homestead includes a config migrator that automatically updates old configs on startup. If you're still having issues, delete the config file and let Homestead regenerate it, then re-apply your customizations.

??? question "My region bank is empty — how do I add money?"

    ```
    /region deposit [amount]
    ```

    Money comes from your personal balance (requires Vault/economy).

---

[Still need help? Join our Discord](https://discord.gg/uh7gqDY6sz)
