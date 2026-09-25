# Frequently Asked Questions

This page collects the questions Homestead users ask most often. Each answer is short and to the point, with links to the full documentation where there is more to read. If your question is not listed here, join our Discord (link at the bottom of the page) and ask.

---

## Quick Answers

??? question "I can't use any commands!"

    You have not set up permissions correctly. Check that you have added the recommended permissions to your `default` group in LuckPerms:

    ```
    homestead.commands.region
    homestead.commands.region.*
    homestead.commands.claim
    homestead.commands.unclaim
    ```

    Make sure groups inherit permissions properly. For example, a `vip` group must inherit from `default`, otherwise players in `vip` lose everything the default group grants. See [First Steps](First%20Steps.md) for the full setup.

??? question "How do I configure a specific region?"

    ```
    /hs set [region name]
    ```

    Once you set a region as targeted, any command or action that requires checking a region will use your targeted region. This is useful when a player belongs to more than one region and wants to act on a specific one without standing inside it.

??? question "WorldGuard is overriding the `/region` command"

    WorldGuard registers its own `/region` command, which takes priority over Homestead's. Use one of Homestead's aliases instead: `/homestead`, `/hs`, or `/rg`.

??? question "I always hit the claim limit"

    Check your limits method in `limits.yml`. Homestead supports three methods, and the one you choose decides how limits are assigned:

    - **static**: Non-operator players all share the same limits; operators get a separate, larger set. No permissions plugin required.
    - **groups**: Tied to permission groups (for example, LuckPerms). Any group not defined in the config gets **zero** limits, so every group on your server must be listed.
    - **permissions**: Uses permission nodes (`homestead.group.NAME`) instead of group names, which is useful when group names are not a good fit.

    Read more: [Ranks & Limits](../Configuration/Ranks%20and%20Limits.md)

??? question "How do I reload the configuration or resource files after changes?"

    ```
    /hsadmin reload
    ```

    Note that some changes may require a full server restart instead, including upkeep, taxes, and renting settings. Reloading when a restart is required can cause problems, so when in doubt, restart.

??? question "Can I migrate from another claiming plugin?"

    Yes! Homestead supports importing from GriefPrevention, ClaimChunk, LandLord4, Lands, and HuskClaims.

    ```
    /hsadmin import [plugin name]
    ```

    Keep your old plugin installed during the import so Homestead can read its data, and back up your server first.

    Read more: [Database Migration](../Configuration/Database%20Migration.md)

??? question "Players can't see borders"

    Border display problems are usually caused by the client or the display type rather than by Homestead itself. Please check:

    1. The border display type in `regions.yml`. Try switching from `particles` to `blocks`.
    2. Whether your resource pack disables or replaces particles.
    3. Minecraft's accessibility settings, which may have particles disabled.

??? question "Can I translate Homestead to another language?"

    Yes! Copy `en-US.yml` from `plugins/Homestead/languages/`, translate the messages, and update the `language` setting in `config.yml` to match your new file's name. Then run `/hsadmin reload`.

    Read more: [Language](../Advanced/Language.md)

??? question "How do I change the database?"

    Changing providers takes a few careful steps because your data must be exported from the old database before you switch. Follow them in order:

    1. Fill in the new provider's connection details in `config.yml`, leaving the current `provider` value unchanged for now.
    2. Run `/hsadmin reload`.
    3. Run `/hsadmin export [provider]` and wait for the export to finish.
    4. Stop the server.
    5. Update `config.yml` with the new provider name.
    6. Start the server. Homestead now reads from the new database.

??? question "Can I disable claiming in certain worlds?"

    Yes. In `regions.yml`, list exact world names or wildcard patterns:

    ```yaml
    # In regions.yml
    disabled-worlds-exact:
      - "world_the_end"

    disabled-worlds-pattern:
      - "em_*"
    ```

    - **Exact** — case-sensitive full world names
    - **Pattern** — `*` matches any characters, so `em_*` matches `em_world`, `em_dungeon`, and so on

    Read more: [Disabled Worlds](../Configuration/Other%20Settings.md#disabled-worlds)

??? question "The plugin updated and my configuration broke!"

    Homestead includes a configuration migrator that automatically updates old configs on startup, so most updates happen without any action on your part.

    If you are still having issues, back up and delete the affected configuration files, let Homestead regenerate them with defaults, then re-apply your customizations.

??? question "My region bank is empty: how do I add money?"

    ```
    /hs deposit [amount]
    ```

    The money comes from your personal balance, which requires an economy plugin and a bridge (Vault or a modern alternative). See [Economy Setup](../Economy/Setup.md) if you have not set one up yet.

---

[Still need help? Join our Discord](https://discord.gg/uh7gqDY6sz)

