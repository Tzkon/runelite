package net.runelite.client.plugins.companion;

import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.Hooks;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.callback.ClientThread;
import javax.inject.Inject;
import java.util.*;

@PluginDescriptor(
        name = "Companions",
        description = "turn a NPC into a companion"
)
public class CompanionPlugin extends Plugin {
    @Inject private Client client;
    @Inject private CompanionConfig config;
    @Inject private ClientThread clientThread;
    @Inject private Hooks hooks;

    private boolean transmogInitialized = false;
    private LocalPoint lastFollowerLocation;
    private final Hooks.RenderableDrawListener drawListener = this::shouldDraw;
    private boolean hidePets = true;
    private List<RuneLiteObject> transmogObjects;
    private List<Integer> modelIds;
    private int previousWalkingFrame = -1;
    private int previousStandingFrame = -1;
    private boolean isMoving = false;
    private boolean previouslyMoved = false;
    private int currentFrame;

    @Provides
    CompanionConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(CompanionConfig.class);
    }

    @Override
    protected void startUp() {
        hooks.registerRenderableDrawListener(drawListener);
        System.out.println("followerTransmog plugin started!");
    }

    @Override
    protected void shutDown() {
        hooks.unregisterRenderableDrawListener(drawListener);
        transmogInitialized = false;
        isMoving = false;
        previouslyMoved = false;
        transmogObjects = null;
        previousWalkingFrame = -1;
        previousStandingFrame = -1;
        System.out.println("Logged off Runelite");
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (client.getGameState() != GameState.LOGGED_IN) {

//            hooks.unregisterRenderableDrawListener(drawListener);
            transmogInitialized = false;
            transmogObjects = null;
            System.out.println("game state reset");
        }
    }


    @Subscribe
    public void onClientTick(ClientTick event) {
        Player player = client.getLocalPlayer();

        if (player != null) {
            if (transmogObjects == null) {
                transmogObjects = new ArrayList<>();
            }
            if (!transmogInitialized) {
                RuneLiteObject transmogObject = initializeTransmogObject();
                if (transmogObject != null) {
                    transmogObjects.add(transmogObject);
                    transmogInitialized = true;
                } else {
                    System.out.println("Failed to initialize transmog object.");
                    return;
                }
            }
            // Update the position and state of the transmog object here if necessary
//            updateTransmogObject();
            updateFollowerMovement(player);
        }
    }



    private RuneLiteObject initializeTransmogObject() {
        RuneLiteObject transmogObject = client.createRuneLiteObject();
        if (transmogObject != null) {
            Model mergedModel = createNpcModel();
            if (mergedModel != null) {
                transmogObject.setModel(mergedModel);
                transmogObjects.add(transmogObject);
                transmogObject.setActive(true);

                if (config.selectedNpc() == CompanionData.CUSTOM) {
                    transmogObject.setDrawFrontTilesFirst(config.drawFrontTilesFirst());
                    transmogObject.setRadius(config.transmogRadius());
                }
            }
        }
        return transmogObject;
    }




    private void updateFollowerMovement(Player player) {
        LocalPoint currentLocation = player.getLocalLocation();
        boolean isFollowerMoving = lastFollowerLocation != null && !currentLocation.equals(lastFollowerLocation);

        lastFollowerLocation = currentLocation;
        if (isFollowerMoving) {
            isMoving = true;
            handleWalkingAnimation(player);
            // Update the transmog object's location to the player's location when the player is moving
            updateTransmogObject(currentLocation);
        } else {
            isMoving = false;
            handleStandingAnimation(player);
            // Update the transmog object's location to the specific location when the player is not moving
            WorldPoint specificTile = new WorldPoint(3212, 6084, client.getPlane());
            LocalPoint specificLocation = LocalPoint.fromWorld(client, specificTile);
            updateTransmogObject(specificLocation);
        }
    }



    private void handleWalkingAnimation(Player player) {
        CompanionData selectedNpc = config.selectedNpc();
        if (selectedNpc == null) {
            System.out.println("No NPC selected for walking animation.");
            return;
        }

        int walkingAnimationId = (selectedNpc == CompanionData.CUSTOM)
                ? config.walkingAnimationId()
                : selectedNpc.getWalkAnim();

        Animation walkingAnimation = client.loadAnimation(walkingAnimationId);
        if (walkingAnimation == null) {
            System.out.println("Failed to load walking animation for ID: " + walkingAnimationId);
            return;
        }

        Player playerWalking = client.getLocalPlayer();
        if (playerWalking == null) {
            System.out.println("No follower found for walking animation.");
            return;
        }

        transmogObjects.forEach(transmogObject -> {
            if (transmogObject != null) {
                currentFrame = transmogObject.getAnimationFrame();
                transmogObject.setActive(true);
                transmogObject.setShouldLoop(true);

                if (shouldUpdateAnimation(previousWalkingFrame, currentFrame)) {
                    transmogObject.setAnimation(walkingAnimation);
                }

                previousWalkingFrame = currentFrame;
                previouslyMoved = true;
            }
        });
    }

    private boolean shouldUpdateAnimation(int previousFrame, int currentFrame) {
        return previousFrame == -1 || previousFrame > currentFrame;
    }

    private void handleStandingAnimation(Player player) {
        CompanionData selectedNpc = config.selectedNpc();
        if (selectedNpc == null) {
            return;
        }

        int standingAnimationId;

        if (selectedNpc == CompanionData.CUSTOM) {
            standingAnimationId = config.standingAnimationId();
        } else {
            standingAnimationId = selectedNpc.getStandingAnim();
        }

        Animation standingAnimation = client.loadAnimation(standingAnimationId);
        NPC followerLoop = client.getFollower();
        for (RuneLiteObject transmogObject : transmogObjects) {
            if (transmogObject != null && followerLoop != null) {
                currentFrame = transmogObject.getAnimationFrame();
                transmogObject.setActive(true);
                transmogObject.setShouldLoop(true);
                if(previousStandingFrame == -1) {
                    transmogObject.setAnimation(standingAnimation);
                }

                if (previousStandingFrame > currentFrame) {
                    transmogObject.setAnimation(standingAnimation);
                }
                previousStandingFrame = currentFrame;
                previouslyMoved = false;
            }
        }
    }


    private void updateTransmogObject(LocalPoint newLocation) {
        WorldView worldView = client.getTopLevelWorldView();

        if (transmogObjects == null) {
            System.out.println("null transmogObject");
        }

        if (transmogObjects != null) {
            for (RuneLiteObject transmogObject : transmogObjects) {
                if (transmogObject != null) {
                    transmogObject.setLocation(newLocation, worldView.getPlane());
                }
            }
        }
    }



    public Model createNpcModel() {
        CompanionData selectedNpc = config.selectedNpc();
        List<Integer> modelIds = new ArrayList<>();

        if (selectedNpc == CompanionData.CUSTOM) {
            // Add custom NPC model IDs if they are valid
            int[] npcModelIDs = {
                    config.npcModelID1(), config.npcModelID2(), config.npcModelID3(),
                    config.npcModelID4(), config.npcModelID5(), config.npcModelID6(),
                    config.npcModelID7(), config.npcModelID8(), config.npcModelID9(),
                    config.npcModelID10()
            };

            for (int modelId : npcModelIDs) {
                if (modelId > 0) {
                    modelIds.add(modelId);
                }
            }
        } else {
            // Add predefined NPC model IDs
            modelIds.addAll(selectedNpc.getModelIDs());
        }

        if (modelIds.isEmpty()) {
            System.out.println("modelId's empty");
            return null; // No NPC is selected or custom data is not set
        }

        ModelData[] modelDataArray = modelIds.stream()
                .map(client::loadModelData)
                .toArray(ModelData[]::new);

        if (Arrays.stream(modelDataArray).anyMatch(Objects::isNull)) {
            return null; // Return if any model data failed to load
        }

        // Merge the ModelData objects into a single ModelData
        ModelData mergedModelData = client.mergeModels(modelDataArray);
        if (mergedModelData == null) {
            System.out.println("Failed to merge model data.");
            return null;
        }

        // Light the merged ModelData to create the final Model
        Model finalModel = mergedModelData.light();
        if (finalModel == null) {
            System.out.println("Failed to light the merged model.");
            return null;
        }

        return finalModel;
    }

    boolean shouldDraw(Renderable renderable, boolean drawingUI)
    {
        if (renderable instanceof NPC) {
            NPC npc = (NPC) renderable;
            if (npc == client.getFollower()) {
                return !hidePets;
            }
        }
        return true;
    }
}