package com.reinforcedchesthighlighter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.WorldView;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

public class ReinforcedChestOverlay extends Overlay
{
    private final Client client;
    private final ReinforcedChestPlugin plugin;
    private final ReinforcedChestConfig config;
    private final ModelOutlineRenderer modelOutlineRenderer;

    @Inject
    public ReinforcedChestOverlay(Client client, ReinforcedChestPlugin plugin,
        ReinforcedChestConfig config, ModelOutlineRenderer modelOutlineRenderer)
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.modelOutlineRenderer = modelOutlineRenderer;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        WorldView worldView = client.getTopLevelWorldView();
        if (worldView == null)
        {
            return null;
        }

        ReinforcedChestConfig.HighlightStyle style = config.highlightStyle();
        int width = Math.max(1, Math.min(8, config.borderWidth()));

        for (GameObject chest : plugin.getChests())
        {
            if (chest.getWorldView() != worldView || chest.getPlane() != worldView.getPlane())
            {
                continue;
            }

            Color colour = plugin.getColour(chest);
            if (colour == null)
            {
                continue;
            }

            if (style == ReinforcedChestConfig.HighlightStyle.MODEL_OUTLINE)
            {
                modelOutlineRenderer.drawOutline(chest, width, colour, 0);
                continue;
            }

            Shape shape = style == ReinforcedChestConfig.HighlightStyle.TILE
                ? chest.getCanvasTilePoly()
                : chest.getConvexHull();
            if (shape == null)
            {
                shape = chest.getCanvasTilePoly();
            }
            if (shape == null)
            {
                continue;
            }

            int alpha = style == ReinforcedChestConfig.HighlightStyle.FILLED_HULL
                ? Math.max(0, Math.min(255, config.fillOpacity()))
                : 0;
            Color fill = new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), alpha);
            OverlayUtil.renderPolygon(graphics, shape, colour, fill, new BasicStroke(width));
        }
        return null;
    }
}
