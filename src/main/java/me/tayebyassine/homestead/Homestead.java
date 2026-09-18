package me.tayebyassine.homestead;

import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import me.tayebyassine.homestead.api.events.APIEvent;
import me.tayebyassine.homestead.commands.CommandRegistry;
import me.tayebyassine.homestead.commands.brigadier.BrigadierCommands;
import me.tayebyassine.homestead.database.Database;
import me.tayebyassine.homestead.database.Driver;
import me.tayebyassine.homestead.database.cache.*;
import me.tayebyassine.homestead.discord.DiscordWebhookClient;
import me.tayebyassine.homestead.events.MemberTaxes;
import me.tayebyassine.homestead.events.RegionRent;
import me.tayebyassine.homestead.events.RegionUpkeep;
import me.tayebyassine.homestead.integrations.*;
import me.tayebyassine.homestead.listeners.borders.BorderBreakListener;
import me.tayebyassine.homestead.listeners.entities.ItemTransportingEntityValidateTargetListener;
import me.tayebyassine.homestead.listeners.player.*;
import me.tayebyassine.homestead.listeners.protection.RegionProtectionListener;
import me.tayebyassine.homestead.listeners.selection.SelectionToolListener;
import me.tayebyassine.homestead.listeners.signs.CustomSignsListener;
import me.tayebyassine.homestead.listeners.util.CopperGolemTracker;
import me.tayebyassine.homestead.logs.Logger;
import me.tayebyassine.homestead.managers.*;
import me.tayebyassine.homestead.resources.ResourceType;
import me.tayebyassine.homestead.resources.Resources;
import me.tayebyassine.homestead.resources.files.ConfigFile;
import me.tayebyassine.homestead.resources.files.RegionsFile;
import me.tayebyassine.homestead.sessions.*;
import me.tayebyassine.homestead.snowflake.SnowflakeGenerator;
import me.tayebyassine.homestead.storage.StorageManager;
import me.tayebyassine.homestead.util.https.UpdateChecker;
import me.tayebyassine.homestead.util.java.ListUtils;
import me.tayebyassine.homestead.util.minecraft.chunks.ProtectionMode;
import me.tayebyassine.homestead.util.minecraft.limits.Limits;
import me.tayebyassine.homestead.util.minecraft.players.DelayedTeleport;
import me.tayebyassine.homestead.util.minecraft.plugins.IntegrationUtility;
import me.tayebyassine.homestead.util.minecraft.plugins.MapIcon;
import me.tayebyassine.homestead.util.minecraft.threads.TaskHandle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.CopperGolem;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Entry point of Homestead plugin.
 */
public class Homestead extends JavaPlugin {

    private static final String VERSION = "6.0.0.0-26w37c";
    private static final boolean SNAPSHOT = true;
    private static final List<String> MC_VERSIONS = List.of("26.1", "26.1.1", "26.1.2", "26.2", "26.3");

    public static RegionCache REGION_CACHE;
    public static RegionMemberCache MEMBER_CACHE;
    public static RegionBanCache BAN_CACHE;
    public static RegionChunkCache CHUNK_CACHE;
    public static RegionIndexedChunkCache REGION_INDEXED_CHUNK_CACHE;
    public static PositionIndexedChunkCache POSITION_INDEXED_CHUNK_CACHE;
    public static RegionInviteCache INVITE_CACHE;
    public static RegionLogCache LOG_CACHE;
    public static RegionRateCache RATE_CACHE;
    public static WarsCache WAR_CACHE;
    public static SubAreasCache SUBAREA_CACHE;
    public static LevelsCache LEVEL_CACHE;

    public static Database database;
    public static Vault VAULT;
    private static Homestead INSTANCE;
    private static DiscordWebhookClient DISCORD_WEBHOOK;
    private static boolean IS_FOLIA = false;
    private static boolean IS_PAPER = false;
    private static long STARTED_AT;
    private static TaskHandle MOVE_CHECK_TASK;

