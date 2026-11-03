package com.reinforcedchesthighlighter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/** A short, transparent full-canvas flash when the chest's teleport trap activates. */
public class ChestTrapAlertOverlay extends Overlay
{
    private final Client client;
    private final ReinforcedChestPlugin plugin;
    private final ReinforcedChestConfig config;

    @Inject
    public ChestTrapAlertOverlay(Client client, ReinforcedChestPlugin plugin, ReinforcedChestConfig config)
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPriority(PRIORITY_HIGH);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        int alpha = plugin.getTrapFlashAlpha();
        if (alpha <= 0)
        {
            return null;
        }

        Color configured = config.trapFlashColor();
        if (configured == null)
        {
            return null;
        }
        graphics.setColor(new Color(configured.getRed(), configured.getGreen(), configured.getBlue(), alpha));
        graphics.fillRect(0, 0, client.getCanvasWidth(), client.getCanvasHeight());
        return null;
    }
}
