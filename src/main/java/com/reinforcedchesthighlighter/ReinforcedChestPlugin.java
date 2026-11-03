package com.reinforcedchesthighlighter;

import com.google.inject.Provides;
import java.awt.Color;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Tile;
import net.runelite.api.WorldView;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

@PluginDescriptor(
    name = "Reinforced Chest Highlighter",
    description = "Highlight a reinforced chest according to its current object ID",
    tags = {"chest", "sailing", "object", "highlight"}
)
public class ReinforcedChestPlugin extends Plugin
{
    // Fixed IDs: kept in code so the RuneLite settings have no ID entry boxes.
    private static final int IDLE_CHEST_ID = 60517;
    private static final int OPENED_CHEST_ID = 60519;
    // OSRS game message reported for the reinforced chest's teleport trap.
    private static final String TRAP_MESSAGE = "you have activated a trap on the chest.";
    private static final long ALERT_DEBOUNCE_NANOS = 1_000_000_000L;

    @Inject private Client client;
    @Inject private ClientThread clientThread;
    @Inject private OverlayManager overlayManager;
    @Inject private ReinforcedChestOverlay overlay;
    @Inject private ChestTrapAlertOverlay trapAlertOverlay;
    @Inject private ReinforcedChestConfig config;

    // Identity-based tracking avoids treating two different objects on one tile as equal.
    private final Set<GameObject> chests = Collections.newSetFromMap(new IdentityHashMap<>());
    private volatile long lastTrapAlertNanos = 0;

    @Provides
    ReinforcedChestConfig provideConfig(ConfigManager manager)
    {
        return manager.getConfig(ReinforcedChestConfig.class);
    }

    @Override
    protected void startUp()
    {
        overlayManager.add(overlay);
        overlayManager.add(trapAlertOverlay);
        // Also finds chests already nearby when the plugin is enabled.
        clientThread.invoke(this::scanCurrentScene);
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(overlay);
        overlayManager.remove(trapAlertOverlay);
        lastTrapAlertNanos = 0;
        chests.clear();
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event)
    {
        trackIfRelevant(event.getGameObject());
    }

    @Subscribe
    public void onGameObjectDespawned(GameObjectDespawned event)
    {
        chests.remove(event.getGameObject());
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() == GameState.LOADING ||
            event.getGameState() == GameState.LOGIN_SCREEN ||
            event.getGameState() == GameState.HOPPING)
        {
            chests.clear();
            // Preserve a live alert through LOADING during an island teleport.
            // Logging out or hopping cancels the alert.
            if (event.getGameState() != GameState.LOADING)
            {
                lastTrapAlertNanos = 0;
            }
        }
        else if (event.getGameState() == GameState.LOGGED_IN)
        {
            scanCurrentScene();
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event)
    {
        // Only messages originating from the game, never public or private player chat.
        if (client.getGameState() != GameState.LOGGED_IN ||
            (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM))
        {
            return;
        }

        String message = event.getMessage();
        if (message == null || !Text.standardize(message).contains(TRAP_MESSAGE))
        {
            return;
        }

        // Prevent duplicate game-message events from playing multiple sounds.
        long now = System.nanoTime();
        if (lastTrapAlertNanos != 0 && now - lastTrapAlertNanos < ALERT_DEBOUNCE_NANOS)
        {
            return;
        }
        lastTrapAlertNanos = now;

        if (config.trapSoundEnabled() && config.trapSoundVolume() > 0)
        {
            client.playSoundEffect(config.trapSoundId(), config.trapSoundVolume());
        }
    }

    // Called from the overlay renderer. The flash is based on elapsed time,
    // so it will end even if game ticks temporarily stop during a teleport.
    int getTrapFlashAlpha()
    {
        if (!config.trapFlashEnabled() || lastTrapAlertNanos == 0)
        {
            return 0;
        }

        long elapsed = System.nanoTime() - lastTrapAlertNanos;
        long duration = Math.max(1, Math.min(10, config.trapFlashSeconds())) * 1_000_000_000L;
        if (elapsed < 0 || elapsed >= duration)
        {
            return 0;
        }

        long interval = Math.max(350, Math.min(1200, config.trapFlashIntervalMs())) * 1_000_000L;
        return (elapsed / interval) % 2 == 0
            ? Math.max(0, Math.min(180, config.trapFlashOpacity()))
            : 0;
    }

    Set<GameObject> getChests()
    {
        return chests;
    }

    // Resolve impostors each time we draw: a chest can change appearance/ID
    // when a varbit changes without producing an object-spawn event.
    Color getColour(GameObject object)
    {
        int id = object.getId();
        ObjectComposition definition = client.getObjectDefinition(id);
        if (definition != null && definition.getImpostorIds() != null)
        {
            ObjectComposition current = definition.getImpostor();
            if (current == null)
            {
                return null;
            }
            id = current.getId();
        }

        if (id == OPENED_CHEST_ID)
        {
            return config.highlightOpened() ? config.openedColor() : null;
        }
        if (id == IDLE_CHEST_ID)
        {
            return config.highlightIdle() ? config.idleColor() : null;
        }
        return null;
    }

    private void trackIfRelevant(GameObject object)
    {
        if (object == null)
        {
            return;
        }

        int id = object.getId();
        if (id == IDLE_CHEST_ID || id == OPENED_CHEST_ID)
        {
            chests.add(object);
            return;
        }

        // Some scenery has a stable base ID but switches visible IDs (impostors).
        ObjectComposition definition = client.getObjectDefinition(id);
        if (definition != null && definition.getImpostorIds() != null)
        {
            for (int candidateId : definition.getImpostorIds())
            {
                if (candidateId == IDLE_CHEST_ID || candidateId == OPENED_CHEST_ID)
                {
                    chests.add(object);
                    return;
                }
            }
        }
    }

    private void scanCurrentScene()
    {
        chests.clear();
        if (client.getGameState() != GameState.LOGGED_IN)
        {
            return;
        }

        WorldView worldView = client.getTopLevelWorldView();
        if (worldView == null || worldView.getScene() == null)
        {
            return;
        }

        Tile[][][] tiles = worldView.getScene().getTiles();
        if (tiles == null)
        {
            return;
        }

        for (Tile[][] plane : tiles)
        {
            for (Tile[] column : plane)
            {
                for (Tile tile : column)
                {
                    if (tile != null && tile.getGameObjects() != null)
                    {
                        for (GameObject object : tile.getGameObjects())
                        {
                            trackIfRelevant(object);
                        }
                    }
                }
            }
        }
    }
}