    /**
     * Get the snowflake ID generator used for unique region, chunk, and
     * sub-area identifiers.
     *
     * @return the singleton snowflake generator
     */
    public static SnowflakeGenerator getSnowflake() {
        return SnowflakeHolder.INSTANCE;
    }

    /**
     * Get the current plugin version string.
     *
     * @return the version identifier
     */
    public static String getVersion() {
        return VERSION;
    }

    /**
     * Whether this build is a snapshot (pre-release) version.
     *
     * @return {@code true} if the build is a snapshot
     */
    public static boolean isSnapshot() {
        return SNAPSHOT;
    }

    /**
     * Get the singleton plugin instance.
     *
     * @return the active Homestead instance, or {@code null} if not yet loaded
     */
    public static Homestead getInstance() {
        return INSTANCE;
    }

    /**
     * Detect whether the server software is Folia by checking for the
     * {@code RegionizedServer} class at runtime.
     *
     * @return {@code true} if Folia classes are present
     */
    public static boolean checkSoftwareIfFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Whether the server is running on Folia.
     *
     * @return {@code true} if Folia was detected during startup
     */
    public static boolean isFolia() {
        return IS_FOLIA;
    }

    /**
     * Detect whether the server software is Paper by checking for the
     * {@code Configuration} class at runtime.
     *
     * @return {@code true} if Paper classes are present
     */
    public static boolean checkSoftwareIfPaper() {
        try {
            Class.forName("io.papermc.paper.configuration.Configuration");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Whether the server is running on Paper.
     *
     * @return {@code true} if Paper was detected during startup
     */
    public static boolean isPaper() {
        return IS_PAPER;
    }

    /**
     * Fire an {@link APIEvent} synchronously and forward it to the Discord
     * webhook if configured.
     *
     * @param event the custom event to dispatch
     */
    public static void callEvent(APIEvent event) {
        Homestead.getInstance().runSyncTask(() -> Bukkit.getPluginManager().callEvent(event));

        if (DISCORD_WEBHOOK != null) {
            DISCORD_WEBHOOK.callEventDiscordWebhook(event);
        }
    }

    /**
     * Called when the plugin is enabled by the server.
     *
     * <p>Initialises caches, database, Vault hooks, event listeners,
     * Brigadier commands, metrics, Discord integration, and all recurring
     * background tasks.
     * </p>
     */
    public void onEnable() {
        Homestead.INSTANCE = this;
        Homestead.STARTED_AT = System.currentTimeMillis();

        Homestead.IS_FOLIA = checkSoftwareIfFolia();
        Homestead.IS_PAPER = checkSoftwareIfPaper();

        new Logger();

        if (!MC_VERSIONS.contains(Bukkit.getMinecraftVersion())) {
            Logger.warning("You are currently using an unsupported Minecraft version!");
            Logger.warning("Homestead will likely face many problems due to the current API structure.");
            Logger.warning("Supported versions: " + String.join(", ", MC_VERSIONS));
        }

        try {
            if (!getDataFolder().exists()) {
                if (!getDataFolder().mkdirs()) {
                    throw new IOException("Unable to create Bukkit data directory");
                }
            }
        } catch (IOException | SecurityException e) {
            endInstance(e);
            return;
        }

        saveDefaultConfig();

        try {
            Resources.load(this);
        } catch (Exception e) {
            endInstance(e);
            return;
        }

        Logger.debug("Debug mode is enabled.");

        Homestead.REGION_CACHE = new RegionCache();
        Homestead.MEMBER_CACHE = new RegionMemberCache();
        Homestead.BAN_CACHE = new RegionBanCache();
        Homestead.CHUNK_CACHE = new RegionChunkCache();
        Homestead.REGION_INDEXED_CHUNK_CACHE = new RegionIndexedChunkCache();
        Homestead.POSITION_INDEXED_CHUNK_CACHE = new PositionIndexedChunkCache();
        Homestead.INVITE_CACHE = new RegionInviteCache();
        Homestead.LOG_CACHE = new RegionLogCache();
        Homestead.RATE_CACHE = new RegionRateCache();
        Homestead.WAR_CACHE = new WarsCache();
        Homestead.SUBAREA_CACHE = new SubAreasCache();
        Homestead.LEVEL_CACHE = new LevelsCache();

        try {
            Driver provider = Driver.parse(Resources.<ConfigFile>get(ResourceType.Config).getDatabaseProvider());

            if (provider == null) {
                throw new IllegalStateException("Database provider not found.");
            }

            Homestead.database = new Database(provider);
            Homestead.database.importToCache();
        } catch (Exception e) {
            Logger.error("A critical database error occurred while starting up. The plugin will NOT be disabled.");
            Logger.error("The plugin will enter protection mode to keep all claims safe from griefing and land theft.");
            Logger.error(e);

            ProtectionMode.enableAutomatic();
            Homestead.database = null;
        }

        if (!IntegrationUtility.isEnabled(IntegrationUtility.Integration.VAULT)) {
            Logger.error("Unable to start the plugin; \"Vault\" is required. Shutting down plugin instance...");

            endInstance();
            return;
        } else {
            Logger.info("Loading service providers with Vault... (Target: " + (isFolia() || Vault.isVaultUnlockedDetected() ? "VaultUnlocked" : "Legacy Vault") + ")");
        }

        if (isFolia() && !Vault.isVaultUnlockedDetected()) {
            Logger.error("Your server is running on Folia! Please use VaultUnlocked instead of Vault!");
            Logger.error("The original Vault plugin doesn't support Folia servers! Shutting down plugin...");

            endInstance();
            return;
        }

        Homestead.VAULT = new Vault(this);

        if (!Homestead.VAULT.setupEconomy()) {
            Logger.warning("No Economy service provider found.");
            Logger.warning("Any feature requiring an Economy service will be skipped.");
        } else {
            Logger.info("Loaded service provider: Economy [" + Homestead.VAULT.getEconomy().getName() + "]");
        }

        if (!Homestead.VAULT.setupPermissions()) {
            if (Limits.getLimitsMethod() == Limits.LimitMethod.GROUPS || Limits.getLimitsMethod() == Limits.LimitMethod.PERMISSIONS) {
                Logger.error("No Permissions service provider found.");
                Logger.error("You are using 'groups' or 'permissions' as a limit method, and a permission plugin is required for this feature to work.");
                Logger.error("Change the limit method in the limits.yml file. Shutting down plugin...");
                endInstance();
                return;
            } else {
                Logger.warning("No Permission service provider found.");
                Logger.warning("The plugin is using 'static' permissions; operator and non-operator.");
            }
        } else {
            Logger.info("Loaded service provider: Permissions [" + Homestead.VAULT.getPermissions().getPermissionsName() + "]");
        }

        StorageManager.init();

        if (Resources.<RegionsFile>get(ResourceType.Regions).isCleanStartupEnabled()) {
            Logger.info("Cleaning up corrupted data... This may take a while!");

            int regions = RegionManager.cleanupInvalidRegions();
            int subareas = SubAreaManager.cleanupInvalidSubAreas();
            int wars = WarManager.cleanupInvalidWars();
            int levels = LevelManager.cleanupInvalidLevels();

            int bans = BanManager.cleanupInvalidBans();
            int chunks = ChunkManager.cleanupInvalidChunks();
            int invites = InviteManager.cleanupInvalidInvites();
            int logs = LogManager.cleanupInvalidLogs();
            int members = MemberManager.cleanupInvalidMembers();
            int rates = RateManager.cleanupInvalidRatings();

            ChunkManager.cleanupOrphanedForceLoadedChunks();

            Logger.info("Done repairing corrupted data.");

            if (Resources.<ConfigFile>get(ResourceType.Config).isDebugEnabled()) {
                String[] headers = {"Model", "Fixed/Removed"};

                Object[][] data = {
                        {"Regions", regions},
                        {"Members", members},
                        {"Chunks", chunks},
                        {"Invites", invites},
                        {"Logs", logs},
                        {"Rates", rates},
                        {"Bans", bans},
                        {"Levels", levels},
                        {"Wars", wars},
                        {"SubAreas", subareas},
                };

                ListUtils.printTable(headers, data);
            }
        }

        ChunkManager.reregisterForceLoadedChunks();

        registerCommands();
        registerEvents();
        registerBrigadier();

        if (Resources.<ConfigFile>get(ResourceType.Config).isMetricsEnabled()) {
            new bStats(this);

            Logger.info("bStats metrics is enabled, anonymous data is being sent to the servers.");

            try {
                new FastStats(this);

                Logger.info("FastStats metrics is enabled, anonymous data is being sent to the servers.");
            } catch (Exception e) {
                Logger.error(e);
            }
        }

        runSyncTask(() -> {
            Logger.debug("Loading Copper Golem spawn locations... This may take a while.");

            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (entity instanceof CopperGolem golem) {
                        CopperGolemTracker.recordSpawnRegion(golem);
                    }
                }
            }

            Logger.debug("Done recording Copper Golems spawn locations.");
        });

        Logger.info("Ready, took " + (System.currentTimeMillis() - STARTED_AT) + " ms to load.");

        if (Resources.<ConfigFile>get(ResourceType.Config).isDiscordEnabled()) {
            Logger.info("Initializing new Discord webhook client...");

            Homestead.DISCORD_WEBHOOK = new DiscordWebhookClient(Resources.<ConfigFile>get(ResourceType.Config).getDiscordWebhookURL());

            Logger.info("Discord webhook instance is ready.");
        }

        int cacheInterval = Resources.<ConfigFile>get(ResourceType.Config).getCacheInterval();

        runAsyncTimerTask(() -> {
            if (Homestead.database == null) {
                tryReconnectDatabase();
                return;
            }

            try {
                Homestead.database.exportFromCache();
                ProtectionMode.clearManualOverride();
            } catch (Exception e) {
                Logger.error("A critical database error occurred while exporting data. The plugin will NOT be disabled.");
                Logger.error("The plugin will enter protection mode to keep all claims safe from griefing and land theft.");
                Logger.error(e);

                ProtectionMode.enable();
                tryReconnectDatabase();
            }
        }, 10, cacheInterval);

        if (Resources.<ConfigFile>get(ResourceType.Config).isDynamicMapsEnabled() && Resources.<ConfigFile>get(ResourceType.Config).isDynamicMapsIconsEnabled()) {
            if (DynamicMaps.isPl3xMapInstalled() || DynamicMaps.isSquaremapInstalled()) {
                runAsyncTask(() -> {
                    MapIcon.downloadAllIcons();
                    Logger.info("Successfully downloaded all icons!");
                });
            } else {
                Logger.warning("Cannot download region icons due to 'Pl3xMap' or 'Squaremap' plugins not being installed/enabled on the server.");
            }
        }

        if (Resources.<ConfigFile>get(ResourceType.Config).isDynamicMapsEnabled()) {
            runAsyncTimerTask(() -> {
                Logger.debug("Updating web-rendering plugin markers...");

                DynamicMaps.trigger(this);

                Logger.debug("Updated " + RegionManager.getAll().size() + " region markers.");
            }, Resources.<ConfigFile>get(ResourceType.Config).getDynamicMapsUpdateInterval());
        }

        if (Homestead.VAULT.isEconomyReady() && Resources.<RegionsFile>get(ResourceType.Regions).isTaxesEnabled()) {
            runAsyncTimerTask(MemberTaxes::trigger, 10);
        }

        if (Homestead.VAULT.isEconomyReady() && Resources.<RegionsFile>get(ResourceType.Regions).isUpkeepEnabled()) {
            runAsyncTimerTask(RegionUpkeep::trigger, 10);
        }

        if (Homestead.VAULT.isEconomyReady() && Resources.<RegionsFile>get(ResourceType.Regions).isRentingEnabled()) {
            runAsyncTimerTask(RegionRent::trigger, 10);
        }

        runAsyncTimerTask(() -> {
            UpdateChecker.FetchedUpdateData data = UpdateChecker.fetch();

            if (data.errored()) {
                Logger.error(Logger.PredefinedMessage.UPDATE_FETCH_FAILURE);
                return;
            }

            if (data.current().equals(data.latest())) {
                Logger.info(Logger.PredefinedMessage.UPDATE_LATEST);
            } else {
                Logger.warning(Logger.PredefinedMessage.UPDATE_FOUND);
            }
        }, 86400);

        registerExternalPlugins();

        // Copper golems interaction platform bridge for SpigotMC and Paper.
        // Since the event ItemTransportingEntityValidateTargetEvent only exists in the PaperMC API, we will need
        // to check if the class exists or not. If the class is found, then the plugin will use it. Otherwise, if
        // the class was not found, then the plugin will use a custom entity moving listener to capture Copper
        // golem locations.
        if (ItemTransportingEntityValidateTargetListener.isClassFound()) {
            registerEvent(new ItemTransportingEntityValidateTargetListener());
        } else {
            if (!isFolia()) {
                Homestead.MOVE_CHECK_TASK = new TaskHandle(Bukkit.getScheduler().runTaskTimer(this, () -> {
                    for (World world : Bukkit.getWorlds()) {
                        for (Entity entity : world.getEntities()) {
                            RegionProtectionListener.onEntityMove(entity);
                        }
                    }
                }, 0L, 1L));
            }
        }
    }

