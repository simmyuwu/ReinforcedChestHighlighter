package com.reinforcedchesthighlighter;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ReinforcedChestPluginTest
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(ReinforcedChestPlugin.class);
        RuneLite.main(args);
    }
}
