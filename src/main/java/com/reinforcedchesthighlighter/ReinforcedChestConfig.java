package com.reinforcedchesthighlighter;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("reinforcedchesthighlighter")
public interface ReinforcedChestConfig extends Config
{
    @ConfigSection(
        name = "Chest",
        description = "Chest highlight settings",
        position = 0
    )
    String chestSection = "chest";

    @ConfigSection(
        name = "TP Alarm",
        description = "Alerts for the chest teleport trap",
        position = 1,
        closedByDefault = true
    )
    String trapAlarmSection = "trapAlarm";

    enum HighlightStyle
    {
        HULL_OUTLINE,
        FILLED_HULL,
        TILE,
        MODEL_OUTLINE
    }

    @ConfigItem(
        position = 1,
        keyName = "highlightIdle",
        name = "Show idle chest",
        description = "Highlight the chest before it's opened.",
        section = chestSection
    )
    default boolean highlightIdle()
    {
        return true;
    }

    @Alpha
    @ConfigItem(
        position = 2,
        keyName = "idleColor",
        name = "Idle colour",
        description = "Highlight colour for the idle chest.",
        section = chestSection
    )
    default Color idleColor()
    {
        return Color.GREEN;
    }

    @ConfigItem(
        position = 3,
        keyName = "highlightOpened",
        name = "Show opened chest",
        description = "Highlight the chest after it's been opened.",
        section = chestSection
    )
    default boolean highlightOpened()
    {
        return true;
    }

    @Alpha
    @ConfigItem(
        position = 4,
        keyName = "openedColor",
        name = "Opened colour",
        description = "Highlight colour for the opened chest.",
        section = chestSection
    )
    default Color openedColor()
    {
        return Color.RED;
    }

    @ConfigItem(
        position = 5,
        keyName = "highlightStyle",
        name = "Highlight style",
        description = "Choose how the chest is highlighted.",
        section = chestSection
    )
    default HighlightStyle highlightStyle()
    {
        return HighlightStyle.HULL_OUTLINE;
    }

    @Range(min = 1, max = 8)
    @ConfigItem(
        position = 6,
        keyName = "borderWidth",
        name = "Line width",
        description = "Outline thickness in pixels.",
        section = chestSection
    )
    default int borderWidth()
    {
        return 2;
    }

    @Range(min = 0, max = 255)
    @ConfigItem(
        position = 7,
        keyName = "fillOpacity",
        name = "Fill opacity",
        description = "Opacity used with the Filled Hull style.",
        section = chestSection
    )
    default int fillOpacity()
    {
        return 45;
    }

    @ConfigItem(
        position = 10,
        keyName = "trapFlashEnabled",
        name = "Screen flash",
        description = "Flash the screen when the chest trap activates.",
        section = trapAlarmSection
    )
    default boolean trapFlashEnabled()
    {
        return true;
    }

    @ConfigItem(
        position = 11,
        keyName = "trapFlashColor",
        name = "Flash colour",
        description = "Colour of the screen flash.",
        section = trapAlarmSection
    )
    default Color trapFlashColor()
    {
        return Color.RED;
    }

    @Range(min = 0, max = 180)
    @ConfigItem(
        position = 12,
        keyName = "trapFlashOpacity",
        name = "Flash strength",
        description = "How strong the screen flash appears.",
        section = trapAlarmSection
    )
    default int trapFlashOpacity()
    {
        return 55;
    }

    @Range(min = 1, max = 10)
    @ConfigItem(
        position = 13,
        keyName = "trapFlashSeconds",
        name = "Flash duration",
        description = "How long the flash lasts, in seconds.",
        section = trapAlarmSection
    )
    default int trapFlashSeconds()
    {
        return 3;
    }

    @Range(min = 350, max = 1200)
    @ConfigItem(
        position = 14,
        keyName = "trapFlashIntervalMs",
        name = "Flash interval (ms)",
        description = "Time between on/off changes; higher is slower.",
        section = trapAlarmSection
    )
    default int trapFlashIntervalMs()
    {
        return 600;
    }

    @ConfigItem(
        position = 15,
        keyName = "trapSoundEnabled",
        name = "Play sound",
        description = "Play a sound when the chest trap activates.",
        section = trapAlarmSection
    )
    default boolean trapSoundEnabled()
    {
        return true;
    }

    @Range(min = 1, max = 65535)
    @ConfigItem(
        position = 16,
        keyName = "trapSoundId",
        name = "Sound ID",
        description = "RuneScape sound effect ID. Default: 3813.",
        section = trapAlarmSection
    )
    default int trapSoundId()
    {
        return 3813;
    }

    @Range(min = 0, max = 127)
    @ConfigItem(
        position = 17,
        keyName = "trapSoundVolume",
        name = "Sound volume",
        description = "Alert volume, from 0 to 127.",
        section = trapAlarmSection
    )
    default int trapSoundVolume()
    {
        return 64;
    }
}