    /**
     * Called when the plugin is disabled by the server.
     *
     * <p>Closes the database connection, cancels running tasks, persists
     * region storage, and clears all in-memory caches.
     * </p>
     */
    public void onDisable() {
        if (Homestead.database != null) {
            Logger.info("Closing database connection...");

            try {
                Homestead.database.closeConnection();
            } catch (Exception e) {
                Logger.error(e);
            }
        }

        if (Homestead.MOVE_CHECK_TASK != null) {
            Homestead.MOVE_CHECK_TASK.cancel();
        }

        Logger.info("Saving storage...");

        StorageManager.saveAll();

        Logger.info("Cleaning cache...");

        Homestead.REGION_CACHE.clear();
        Homestead.MEMBER_CACHE.clear();
        Homestead.BAN_CACHE.clear();
        Homestead.CHUNK_CACHE.clear();
        Homestead.REGION_INDEXED_CHUNK_CACHE.clear();
        Homestead.POSITION_INDEXED_CHUNK_CACHE.clear();
        Homestead.INVITE_CACHE.clear();
        Homestead.LOG_CACHE.clear();
        Homestead.RATE_CACHE.clear();
        Homestead.WAR_CACHE.clear();
        Homestead.SUBAREA_CACHE.clear();
        Homestead.LEVEL_CACHE.clear();

        Logger.info("Cleaning sessions...");
        AutoClaimSession.SESSIONS.clear();
        ClaimFlySession.cleanupAll();
        MergeRegionRequest.REQUESTS.clear();
        PlayerInputSession.SESSIONS.clear();
        PrivateChatSession.SESSIONS.clear();
        TargetRegionSession.SESSIONS.clear();

        DelayedTeleport.cleanup();

        Logger.info("Homestead has been disabled.");
    }

