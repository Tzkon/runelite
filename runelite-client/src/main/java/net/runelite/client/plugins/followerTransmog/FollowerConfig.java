package net.runelite.client.plugins.followerTransmog;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(FollowerConfig.GROUP)
public interface FollowerConfig extends Config
{
	String GROUP = "followerTransmog";

	@ConfigItem(
		keyName = "selectedNpc",
		name = "Select NPC",
		description = "Select an NPC to transmog into",
		position = 0
	)
	default TransmogData selectedNpc()
	{
		return TransmogData.GnomeChild;
	}

	@ConfigItem(
		keyName = "enableCustom",
		name = "Enable Custom Configuration",
		description = "Check this box to enable custom configuration",
		position = 1
	)
	default boolean enableCustom()
	{
		return false;
	}

	@ConfigItem(
		keyName = "npcModelID1",
		name = "CUSTOM ModelID1",
		description = "Enter the model ID of the NPC you want to overlay",
		position = 2
	)
	default int npcModelID1()
	{
		return -1; // Default model ID
	}

	@ConfigItem(
		keyName = "npcModelID2",
		name = "CUSTOM ModelID2",
		description = "Enter the model ID of the NPC you want to overlay",
		position = 3
	)
	default int npcModelID2()
	{
		return -1; // Default model ID
	}


	@ConfigItem(
		keyName = "npcModelID3",
		name = "CUSTOM ModelID3",
		description = "Enter the model ID of the NPC you want to overlay",
		position = 4
	)
	default int npcModelID3()
	{
		return -1; // Default model ID
	}

	@ConfigItem(
		keyName = "npcModelID4",
		name = "CUSTOM ModelID4",
		description = "Enter the model ID of the NPC you want to overlay",
		position = 5
	)
	default int npcModelID4()
	{
		return -1; // Default model ID
	}

	@ConfigItem(
		keyName = "npcModelID5",
		name = "CUSTOM ModelID5",
		description = "Enter the model ID of the NPC you want to overlay",
		position = 6
	)
	default int npcModelID5()
	{
		return -1; // Default model ID
	}

	@ConfigItem(
		keyName = "npcModelID6",
		name = "CUSTOM ModelID6",
		description = "Enter the model ID of the NPC you want to overlay",
		position = 7
	)
	default int npcModelID6()
	{
		return -1; // Default model ID
	}

	@ConfigItem(
		keyName = "npcModelID7",
		name = "CUSTOM ModelID7",
		description = "Enter the model ID of the NPC you want to overlay",
		position = 8
	)
	default int npcModelID7()
	{
		return -1; // Default model ID
	}

	@ConfigItem(
		keyName = "npcModelID8",
		name = "CUSTOM ModelID8",
		description = "Enter the model ID of the NPC you want to overlay",
		position = 9
	)
	default int npcModelID8()
	{
		return -1; // Default model ID
	}

	@ConfigItem(
		keyName = "npcModelID9",
		name = "CUSTOM ModelID9",
		description = "Enter the model ID of the NPC you want to overlay",
		position = 10
	)
	default int npcModelID9()
	{
		return -1; // Default model ID
	}

	@ConfigItem(
		keyName = "npcModelID10",
		name = "CUSTOM ModelID10",
		description = "Enter the model ID of the NPC you want to overlay",
		position = 11
	)
	default int npcModelID10()
	{
		return -1; // Default model ID
	}


	@ConfigItem(
		keyName = "standingAnimationId",
		name = "Standing Animation ID",
		description = "Enter the standing animation ID for the NPC",
		position = 12
	)
	default int standingAnimationId()
	{
		return -1; // Default standing animation ID
	}

	@ConfigItem(
		keyName = "walkingAnimationId",
		name = "Walking Animation ID",
		description = "Enter the walking animation ID for the NPC",
		position = 13
	)
	default int walkingAnimationId()
	{
		return -1; // Default standing animation ID
	}

	@ConfigItem(
		keyName = "transmogRadius",
		name = "Transmog Radius",
		description = "Set the radius for the model",
		position = 14
	)
	default int transmogRadius()
	{
		return 60; // Default radius
	}

	@ConfigItem(
		keyName = "offsetX",
		name = "Offset X",
		description = "Horizontal offset for the model",
		position = 15
	)
	default int offsetX()
	{
		return 0; // Default horizontal offset
	}

	@ConfigItem(
		keyName = "offsetY",
		name = "Offset Y",
		description = "Vertical offset for the model",
		position = 16
	)
	default int offsetY()
	{
		return 0; // Default vertical offset
	}
}
