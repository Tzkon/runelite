package net.runelite.client.plugins.followerTransmog;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(FollowerConfig.GROUP)
public interface FollowerConfig extends Config {
    String GROUP = "followerTransmog";

    @ConfigItem(
            keyName = "selectedNpc",
            name = "Select NPC",
            description = "Select an NPC to transmog into",
            position = 0
    )
    default TransmogData selectedNpc() {
        return TransmogData.CUSTOM;
    }

    @ConfigItem(
            keyName = "npcModelID1",
            name = "NPC Model ID1",
            description = "Enter the model ID of the NPC you want to overlay",
            position = 1
    )
    default int npcModelID1()
    {
        return -1; // Default model ID
    }

    @ConfigItem(
            keyName = "npcModelID2",
            name = "NPC Model ID2",
            description = "Enter the model ID of the NPC you want to overlay",
            position = 2
    )
    default int npcModelID2()
    {
        return -1; // Default model ID
    }


    @ConfigItem(
            keyName = "npcModelID3",
            name = "NPC Model ID3",
            description = "Enter the model ID of the NPC you want to overlay",
            position = 3
    )
    default int npcModelID3()
    {
        return -1; // Default model ID
    }

    @ConfigItem(
            keyName = "npcModelID4",
            name = "NPC Model ID4",
            description = "Enter the model ID of the NPC you want to overlay",
            position = 4
    )
    default int npcModelID4()
    {
        return -1; // Default model ID
    }

    @ConfigItem(
            keyName = "npcModelID5",
            name = "NPC Model ID5",
            description = "Enter the model ID of the NPC you want to overlay",
            position = 5
    )
    default int npcModelID5()
    {
        return -1; // Default model ID
    }

    @ConfigItem(
            keyName = "npcModelID6",
            name = "NPC Model ID6",
            description = "Enter the model ID of the NPC you want to overlay",
            position = 6
    )
    default int npcModelID6()
    {
        return -1; // Default model ID
    }

    @ConfigItem(
            keyName = "npcModelID7",
            name = "NPC Model ID7",
            description = "Enter the model ID of the NPC you want to overlay",
            position = 7
    )
    default int npcModelID7()
    {
        return -1; // Default model ID
    }

    @ConfigItem(
            keyName = "npcModelID8",
            name = "NPC Model ID8",
            description = "Enter the model ID of the NPC you want to overlay",
            position = 8
    )
    default int npcModelID8()
    {
        return -1; // Default model ID
    }

    @ConfigItem(
            keyName = "npcModelID9",
            name = "NPC Model ID9",
            description = "Enter the model ID of the NPC you want to overlay",
            position = 9
    )
    default int npcModelID9()
    {
        return -1; // Default model ID
    }

    @ConfigItem(
            keyName = "npcModelID10",
            name = "NPC Model ID10",
            description = "Enter the model ID of the NPC you want to overlay",
            position = 10
    )
    default int npcModelID10()
    {
        return -1; // Default model ID
    }


    @ConfigItem(
            keyName = "standingAnimationId",
            name = "Standing Animation ID",
            description = "Enter the standing animation ID for the NPC",
            position = 11
    )
    default int standingAnimationId()
    {
        return -1; // Default standing animation ID
    }

    @ConfigItem(
            keyName = "walkingAnimationId",
            name = "Walking Animation ID",
            description = "Enter the walking animation ID for the NPC",
            position = 12
    )
    default int walkingAnimationId()
    {
        return -1; // Default standing animation ID
    }

    @ConfigItem(
            keyName = "transmogRadius",
            name = "Transmog Radius",
            description = "Set the radius for the model",
            position = 13
    )
    default int transmogRadius() {
        return 320; // Default radius
    }

    @ConfigItem(
            keyName = "offsetX",
            name = "Offset X",
            description = "Horizontal offset for the model",
            position = 14
    )
    default int offsetX() {
        return 0; // Default horizontal offset
    }

    @ConfigItem(
            keyName = "offsetY",
            name = "Offset Y",
            description = "Vertical offset for the model",
            position = 15
    )
    default int offsetY() {
        return 0; // Default vertical offset
    }
}