    /**
     * Register all plugin commands defined in {@code plugin.yml}.
     */
    private void registerCommands() {
        CommandRegistry.registerAll();
    }

    /**
     * Register all Bukkit event listeners.
     */
    private void registerEvents() {
        registerEvent(new PlayerJoinListener());
        registerEvent(new PlayerEnterEndExitPortalListener());
        registerEvent(new DelayedTeleportListener());
        registerEvent(new EntityDeathListener());
        registerEvent(new BorderBreakListener());
        registerEvent(new PlayerDeathListener());
        registerEvent(new PlayerAutoClaimListener());
        registerEvent(new CustomSignsListener());
        registerEvent(new CommandsCooldownListener());
        registerEvent(new SelectionToolListener());
        registerEvent(new RegionProtectionListener());
        registerEvent(new PlayerRegionEnterAndExitListener());
        registerEvent(new PrivateRegionChatListener());
    }

    /**
     * Register a single Bukkit event listener.
     *
     * @param listener the listener to register
     */
    private void registerEvent(Listener listener) {
        try {
            getServer().getPluginManager().registerEvents(listener, this);
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    /**
     * Register Brigadier command completions and argument types via
     * Commodore, if the server supports Mojang Brigadier.
     */
    private void registerBrigadier() {
        try {
            if (CommodoreProvider.isSupported()) {
                Commodore commodore = CommodoreProvider.getCommodore(this);
                new BrigadierCommands(commodore);
            } else {
                Logger.warning("Mojang Brigadier is not supported on this server software.");
            }
        } catch (NoClassDefFoundError e) {
            Logger.warning("Commodore/Brigadier classes not present. Skipping Brigadier command registration.");
        }
    }

    /**
     * Register external plugin integrations (e.g. PlaceholderAPI).
     */
    public void registerExternalPlugins() {
        if (IntegrationUtility.isEnabled(IntegrationUtility.Integration.PAPI)) {
            boolean registered = new PlaceholderAPI().register();

            if (!registered) {
                Logger.error("Failed to register hooks.");
            }
        }
    }

    /**
     * Run a task on the region thread that owns the given player.
     *
     * <p>Use this instead of {@link #runSyncTask(Runnable)} whenever the
     * task involves world/chunk/location access triggered by a player
     * action (e.g. inventory clicks).
     * </p>
     *
     * @param player   the player whose region thread to run on
     * @param callable the task to run
     * @return a handle to the scheduled task
     */
    public TaskHandle runPlayerTask(Player player, Runnable callable) {
        if (isFolia()) {
            return new TaskHandle(player.getScheduler().run(this, task -> callable.run(), null));
        }

        return new TaskHandle(Bukkit.getScheduler().runTask(this, callable));
    }

    /**
     * Run a task on the region thread that owns the given player after a
     * delay.
     *
     * @param player   the player whose region thread to run on
     * @param callable the task to run
     * @param delay    the delay, in seconds
     * @return a handle to the scheduled task
     */
    public TaskHandle runPlayerTaskLater(Player player, Runnable callable, int delay) {
        if (isFolia()) {
            long delayTicks = delay * 20L;
            return new TaskHandle(player.getScheduler().runDelayed(this, task -> callable.run(), null, delayTicks));
        }

        long delayTicks = delay * 20L;
        return new TaskHandle(Bukkit.getScheduler().runTaskLater(this, callable, delayTicks));
    }

    /**
     * Run a repeating task on the region thread that owns the given player.
     *
     * @param player   the player whose region thread to run on
     * @param callable the task to run
     * @param delay    ticks to wait before first execution
     * @param period   ticks between executions
     * @return a handle to the scheduled task
     */
    public TaskHandle runPlayerTaskTimer(Player player, Runnable callable, long delay, long period) {
        if (isFolia()) {
            return new TaskHandle(player.getScheduler().runAtFixedRate(this, task -> callable.run(), null, delay, period));
        }

        return new TaskHandle(Bukkit.getScheduler().runTaskTimer(this, callable, delay, period));
    }

    /**
     * Run a task synchronously on the main thread.
     *
     * @param callable the task to run
     * @return a handle to the scheduled task
     */
    public TaskHandle runSyncTask(Runnable callable) {
        if (isFolia()) {
            return new TaskHandle(Bukkit.getGlobalRegionScheduler().run(this, task -> callable.run()));
        }

        return new TaskHandle(Bukkit.getScheduler().runTask(this, callable));
    }

    /**
     * Run a task synchronously after a delay.
     *
     * @param callable the task to run
     * @param delay    the delay, in seconds
     * @return a handle to the scheduled task
     */
    public TaskHandle runSyncTaskLater(Runnable callable, int delay) {
        if (isFolia()) {
            long delayTicks = delay * 20L;
            return new TaskHandle(Bukkit.getGlobalRegionScheduler().runDelayed(this, task -> callable.run(), delayTicks));
        }

        long delayTicks = delay * 20L;

        return new TaskHandle(Bukkit.getScheduler().runTaskLater(this, callable, delayTicks));
    }

    /**
     * Run a repeating task synchronously with interval in ticks.
     *
     * @param callable the task to run
     * @param ticks    the interval, in ticks
     * @return a handle to the scheduled task
     */
    public TaskHandle runSyncTimerTask(Runnable callable, long ticks) {
        if (isFolia()) {
            // Folia requires initial delay >= 1; global region scheduler uses ticks
            return new TaskHandle(Bukkit.getGlobalRegionScheduler().runAtFixedRate(this, task -> callable.run(), 1L, ticks));
        }

        return new TaskHandle(Bukkit.getScheduler().runTaskTimer(this, callable, 0L, ticks));
    }

    /**
     * Run a task asynchronously.
     *
     * @param callable the task to run
     * @return a handle to the scheduled task
     */
    public TaskHandle runAsyncTask(Runnable callable) {
        if (isFolia()) {
            return new TaskHandle(Bukkit.getAsyncScheduler().runNow(this, task -> callable.run()));
        }

        return new TaskHandle(Bukkit.getScheduler().runTaskAsynchronously(this, callable));
    }

    /**
     * Run a task asynchronously after a delay.
     *
     * @param callable the task to run
     * @param delay    the delay, in seconds
     * @return a handle to the scheduled task
     */
    public TaskHandle runAsyncTaskLater(Runnable callable, int delay) {
        if (isFolia()) {
            return new TaskHandle(Bukkit.getAsyncScheduler().runDelayed(this, task -> callable.run(), delay, TimeUnit.SECONDS));
        }

        long delayTicks = delay * 20L;

        return new TaskHandle(Bukkit.getScheduler().runTaskLaterAsynchronously(this, callable, delayTicks));
    }

    /**
     * Run a repeating task asynchronously with interval in seconds.
     *
     * @param callable the task to run
     * @param interval the interval, in seconds
     * @return a handle to the scheduled task
     */
    public TaskHandle runAsyncTimerTask(Runnable callable, int interval) {
        if (isFolia()) {
            return new TaskHandle(Bukkit.getAsyncScheduler().runAtFixedRate(this, task -> callable.run(), 0, interval, TimeUnit.SECONDS));
        }

        long intervalTicks = interval * 20L;

        return new TaskHandle(Bukkit.getScheduler().runTaskTimerAsynchronously(this, callable, 0L, intervalTicks));
    }

    /**
     * Run a repeating task asynchronously with a delay and interval in
     * seconds.
     *
     * @param callable the task to run
     * @param delay    the delay before first execution, in seconds
     * @param interval the interval between executions, in seconds
     * @return a handle to the scheduled task
     */
    public TaskHandle runAsyncTimerTask(Runnable callable, int delay, int interval) {
        if (isFolia()) {
            return new TaskHandle(Bukkit.getAsyncScheduler().runAtFixedRate(this, task -> callable.run(), delay, interval, TimeUnit.SECONDS));
        }

        long delayTicks = delay * 20L;
        long intervalTicks = interval * 20L;

        return new TaskHandle(Bukkit.getScheduler().runTaskTimerAsynchronously(this, callable, delayTicks, intervalTicks));
    }

    /**
     * Run a task on the region thread that owns the given location.
     *
     * <p>Use this for any event or operation tied to a specific world
     * location.
     * </p>
     *
     * @param location the location whose owning region thread to run on
     * @param callable the task to run
     * @return a handle to the scheduled task
     */
    public TaskHandle runLocationTask(Location location, Runnable callable) {
        if (isFolia()) {
            return new TaskHandle(Bukkit.getRegionScheduler().run(this, location, task -> callable.run()));
        }

        return new TaskHandle(Bukkit.getScheduler().runTask(this, callable));
    }

    /**
     * Run a repeating task on the region thread that owns the given
     * location.
     *
     * @param location the location to determine which region thread to use
     * @param callable the task to run
     * @param delay    ticks to wait before first execution
     * @param period   ticks between executions
     * @return a handle to the scheduled task
     */
    public TaskHandle runLocationTaskTimer(Location location, Runnable callable, long delay, long period) {
        if (isFolia()) {
            return new TaskHandle(Bukkit.getRegionScheduler().runAtFixedRate(this, location, task -> callable.run(), delay, period));
        }

        return new TaskHandle(Bukkit.getScheduler().runTaskTimer(this, callable, delay, period));
    }

    /**
     * Get a list of all offline players (including those who have never
     * logged in).
     *
     * @return an unmodifiable view of all known offline players
     */
    public List<OfflinePlayer> getOfflinePlayersSync() {
        OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();

        return Arrays.asList(offlinePlayers);
    }

    /**
     * Get a list of all currently online players.
     *
     * @return a snapshot list of online players
     */
    public List<Player> getOnlinePlayersSync() {
        return new ArrayList<>(Bukkit.getOnlinePlayers());
    }

    /**
     * Get the names of all offline players synchronously.
     *
     * @return list of player names, may contain {@code null} entries
     */
    public List<String> getOfflinePlayerNamesSync() {
        return Homestead.getInstance().getOfflinePlayersSync().stream()
                .map(OfflinePlayer::getName)
                .collect(Collectors.toList());
    }

    /**
     * Get the names of all currently online players synchronously.
     *
     * @return list of online player names
     */
    public List<String> getOnlinePlayerNamesSync() {
        return Homestead.getInstance().getOnlinePlayersSync().stream()
                .map(Player::getName)
                .collect(Collectors.toList());
    }

    /**
     * Look up an offline player by UUID, using a safe method that checks
     * online players first, then the offline cache.
     *
     * @param playerId the player UUID
     * @return the offline player, or {@code null} if not found or never played
     */
    public @Nullable OfflinePlayer getOfflinePlayerSync(UUID playerId) {
        Player onlinePlayer = Bukkit.getPlayer(playerId);

        if (onlinePlayer != null) {
            return onlinePlayer;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);

        return player.getName() != null && (player.hasPlayedBefore() || player.isOnline()) ? player : null;
    }

    /**
     * Look up an offline player by name, using a safe method that checks
     * online players, the cached player list, and a full scan.
     *
     * @param playerName the player name
     * @return the offline player, or {@code null} if not found or never played
     */
    public @Nullable OfflinePlayer getOfflinePlayerSync(String playerName) {
        Player onlinePlayer = Bukkit.getPlayer(playerName);

        if (onlinePlayer != null) {
            return onlinePlayer;
        }

        OfflinePlayer cached = Bukkit.getOfflinePlayerIfCached(playerName);

        if (cached != null) {
            return cached;
        }

        OfflinePlayer player = Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(offlinePlayer -> playerName.equals(offlinePlayer.getName()))
                .findFirst()
                .orElse(null);

        return player != null && player.getName() != null && (player.hasPlayedBefore() || player.isOnline()) ? player : null;
    }

    /**
     * Disable the plugin instance through the server's plugin manager.
     */
    public void endInstance() {
        getServer().getPluginManager().disablePlugin(this);
    }

    /**
     * Log the given throwable and then disable the plugin instance.
     *
     * @param e the error that caused the shutdown
     */
    public void endInstance(Throwable e) {
        Logger.error(e);

        endInstance();
    }

    /**
     * Attempt to (re)establish the database connection.
     *
     * <p>Used while the plugin is in protection mode after a database
     * failure, so the connection can recover in the background.
     * </p>
     *
     * <p>Reconnecting does NOT automatically disable protection mode; an
     * operator must turn it off manually.
     * </p>
     */
    private void tryReconnectDatabase() {
        try {
            Driver provider = Driver.parse(Resources.<ConfigFile>get(ResourceType.Config).getDatabaseProvider());

            if (provider == null) {
                Logger.error("Unable to reconnect: database provider not found in config.");
                return;
            }

            Homestead.database = new Database(provider);
            Logger.info("Database connection re-established successfully.");
        } catch (Exception e) {
            Logger.error("Unable to re-establish the database connection. The plugin will remain in protection mode and retry on the next cycle.");
        }
    }

    private static class SnowflakeHolder {
        static final SnowflakeGenerator INSTANCE = new SnowflakeGenerator();
    }
}
