package net.minecraft.server;

import co.aikar.timings.SpigotTimings;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import jline.console.ConsoleReader;
import joptsimple.OptionSet;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.craftbukkit.Main;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.security.KeyPair;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public abstract class MinecraftServer extends com.minexd.spigot.tickloop.ReentrantIAsyncHandler<com.minexd.spigot.tickloop.TasksPerTick> implements ICommandListener, IAsyncTaskHandler, IMojangStatistics { // PandaSpigot - Modern tick loop

    public static final Logger LOGGER = LogManager.getLogger();
    public static final File a = new File("usercache.json");
    public static long LAST_TICK_TIME;
    private static MinecraftServer l;
    public Convertable convertable;
    private final MojangStatisticsGenerator n = new MojangStatisticsGenerator("server", this, az());
    public File universe;
    private final List<IUpdatePlayerListBox> p = Lists.newArrayList();
    protected final ICommandHandler b;
    public final MethodProfiler methodProfiler = new MethodProfiler();
    private ServerConnection serverConnection;
    private final ServerPing r = new ServerPing();
    private final Random s = new Random();
    private String serverIp;
    private int u = -1;
    public WorldServer[] worldServer;
    private PlayerList playerList;
    private boolean isRunning = true;
    private boolean isStopped;
    private int ticks;
    protected final Proxy e;
    public String f;
    public int g;
    private boolean onlineMode;
    private boolean spawnAnimals;
    private boolean spawnNPCs;
    private boolean pvpMode;
    private boolean allowFlight;
    private String motd;
    private int maxBuildHeight;
    private int G = 0;
    public final long[] h = new long[100];
    public long[][] i;
    private KeyPair H;
    private String I;
    private String worldName;
    private boolean demoMode;
    private boolean M;
    private boolean N;
    private String O = "";
    private String P = "";
    private boolean Q;
    private long R;
    private String S;
    private boolean T;
    private boolean U;
    private final YggdrasilAuthenticationService V;
    private final MinecraftSessionService W;
    private long X = 0L;
    private final GameProfileRepository Y;
    private final UserCache Z;
    protected final Queue<FutureTask<?>> j = new java.util.concurrent.ConcurrentLinkedQueue<>();
    private Thread serverThread;
    private long ab = az();

    public List<WorldServer> worlds = new ArrayList<>();
    public org.bukkit.craftbukkit.CraftServer server;
    public OptionSet options;
    public org.bukkit.command.ConsoleCommandSender console;
    public org.bukkit.command.RemoteConsoleCommandSender remoteConsole;
    public ConsoleReader reader;
    public static int currentTick = 0;
    public final Thread primaryThread;
    public java.util.Queue<Runnable> processQueue = new java.util.concurrent.ConcurrentLinkedQueue<>();
    public int autoSavePeriod;

    // PandaSpigot start - Modern tick loop
    private long nextTickTime;
    private long delayedTasksMaxNextTickTime;
    private boolean mayHaveDelayedTasks;
    private boolean forceTicks;
    private volatile boolean isReady;
    private long lastOverloadWarning;
    public long serverStartTime;
    public volatile Thread shutdownThread;
    private long lastTick = 0;
    private long catchupTime = 0;
    public static <S extends MinecraftServer> S spin(java.util.function.Function<Thread, S> serverFactory) {
        java.util.concurrent.atomic.AtomicReference<S> reference = new java.util.concurrent.atomic.AtomicReference<>();
        Thread thread = new Thread(() -> reference.get().run(), "Server thread");

        thread.setUncaughtExceptionHandler((thread1, throwable) -> MinecraftServer.LOGGER.error(throwable));
        S server = serverFactory.apply(thread);

        reference.set(server);
        thread.setPriority(Thread.NORM_PRIORITY + 2);
        thread.start();
        return server;
    }

    public MinecraftServer(OptionSet options, Proxy proxy, File file1, Thread thread) {
        super("Server");
        this.nextTickTime = getMillis();
        this.primaryThread = thread;
        this.serverThread = thread;
        // PandaSpigot end
        io.netty.util.ResourceLeakDetector.setEnabled(false);

        this.e = proxy;
        MinecraftServer.l = this;
        this.Z = new UserCache(this, file1);
        this.b = this.h();
        this.V = new YggdrasilAuthenticationService(proxy, UUID.randomUUID().toString());
        this.W = this.V.createMinecraftSessionService();
        this.Y = this.V.createProfileRepository();

        this.options = options;

        if (System.console() == null && System.getProperty("jline.terminal") == null) {
            System.setProperty("jline.terminal", "jline.UnsupportedTerminal");

            Main.useJline = false;
        }

        try {
            reader = new ConsoleReader(System.in, System.out);
            reader.setExpandEvents(false);
        } catch (Throwable e) {
            try {
                System.setProperty("jline.terminal", "jline.UnsupportedTerminal");
                System.setProperty("user.language", "en");

                Main.useJline = false;

                reader = new ConsoleReader(System.in, System.out);
                reader.setExpandEvents(false);
            } catch (IOException ex) {
                LOGGER.warn((String) null, ex);
            }
        }

        Runtime.getRuntime().addShutdownHook(new org.bukkit.craftbukkit.util.ServerShutdownThread(this));

        // this.serverThread = primaryThread = new Thread(this, "Server thread");
    }

    public abstract PropertyManager getPropertyManager();

    protected CommandDispatcher h() {
        return new CommandDispatcher();
    }

    protected abstract boolean init() throws IOException;

    protected void a(String s) {
        if (this.getConvertable().isConvertable(s)) {
            MinecraftServer.LOGGER.info("Converting map!");
            this.b("menu.convertingLevel");

            this.getConvertable().convert(s, new IProgressUpdate() {
                private long b = System.currentTimeMillis();

                public void a(String s) {}

                public void a(int i) {
                    if (System.currentTimeMillis() - this.b >= 1000L) {
                        this.b = System.currentTimeMillis();
                        MinecraftServer.LOGGER.info("Converting... " + i + "%");
                    }

                }

                public void c(String s) {}
            });
        }

    }

    protected synchronized void b(String s) {
        this.S = s;
    }

    protected void a(String s, String s1, long i, WorldType worldtype, String s2) {
        this.a(s);
        this.b("menu.loadingLevel");
        this.worldServer = new WorldServer[3];

        int worldCount = 3;

        for (int j = 0; j < worldCount; ++j) {
            WorldServer world;
            byte dimension = 0;

            if (j == 1) {
                if (getAllowNether()) {
                    dimension = -1;
                } else {
                    continue;
                }
            }

            if (j == 2) {
                if (server.getAllowEnd()) {
                    dimension = 1;
                } else {
                    continue;
                }
            }

            String worldType = org.bukkit.World.Environment.getEnvironment(dimension).toString().toLowerCase();
            String name = (dimension == 0) ? s : s + "_" + worldType;

            org.bukkit.generator.ChunkGenerator gen = this.server.getGenerator(name);
            WorldSettings worldsettings = new WorldSettings(i, this.getGamemode(), this.getGenerateStructures(), this.isHardcore(), worldtype);
            worldsettings.setGeneratorSettings(s2);

            if (j == 0) {
                IDataManager idatamanager = new ServerNBTManager(server.getWorldContainer(), s1, true);
                WorldData worlddata = idatamanager.getWorldData();

                if (worlddata == null) {
                    worlddata = new WorldData(worldsettings, s1);
                }

                worlddata.checkName(s1);

                if (this.X()) {
                    world = (WorldServer) (new DemoWorldServer(this, idatamanager, worlddata, dimension, this.methodProfiler)).b();
                } else {
                    world = (WorldServer) (new WorldServer(this, idatamanager, worlddata, dimension, this.methodProfiler, org.bukkit.World.Environment.getEnvironment(dimension), gen)).b();
                }

                world.a(worldsettings);

                this.server.scoreboardManager = new org.bukkit.craftbukkit.scoreboard.CraftScoreboardManager(this, world.getScoreboard());
            } else {
                String dim = "DIM" + dimension;

                File newWorld = new File(new File(name), dim);
                File oldWorld = new File(new File(s), dim);

                if ((!newWorld.isDirectory()) && (oldWorld.isDirectory())) {
                    MinecraftServer.LOGGER.info("---- Migration of old " + worldType + " folder required ----");
                    MinecraftServer.LOGGER.info("Unfortunately due to the way that Minecraft implemented multiworld support in 1.6, Bukkit requires that you move your " + worldType + " folder to a new location in order to operate correctly.");
                    MinecraftServer.LOGGER.info("We will move this folder for you, but it will mean that you need to move it back should you wish to stop using Bukkit in the future.");
                    MinecraftServer.LOGGER.info("Attempting to move " + oldWorld + " to " + newWorld + "...");

                    if (newWorld.exists()) {
                        MinecraftServer.LOGGER.warn("A file or folder already exists at " + newWorld + "!");
                        MinecraftServer.LOGGER.info("---- Migration of old " + worldType + " folder failed ----");
                    } else if (newWorld.getParentFile().mkdirs()) {
                        if (oldWorld.renameTo(newWorld)) {
                            MinecraftServer.LOGGER.info("Success! To restore " + worldType + " in the future, simply move " + newWorld + " to " + oldWorld);
                            // Migrate world data too.
                            try {
                                com.google.common.io.Files.copy(new File(new File(s), "level.dat"), new File(new File(name), "level.dat"));
                            } catch (IOException exception) {
                                MinecraftServer.LOGGER.warn("Unable to migrate world data.");
                            }
                            MinecraftServer.LOGGER.info("---- Migration of old " + worldType + " folder complete ----");
                        } else {
                            MinecraftServer.LOGGER.warn("Could not move folder " + oldWorld + " to " + newWorld + "!");
                            MinecraftServer.LOGGER.info("---- Migration of old " + worldType + " folder failed ----");
                        }
                    } else {
                        MinecraftServer.LOGGER.warn("Could not create path for " + newWorld + "!");
                        MinecraftServer.LOGGER.info("---- Migration of old " + worldType + " folder failed ----");
                    }
                }

                IDataManager idatamanager = new ServerNBTManager(server.getWorldContainer(), name, true);
                WorldData worlddata = idatamanager.getWorldData();

                if (worlddata == null) {
                    worlddata = new WorldData(worldsettings, name);
                }

                worlddata.checkName(name);
                world = (WorldServer) new SecondaryWorldServer(this, idatamanager, dimension, this.worlds.get(0), this.methodProfiler, worlddata, org.bukkit.World.Environment.getEnvironment(dimension), gen).b();
            }

            this.server.getPluginManager().callEvent(new org.bukkit.event.world.WorldInitEvent(world.getWorld()));
            world.addIWorldAccess(new WorldManager(this, world));

            if (!this.T()) {
                world.getWorldData().setGameType(this.getGamemode());
            }

            worlds.add(world);
            getPlayerList().setPlayerFileData(worlds.toArray(new WorldServer[worlds.size()]));
        }

        this.a(this.getDifficulty());
        this.k();
    }

    protected void k() {
        this.b("menu.generatingTerrain");

        for (WorldServer world : this.worlds) {
            this.server.getPluginManager().callEvent(new org.bukkit.event.world.WorldLoadEvent(world.getWorld()));
        }

        this.s();
    }

    protected void a(String s, IDataManager idatamanager) {
        File file = new File(idatamanager.getDirectory(), "resources.zip");

        if (file.isFile()) {
            this.setResourcePack("level://" + s + "/" + file.getName(), "");
        }

    }

    public abstract boolean getGenerateStructures();

    public abstract WorldSettings.EnumGamemode getGamemode();

    public abstract EnumDifficulty getDifficulty();

    public abstract boolean isHardcore();

    public abstract int p();

    public abstract boolean q();

    public abstract boolean r();

    protected void a_(String s, int i) {
        this.f = s;
        this.g = i;
        MinecraftServer.LOGGER.info(s + ": " + i + "%");
    }

    protected void s() {
        this.f = null;
        this.g = 0;

        this.server.enablePlugins(org.bukkit.plugin.PluginLoadOrder.POSTWORLD);
    }

    protected void saveChunks(boolean flag) throws ExceptionWorldConflict {
        if (!this.N) {
            for (int j = 0; j < worlds.size(); ++j) {
                WorldServer worldserver = worlds.get(j);

                if (worldserver != null) {
                    if (!flag) {
                        MinecraftServer.LOGGER.info("Saving chunks for level \'" + worldserver.getWorldData().getName() + "\'/" + worldserver.worldProvider.getName());
                    }

                    try {
                        worldserver.save(true, null);
                        worldserver.saveLevel();
                    } catch (ExceptionWorldConflict exceptionworldconflict) {
                        MinecraftServer.LOGGER.warn(exceptionworldconflict.getMessage());
                    }
                }
            }

        }
    }

    private boolean hasStopped = false;
    private final Object stopLock = new Object();

    public void stop() throws ExceptionWorldConflict {
        synchronized(stopLock) {
            if (hasStopped) {
                return;
            }

            hasStopped = true;
        }

        if (!this.N) {
            MinecraftServer.LOGGER.info("Stopping server");
            SpigotTimings.stopServer();

            if (this.server != null) {
                this.server.disablePlugins();
            }

            if (this.aq() != null) {
                this.aq().b();
            }

            if (this.playerList != null) {
                MinecraftServer.LOGGER.info("Saving players");
                this.playerList.savePlayers();
                this.playerList.u();

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {}
            }

            if (this.worldServer != null) {
                MinecraftServer.LOGGER.info("Saving worlds");
                this.saveChunks(false);
            }

            if (this.n.d()) {
                this.n.e();
            }

            if (org.spigotmc.SpigotConfig.saveUserCacheOnStopOnly) {
                LOGGER.info("Saving usercache.json");
                this.Z.c();
            }
        }
    }

    public String getServerIp() {
        return this.serverIp;
    }

    public void setServerIp(String s) {
        this.serverIp = s;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public void safeShutdown() {
        this.isRunning = false;
    }

    public static int TPS = 20;
    public static final long SEC_IN_NANO = 1000000000;
    public static long TICK_TIME = SEC_IN_NANO / TPS;
    public static long NORMAL_TICK_TIME = TPS / 20;
    public static long MAX_CATCHUP_BUFFER = TICK_TIME * TPS * (TPS * 3);
    private static final int SAMPLE_INTERVAL = TPS;
    public final RollingAverage tps1 = new RollingAverage(60);
    public final RollingAverage tps5 = new RollingAverage(60 * 5);
    public final RollingAverage tps15 = new RollingAverage(60 * 15);

    public static class RollingAverage {

        private final int size;
        private long time;
        private double total;
        private int index = 0;
        private final double[] samples;
        private final long[] times;

        RollingAverage(int size) {
            this.size = size;
            this.time = size * SEC_IN_NANO;
            this.total = TPS * SEC_IN_NANO * size;
            this.samples = new double[size];
            this.times = new long[size];

            for (int i = 0; i < size; i++) {
                this.samples[i] = TPS;
                this.times[i] = SEC_IN_NANO;
            }
        }

        public void add(double x, long t) {
            time -= times[index];
            total -= samples[index] * times[index];
            samples[index] = x;
            times[index] = t;
            time += t;
            total += x * t;

            if (++index == size) {
                index = 0;
            }
        }

        public double getAverage() {
            return total / time;
        }

    }

    // PandaSpigot start - Modern tick loop
    public static long getMillis() {
        return getNanos() / 1000000L;
    }
    public static long getNanos() {
        return System.nanoTime();
    }

    public void run() {
        try {
            this.serverStartTime = getNanos();
            // PandaSpigot end
            if (this.init()) {
                this.ab = az();

                this.r.setMOTD(new ChatComponentText(this.motd));
                this.r.setServerInfo(new ServerPing.ServerData("1.8.8", 47));
                this.a(this.r);

                // PandaSpigot start - Modern tick loop
                long start = System.nanoTime(), curTime, tickSection = start;
                lastTick = start - TICK_TIME;
                // PandaSpigot end

                while (this.isRunning) {
                    // PandaSpigot start - Modern tick loop
                    long i = ((curTime = System.nanoTime()) / (1000L * 1000L)) - this.nextTickTime; // Paper
                    if (i > 5000L && this.nextTickTime - this.lastOverloadWarning >= 30000L && ticks > 500) { // CraftBukkit
                        long j = i / 50L;
                        if (this.server.getWarnOnOverload()) // CraftBukkit
                            MinecraftServer.LOGGER.warn("Can't keep up! Is the server overloaded? Running {}ms or {} ticks behind", i, j);
                        this.nextTickTime += j * 50L;
                        this.lastOverloadWarning = this.nextTickTime;
                    }

                    if ( ++MinecraftServer.currentTick % SAMPLE_INTERVAL == 0) {
                        final long diff = curTime - tickSection;
                        double currentTps = 1E9 / diff * SAMPLE_INTERVAL;

                        tps1.add(currentTps, diff);
                        tps5.add(currentTps, diff);
                        tps15.add(currentTps, diff);

                        tickSection = curTime;
                    }

                    lastTick = curTime;
	                LAST_TICK_TIME = System.currentTimeMillis();

                    this.nextTickTime += 50L;
                    this.methodProfiler.a("tick");
                    this.A(this::haveTime);
                    this.methodProfiler.c("nextTickWait");
                    this.mayHaveDelayedTasks = true;
                    this.delayedTasksMaxNextTickTime = Math.max(getMillis() + 50L, this.nextTickTime);
                    this.waitUntilNextTick();
                    this.methodProfiler.b();
                    this.isReady = true;
                    // PandaSpigot end
                }
            } else {
                this.a((CrashReport) null);
            }
        } catch (Throwable throwable) {
            MinecraftServer.LOGGER.error("Encountered an unexpected exception", throwable);

            if ( throwable.getCause() != null ) {
                MinecraftServer.LOGGER.error( "\tCause of unexpected exception was", throwable.getCause() );
            }

            CrashReport crashreport;

            if (throwable instanceof ReportedException) {
                crashreport = this.b(((ReportedException) throwable).a());
            } else {
                crashreport = this.b(new CrashReport("Exception in server tick loop", throwable));
            }

            File file = new File(new File(this.y(), "crash-reports"), "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-server.txt");

            if (crashreport.a(file)) {
                MinecraftServer.LOGGER.error("This crash report has been saved to: " + file.getAbsolutePath());
            } else {
                MinecraftServer.LOGGER.error("We were unable to save this crash report to disk.");
            }

            this.a(crashreport);
        } finally {
            try {
                org.spigotmc.WatchdogThread.doStop();
                this.isStopped = true;
                this.stop();
            } catch (Throwable throwable1) {
                MinecraftServer.LOGGER.error("Exception stopping the server", throwable1);
            } finally {
                try {
                    reader.getTerminal().restore();
                } catch (Exception ignored) {
                }

                this.z();
            }
        }

    }

    // PandaSpigot start - Modern tick loop
    private boolean haveTime() {
        if (isOversleep) return canOversleep();
        return this.forceTicks || this.runningTask() || getMillis() < (this.mayHaveDelayedTasks ? this.delayedTasksMaxNextTickTime : this.nextTickTime);
    }

    boolean isOversleep = false;
    private boolean canOversleep() {
        return this.mayHaveDelayedTasks && getMillis() < this.delayedTasksMaxNextTickTime;
    }

    private boolean canSleepForTickNoOversleep() {
        return this.forceTicks || this.runningTask() || getMillis() < this.nextTickTime;
    }

    private void executeModerately() {
        this.runAllRunnable();
        java.util.concurrent.locks.LockSupport.parkNanos("executing tasks", 1000L);
    }

    protected void waitUntilNextTick() {
        this.controlTerminate(() -> !this.canSleepForTickNoOversleep());
    }

    @Override
    protected com.minexd.spigot.tickloop.TasksPerTick packUpRunnable(Runnable runnable) {
        // anything that does try to post to main during watchdog crash, run on watchdog
        if (this.hasStopped && Thread.currentThread().equals(shutdownThread)) {
            runnable.run();
            runnable = () -> {};
        }
        return new com.minexd.spigot.tickloop.TasksPerTick(this.ticks, runnable);
    }

    @Override
    protected boolean shouldRun(com.minexd.spigot.tickloop.TasksPerTick task) {
        return task.getTick() + 3 < this.ticks || this.haveTime();
    }

    @Override
    public boolean drawRunnable() {
        boolean flag = this.pollTaskInternal();

        this.mayHaveDelayedTasks = flag;
        return flag;
    }

    private boolean pollTaskInternal() {
        return super.drawRunnable();
    }

    @Override
    public Thread getMainThread() {
        return serverThread;
    }
    // PandaSpigot end

    private void a(ServerPing serverping) {
        File file = this.d("server-icon.png");

        if (file.isFile()) {
            ByteBuf bytebuf = Unpooled.buffer();

            try {
                BufferedImage bufferedimage = ImageIO.read(file);

                Validate.validState(bufferedimage.getWidth() == 64, "Must be 64 pixels wide", new Object[0]);
                Validate.validState(bufferedimage.getHeight() == 64, "Must be 64 pixels high", new Object[0]);
                ImageIO.write(bufferedimage, "PNG", new ByteBufOutputStream(bytebuf));

                ByteBuf buffer = Base64.encode(bytebuf);

                serverping.setFavicon("data:image/png;base64," + buffer.toString(Charsets.UTF_8));
            } catch (Exception exception) {
                MinecraftServer.LOGGER.error("Couldn\'t load server icon", exception);
            } finally {
                bytebuf.release();
            }
        }
    }

    public File y() {
        return new File(".");
    }

    protected void a(CrashReport crashreport) {}

    protected void z() {}

    // PandaSpigot start - Modern tick loop
    protected void A(java.util.function.BooleanSupplier shouldKeepTicking) throws ExceptionWorldConflict { // CraftBukkit - added throws
        co.aikar.timings.TimingsManager.FULL_SERVER_TICK.startTiming(); // Spigot
        long i = System.nanoTime();
        isOversleep = true;
        this.controlTerminate(() -> !this.canOversleep());
        isOversleep = false;
        // PandaSpigot end

        ++this.ticks;

        if (this.T) {
            this.T = false;
            this.methodProfiler.a = true;
            this.methodProfiler.a();
        }

        this.methodProfiler.a("root");
        this.tickServer();

        if (i - this.X >= 5000000000L) {
            this.X = i;
            this.r.setPlayerSample(new ServerPing.ServerPingPlayerSample(this.J(), this.I()));
            GameProfile[] gameProfile = new GameProfile[Math.min(this.I(), 12)];
            int j = MathHelper.nextInt(this.s, 0, this.I() - gameProfile.length);

            for (int k = 0; k < gameProfile.length; ++k) {
                gameProfile[k] = (this.playerList.v().get(j + k)).getProfile();
            }

            Collections.shuffle(Arrays.asList(gameProfile));
            this.r.b().a(gameProfile);
        }

        if (autoSavePeriod > 0 && this.ticks % autoSavePeriod == 0) {
            SpigotTimings.worldSaveTimer.startTiming();
            this.methodProfiler.a("save");
            this.playerList.savePlayers();

            server.playerCommandState = true;

            for (World world : worlds) {
                world.getWorld().save(false);
            }

            server.playerCommandState = false;

            this.methodProfiler.b();
            SpigotTimings.worldSaveTimer.stopTiming();
        }

        // PandaSpigot start - Modern tick loop
        long endTime = System.nanoTime();
        long remaining = (TICK_TIME - (endTime - lastTick)) - catchupTime;
        // PandaSpigot end

        this.methodProfiler.a("tallying");
        this.h[this.ticks % 100] = System.nanoTime() - i;
        this.methodProfiler.b();
        this.methodProfiler.a("snooper");

        if (getSnooperEnabled() && !this.n.d() && this.ticks > 100) {
            this.n.a();
        }

        if (getSnooperEnabled() && this.ticks % 6000 == 0) {
            this.n.b();
        }

        this.methodProfiler.b();
        this.methodProfiler.b();
        org.spigotmc.WatchdogThread.tick();
        co.aikar.timings.TimingsManager.FULL_SERVER_TICK.stopTiming();
    }

    public void tickServer() {
        SpigotTimings.minecraftSchedulerTimer.startTiming();
        this.methodProfiler.a("jobs");

        FutureTask<?> entry;
        int count = this.j.size();

        while (count-- > 0 && (entry = this.j.poll()) != null) {
            SystemUtils.a(entry, MinecraftServer.LOGGER);
        }

        SpigotTimings.minecraftSchedulerTimer.stopTiming();

        this.methodProfiler.c("levels");

        // Tick main heartbeat
        SpigotTimings.bukkitSchedulerTimer.startTiming();
        this.server.getScheduler().mainThreadHeartbeat(this.ticks);
        SpigotTimings.bukkitSchedulerTimer.stopTiming();

        // Tick process queue
        SpigotTimings.processQueueTimer.startTiming();

        while (!processQueue.isEmpty()) {
            processQueue.remove().run();
        }

        SpigotTimings.processQueueTimer.stopTiming();

        // Tick chunks
        SpigotTimings.chunkIOTickTimer.startTiming();
        org.bukkit.craftbukkit.chunkio.ChunkIOExecutor.tick();
        SpigotTimings.chunkIOTickTimer.stopTiming();

        SpigotTimings.timeUpdateTimer.startTiming();

        // Send time updates to everyone, it will get the right time from the world the player is in.
        for (int i = 0; i < this.getPlayerList().players.size(); ++i) {
            final EntityPlayer entityplayer = this.getPlayerList().players.get(i);

            entityplayer.playerConnection.sendPacket(new PacketPlayOutUpdateTime(entityplayer.world.getTime(), entityplayer.getPlayerTime(), entityplayer.world.getGameRules().getBoolean("doDaylightCycle")));
        }

        SpigotTimings.timeUpdateTimer.stopTiming();

        int i;

        for (i = 0; i < this.worlds.size(); ++i) {
            WorldServer worldserver = this.worlds.get(i);

            this.methodProfiler.a(worldserver.getWorldData().getName());
            this.methodProfiler.a("tick");

            CrashReport crashreport;

            try {
                worldserver.timings.doTick.startTiming();
                worldserver.doTick(this.ticks);
                worldserver.timings.doTick.stopTiming();
            } catch (Throwable throwable) {
                try {
                    crashreport = CrashReport.a(throwable, "Exception ticking world");
                } catch (Throwable t){
                    throw new RuntimeException("Error generating crash report", t);
                }

                worldserver.a(crashreport);

                throw new ReportedException(crashreport);
            }

            try {
                if (this.ticks % DedicatedServer.NORMAL_TICK_TIME == 0) {
                    worldserver.timings.tickEntities.startTiming();
                    worldserver.tickEntities();
                    worldserver.timings.tickEntities.stopTiming();
                }
            } catch (Throwable throwable1) {
                try {
                    crashreport = CrashReport.a(throwable1, "Exception ticking world entities");
                } catch (Throwable t){
                    throw new RuntimeException("Error generating crash report", t);
                }

                worldserver.a(crashreport);

                throw new ReportedException(crashreport);
            }

            this.methodProfiler.b();
            this.methodProfiler.a("tracker");

            // Tick trackers
            worldserver.timings.tracker.startTiming();
            worldserver.getTracker().updatePlayers();
            worldserver.timings.tracker.stopTiming();

            this.methodProfiler.b();
            this.methodProfiler.b();
            worldserver.explosionDensityCache.clear();
        }

        // Tick connections
        this.methodProfiler.c("connection");
        SpigotTimings.connectionTimer.startTiming(); // Spigot
        this.aq().c();
        SpigotTimings.connectionTimer.stopTiming(); // Spigot

        // Tick players
        this.methodProfiler.c("players");
        SpigotTimings.playerListTimer.startTiming(); // Spigot
        this.playerList.tick();
        SpigotTimings.playerListTimer.stopTiming(); // Spigot

        // Tick tickables
        this.methodProfiler.c("tickables");
        SpigotTimings.tickablesTimer.startTiming();

        for (i = 0; i < this.p.size(); ++i) {
            this.p.get(i).c();
        }

        SpigotTimings.tickablesTimer.stopTiming();

        this.methodProfiler.b();
    }

    public boolean getAllowNether() {
        return true;
    }

    public void a(IUpdatePlayerListBox iupdateplayerlistbox) {
        this.p.add(iupdateplayerlistbox);
    }

    public static void main(final OptionSet options) {
        DispenserRegistry.c();

        try {
            /* // PandaSpigot start - comment out
            DedicatedServer dedicatedserver = new DedicatedServer(options);

            if (options.has("port")) {
                int port = (Integer) options.valueOf("port");

                if (port > 0) {
                    dedicatedserver.setPort(port);
                }
            }

            if (options.has("universe")) {
                dedicatedserver.universe = (File) options.valueOf("universe");
            }

            if (options.has("world")) {
                dedicatedserver.setWorld((String) options.valueOf("world"));
            }

            dedicatedserver.primaryThread.start();
            */ // PandaSpigot end
        } catch (Exception exception) {
            MinecraftServer.LOGGER.fatal("Failed to start the minecraft server", exception);
        }
    }

    public void C() {}

    public File d(String s) {
        return new File(this.y(), s);
    }

    public void info(String s) {
        MinecraftServer.LOGGER.info(s);
    }

    public void warning(String s) {
        MinecraftServer.LOGGER.warn(s);
    }

    public WorldServer getWorldServer(int i) {
        for (WorldServer world : worlds) {
            if (world.dimension == i) {
                return world;
            }
        }

        return worlds.get(0);
    }

    public String E() {
        return this.serverIp;
    }

    public int F() {
        return this.u;
    }

    public String G() {
        return this.motd;
    }

    public String getVersion() {
        return "1.8.8";
    }

    public int I() {
        return this.playerList.getPlayerCount();
    }

    public int J() {
        return this.playerList.getMaxPlayers();
    }

    public String[] getPlayers() {
        return this.playerList.f();
    }

    public GameProfile[] L() {
        return this.playerList.g();
    }

    public boolean isDebugging() {
        return this.getPropertyManager().getBoolean("debug", false);
    }

    public void g(String s) {
        MinecraftServer.LOGGER.error(s);
    }

    public void h(String s) {
        if (this.isDebugging()) {
            MinecraftServer.LOGGER.info(s);
        }

    }

    public String getServerModName() {
        return "Spigot";
    }

    public CrashReport b(CrashReport crashreport) {
        crashreport.g().a("Profiler Position", new Callable() {
            public String a() throws Exception {
                return MinecraftServer.this.methodProfiler.a ? MinecraftServer.this.methodProfiler.c() : "N/A (disabled)";
            }

            public Object call() throws Exception {
                return this.a();
            }
        });
        if (this.playerList != null) {
            crashreport.g().a("Player Count", new Callable() {
                public String a() {
                    return MinecraftServer.this.playerList.getPlayerCount() + " / " + MinecraftServer.this.playerList.getMaxPlayers() + "; " + MinecraftServer.this.playerList.v();
                }

                public Object call() throws Exception {
                    return this.a();
                }
            });
        }

        return crashreport;
    }

    public List<String> tabCompleteCommand(ICommandListener icommandlistener, String s, BlockPosition blockposition) {
        return server.tabComplete(icommandlistener, s, blockposition);
    }

    public static MinecraftServer getServer() {
        return MinecraftServer.l;
    }

    public boolean O() {
        return true;
    }

    public String getName() {
        return "Server";
    }

    public void sendMessage(IChatBaseComponent ichatbasecomponent) {
        MinecraftServer.LOGGER.info(ichatbasecomponent.c());
    }

    public boolean a(int i, String s) {
        return true;
    }

    public ICommandHandler getCommandHandler() {
        return this.b;
    }

    public KeyPair Q() {
        return this.H;
    }

    public int getPort() {
        return this.u;
    }

    public void setPort(int i) {
        this.u = i;
    }

    public String S() {
        return this.I;
    }

    public void i(String s) {
        this.I = s;
    }

    public boolean T() {
        return this.I != null;
    }

    public String U() {
        return this.worldName;
    }

    public void setWorld(String worldName) {
        this.worldName = worldName;
    }

    public void a(KeyPair keypair) {
        this.H = keypair;
    }

    public void a(EnumDifficulty enumdifficulty) {
        for (int i = 0; i < this.worlds.size(); ++i) {
            WorldServer worldserver = this.worlds.get(i);

            if (worldserver != null) {
                if (worldserver.getWorldData().isHardcore()) {
                    worldserver.getWorldData().setDifficulty(EnumDifficulty.HARD);
                    worldserver.setSpawnFlags(true, true);
                } else if (this.T()) {
                    worldserver.getWorldData().setDifficulty(enumdifficulty);
                    worldserver.setSpawnFlags(worldserver.getDifficulty() != EnumDifficulty.PEACEFUL, true);
                } else {
                    worldserver.getWorldData().setDifficulty(enumdifficulty);
                    worldserver.setSpawnFlags(this.getSpawnMonsters(), this.spawnAnimals);
                }
            }
        }

    }

    protected boolean getSpawnMonsters() {
        return true;
    }

    public boolean X() {
        return this.demoMode;
    }

    public void b(boolean flag) {
        this.demoMode = flag;
    }

    public Convertable getConvertable() {
        return this.convertable;
    }

    public void aa() {
        this.N = true;

        this.getConvertable().d();

        for (int i = 0; i < this.worlds.size(); ++i) {
            WorldServer worldserver = this.worlds.get(i);

            if (worldserver != null) {
                worldserver.saveLevel();
            }
        }

        this.getConvertable().e(this.worlds.get(0).getDataManager().g());
        this.safeShutdown();
    }

    public String getResourcePack() {
        return this.O;
    }

    public String getResourcePackHash() {
        return this.P;
    }

    public void setResourcePack(String s, String s1) {
        this.O = s;
        this.P = s1;
    }

    public void a(MojangStatisticsGenerator mojangstatisticsgenerator) {
        mojangstatisticsgenerator.a("whitelist_enabled", false);
        mojangstatisticsgenerator.a("whitelist_count", 0);

        if (this.playerList != null) {
            mojangstatisticsgenerator.a("players_current", this.I());
            mojangstatisticsgenerator.a("players_max", this.J());
            mojangstatisticsgenerator.a("players_seen", this.playerList.getSeenPlayers().length);
        }

        mojangstatisticsgenerator.a("uses_auth", this.onlineMode);
        mojangstatisticsgenerator.a("gui_state", this.as() ? "enabled" : "disabled");
        mojangstatisticsgenerator.a("run_time", (az() - mojangstatisticsgenerator.g()) / 60L * 1000L);
        mojangstatisticsgenerator.a("avg_tick_ms", (int) (MathHelper.a(this.h) * 1.0E-6D));

        int i = 0;

        if (this.worldServer != null) {
            for (int j = 0; j < this.worlds.size(); ++j) {
                WorldServer worldserver = this.worlds.get(j);

                if (worldserver != null) {
                    WorldData worlddata = worldserver.getWorldData();

                    mojangstatisticsgenerator.a("world[" + i + "][dimension]", worldserver.worldProvider.getDimension());
                    mojangstatisticsgenerator.a("world[" + i + "][mode]", worlddata.getGameType());
                    mojangstatisticsgenerator.a("world[" + i + "][difficulty]", worldserver.getDifficulty());
                    mojangstatisticsgenerator.a("world[" + i + "][hardcore]", worlddata.isHardcore());
                    mojangstatisticsgenerator.a("world[" + i + "][generator_name]", worlddata.getType().name());
                    mojangstatisticsgenerator.a("world[" + i + "][generator_version]", worlddata.getType().getVersion());
                    mojangstatisticsgenerator.a("world[" + i + "][height]", this.maxBuildHeight);
                    mojangstatisticsgenerator.a("world[" + i + "][chunks_loaded]", worldserver.N().getLoadedChunks());

                    ++i;
                }
            }
        }

        mojangstatisticsgenerator.a("worlds", i);
    }

    public void b(MojangStatisticsGenerator mojangstatisticsgenerator) {
        mojangstatisticsgenerator.b("singleplayer", this.T());
        mojangstatisticsgenerator.b("server_brand", this.getServerModName());
        mojangstatisticsgenerator.b("gui_supported", GraphicsEnvironment.isHeadless() ? "headless" : "supported");
        mojangstatisticsgenerator.b("dedicated", this.ae());
    }

    public boolean getSnooperEnabled() {
        return true;
    }

    public abstract boolean ae();

    public boolean getOnlineMode() {
        return server.getOnlineMode();
    }

    public void setOnlineMode(boolean flag) {
        this.onlineMode = flag;
    }

    public boolean getSpawnAnimals() {
        return this.spawnAnimals;
    }

    public void setSpawnAnimals(boolean flag) {
        this.spawnAnimals = flag;
    }

    public boolean getSpawnNPCs() {
        return this.spawnNPCs;
    }

    public abstract boolean ai();

    public void setSpawnNPCs(boolean flag) {
        this.spawnNPCs = flag;
    }

    public boolean getPVP() {
        return this.pvpMode;
    }

    public void setPVP(boolean flag) {
        this.pvpMode = flag;
    }

    public boolean getAllowFlight() {
        return this.allowFlight;
    }

    public void setAllowFlight(boolean flag) {
        this.allowFlight = flag;
    }

    public abstract boolean getEnableCommandBlock();

    public String getMotd() {
        return this.motd;
    }

    public void setMotd(String s) {
        this.motd = s;
    }

    public int getMaxBuildHeight() {
        return this.maxBuildHeight;
    }

    public void setMaxBuildHeight(int i) {
        this.maxBuildHeight = i;
    }

    public boolean isStopped() {
        return this.isStopped;
    }

    public PlayerList getPlayerList() {
        return this.playerList;
    }

    public void a(PlayerList playerlist) {
        this.playerList = playerlist;
    }

    public void setGamemode(WorldSettings.EnumGamemode worldsettings_enumgamemode) {
        // CraftBukkit start
        for (int i = 0; i < this.worlds.size(); ++i) {
            getServer().worlds.get(i).getWorldData().setGameType(worldsettings_enumgamemode);
        }

    }

    public ServerConnection getServerConnection() {
        return this.serverConnection;
    }

    public ServerConnection aq() {
        return this.serverConnection == null ? this.serverConnection = new ServerConnection(this) : this.serverConnection; // Spigot
    }

    public boolean as() {
        return false;
    }

    public abstract String a(WorldSettings.EnumGamemode worldsettings_enumgamemode, boolean flag);

    public int at() {
        return this.ticks;
    }

    public void au() {
        this.T = true;
    }

    public BlockPosition getChunkCoordinates() {
        return BlockPosition.ZERO;
    }

    public Vec3D d() {
        return new Vec3D(0.0D, 0.0D, 0.0D);
    }

    public World getWorld() {
        return this.worlds.get(0);
    }

    public Entity f() {
        return null;
    }

    public int getSpawnProtection() {
        return 16;
    }

    public boolean a(World world, BlockPosition blockposition, EntityHuman entityhuman) {
        return false;
    }

    public void setForceGamemode(boolean flag) {
        this.U = flag;
    }

    public boolean getForceGamemode() {
        return this.U;
    }

    public Proxy ay() {
        return this.e;
    }

    public static long az() {
        return getMillis(); // PandaSpigot - Modern tick loop
    }

    public int getIdleTimeout() {
        return this.G;
    }

    public void setIdleTimeout(int i) {
        this.G = i;
    }

    public IChatBaseComponent getScoreboardDisplayName() {
        return new ChatComponentText(this.getName());
    }

    public boolean aB() {
        return true;
    }

    public MinecraftSessionService aD() {
        return this.W;
    }

    public GameProfileRepository getGameProfileRepository() {
        return this.Y;
    }

    public UserCache getUserCache() {
        return this.Z;
    }

    public ServerPing aG() {
        return this.r;
    }

    public void aH() {
        this.X = 0L;
    }

    public Entity a(UUID uuid) {
        for (int j = 0; j < worlds.size(); ++j) {
            WorldServer worldserver = worlds.get(j);

            if (worldserver != null) {
                Entity entity = worldserver.getEntity(uuid);

                if (entity != null) {
                    return entity;
                }
            }
        }

        return null;
    }

    public boolean getSendCommandFeedback() {
        return getServer().worlds.get(0).getGameRules().getBoolean("sendCommandFeedback");
    }

    public void a(CommandObjectiveExecutor.EnumCommandResult result, int i) {}

    public int aI() {
        return 29999984;
    }

    public <V> ListenableFuture<V> a(Callable<V> callable) {
        Validate.notNull(callable);

        if (!this.isMainThread()) {
            ListenableFutureTask task = ListenableFutureTask.create(callable);

            this.j.add(task);

            return task;
        } else {
            try {
                return Futures.immediateFuture(callable.call());
            } catch (Exception exception) {
                return Futures.immediateFailedCheckedFuture(exception);
            }
        }
    }

    public ListenableFuture<Object> postToMainThread(Runnable runnable) {
        Validate.notNull(runnable);
        return this.a(Executors.callable(runnable));
    }

    public boolean isMainThread() {
        return Thread.currentThread() == this.serverThread;
    }

    public int aK() {
        return 256;
    }

    public long aL() {
        return this.ab;
    }

    public Thread aM() {
        return this.serverThread;
    }
}
