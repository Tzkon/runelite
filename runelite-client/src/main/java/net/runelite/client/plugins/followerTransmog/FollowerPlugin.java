package net.runelite.client.plugins.followerTransmog;

import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.Hooks;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.callback.ClientThread;
import net.runelite.api.Renderable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;


@PluginDescriptor(
        name = "FollowerTransmog",
        description = "Alter the appearance of your pets/followers"
)
public class FollowerPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private FollowerConfig config;

    private boolean hasFollower = false;
    private boolean transmogInitialized = false;
    private LocalPoint lastFollowerLocation;
    private int tickCounter = 0;
    private final Hooks.RenderableDrawListener drawListener = this::shouldDraw;
    private boolean hidePets = true;
    private Model model;
    private List<RuneLiteObject> transmogObjects;
    private List<Integer> modelIds;
    //private int standingAnim;
    //private int walkAnim;
    //private int speed;
    //private int size;


    @Provides
    FollowerConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(FollowerConfig.class);
    }

    @Inject
    public ClientThread clientThread;

    @Inject
    private Hooks hooks;

    @Override
    protected void startUp() {
        // Initialize the list of transmog objects
        hooks.registerRenderableDrawListener(drawListener);
        System.out.println("followerTransmog plugin started!");
    }

    @Override
    protected void shutDown() {
        //  initializeTransmog();
        hooks.unregisterRenderableDrawListener(drawListener);
        System.out.println("Logged off Runelite ");
    }


    @Subscribe
    public void onGameTick(GameTick tick) {
        NPC follower = client.getFollower();

        if (follower == null || !transmogInitialized) {
            return;
        }
    }

    @Subscribe
    public void onClientTick(ClientTick event) {
        NPC follower = client.getFollower();

        if (follower != null) {
            if (transmogObjects == null) {
                transmogObjects = new ArrayList<>();
            }
            hasFollower = true;
            if (!transmogInitialized) {
                RuneLiteObject transmogObject = initializeTransmogObject(follower);
                if (transmogObject != null) {
                    transmogObjects.add(transmogObject);
                    transmogInitialized = true;
                } else {
                    System.out.println("Failed to initialize transmog object.");
                    return;
                }
            }
            // Update the position and state of the transmog object here if necessary
            updateTransmogObject(follower);
            updateFollowerMovement(follower);
        } else {
            // Handle the case where the follower is no longer present
            transmogInitialized = false;
            hasFollower = false;
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        TransmogData selectedNpc = config.selectedNpc();
        modelIds = selectedNpc.getModelIDs();
        if (modelIds.isEmpty()) {
            return; // Exit if there are no model IDs to process
        }

        if (!event.getGroup().equals(FollowerConfig.GROUP)) {
            return;
        }

        if (event.getKey().equals("selectedNpc")) {
            clientThread.invokeLater(() -> {

                if (selectedNpc == null) {
                    System.out.println("Null NPC");
                    return; // Exit if no NPC is selected or if it's the custom option
                }

                // Update class-level fields with the selected NPC's data
//                standingAnim = selectedNpc.getStandingAnim();
//                walkAnim = selectedNpc.getWalkAnim();
//                speed = selectedNpc.getSpeed();


                System.out.println("onConfig walkID" + selectedNpc.walkAnim);

                // Create and set the new model
                Model mergedModel = createNpcModel();
                if (mergedModel != null) {
                    // Assuming transmogObjects has been initialized and contains the object to update
                    RuneLiteObject transmogObject = transmogObjects.get(0);
                    transmogObject.setModel(mergedModel);
                    transmogObject.setActive(true);
                    System.out.println("Transmog object updated with new model.");
                }
            });
        }
    }

    private RuneLiteObject initializeTransmogObject(NPC follower) {
        RuneLiteObject transmogObject = client.createRuneLiteObject();
        if (transmogObject != null) {
            Model mergedModel = createNpcModel();
            if (mergedModel != null) {
                transmogObject.setModel(mergedModel);
                transmogObject.setActive(true);
                transmogObjects.add(transmogObject);
                System.out.println("Transmog object initialized with merged model.");
            }
        }
        return transmogObject;
    }


    private void updateFollowerMovement(NPC follower) {
        LocalPoint currentLocation = follower.getLocalLocation();
        boolean isFollowerMoving = lastFollowerLocation != null && !currentLocation.equals(lastFollowerLocation);
        lastFollowerLocation = currentLocation;
        if (isFollowerMoving) {
            handleWalkingAnimation(follower);
            //           System.out.println("Walking!");
        } else {
            tickCounter = 0;
            handleStandingAnimation(follower);
            //          System.out.println("Standing!");
        }
    }

    // The number of frames in the walking animation

    private void handleWalkingAnimation(NPC follower) {
        TransmogData selectedNpc = config.selectedNpc();
        if (selectedNpc == null) {
            // Handle the case where selectedNpc is null
            return;
        }

        int walkingAnimationId;
        int animationFrameCount;

        if (selectedNpc == TransmogData.CUSTOM) {
            walkingAnimationId = config.walkingAnimationId();
            animationFrameCount = config.walkingAnimationSpeed();
        } else {
            walkingAnimationId = selectedNpc.getWalkAnim();
            animationFrameCount = selectedNpc.getSpeed();
        }

        Animation walkingAnimation = client.loadAnimation(walkingAnimationId);

        if (tickCounter == 0) {
            for (RuneLiteObject transmogObject : transmogObjects) {
                if (transmogObject != null) {
                    transmogObject.setActive(true);
                    transmogObject.setAnimation(walkingAnimation);
                }
            }
        }
        // Increment the tick counter and reset it after the animation duration
        tickCounter = (tickCounter + 1) % animationFrameCount;
    }


    private void handleStandingAnimation(NPC follower) {
        TransmogData selectedNpc = config.selectedNpc();
        if (selectedNpc == null) {
            // Handle the case where selectedNpc is null
            return;
        }

        int standingAnimationId;

        if (selectedNpc == TransmogData.CUSTOM) {
            standingAnimationId = config.standingAnimationId();
        } else {
            standingAnimationId = selectedNpc.getWalkAnim();
        }

        Animation standingAnimation = client.loadAnimation(standingAnimationId);

        for (RuneLiteObject transmogObject : transmogObjects) {
            if (transmogObject != null) {
                    //    System.out.println("selected stand");
                    transmogObject.setAnimation(standingAnimation);
            }
        }
        // Do not reset tickCounter here if it's managed by the walking animation logic
    }

    private void updateTransmogObject(NPC follower) {
        WorldView worldView = client.getTopLevelWorldView();
        LocalPoint followerLocation = follower.getLocalLocation();
        if (transmogObjects != null) {
            for (RuneLiteObject transmogObject : transmogObjects) {
                if (transmogObject != null) {
                    transmogObject.setLocation(followerLocation, worldView.getPlane());
                    transmogObject.setOrientation(follower.getCurrentOrientation());// Set other properties as needed
                }
            }
        }
    }

    public Model createNpcModel() {
        TransmogData selectedNpc = config.selectedNpc();
        //System.out.println("Selected NPC: " + (selectedNpc != null ? selectedNpc.getName() : "null"));

        List<Integer> modelIds = new ArrayList<>();

        if (selectedNpc != TransmogData.CUSTOM) {
            // Predefined NPC is selected
            modelIds.addAll(selectedNpc.getModelIDs());
            System.out.println("drop down model ID's added");
        } else {
            modelIds.clear();
            //System.out.println("CUSTOM model ID's added: " + modelIds);
            // Custom NPC data is entered
            if (config.npcModelID1() != -1) modelIds.add(config.npcModelID1());
            if (config.npcModelID2() != -1) modelIds.add(config.npcModelID2());
            if (config.npcModelID3() != -1) modelIds.add(config.npcModelID3());
            if (config.npcModelID4() != -1) modelIds.add(config.npcModelID4());
            if (config.npcModelID5() != -1) modelIds.add(config.npcModelID5());
            if (config.npcModelID6() != -1) modelIds.add(config.npcModelID6());
            if (config.npcModelID7() != -1) modelIds.add(config.npcModelID7());
            if (config.npcModelID8() != -1) modelIds.add(config.npcModelID8());
            if (config.npcModelID9() != -1) modelIds.add(config.npcModelID9());
            if (config.npcModelID10() != -1) modelIds.add(config.npcModelID10());
        }

        if (modelIds.isEmpty()) {
            System.out.println("modelId's empty");
            return null; // Handle case where no NPC is selected or custom data is not set
        }

        ModelData[] modelDataArray = new ModelData[modelIds.size()];

        for (int i = 0; i < modelIds.size(); i++) {
            modelDataArray[i] = client.loadModelData(modelIds.get(i));
            if (modelDataArray[i] == null) {
             //   System.out.println("Failed to load model data for model ID: " + modelIds.get(i));
                return null;
            }
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

            //System.out.println("Checking shouldDraw for NPC: " + npc.getName());

            if (npc == client.getFollower()) {
                //  System.out.println("NPC is follower, hidePets is: " + hidePets);
                return !hidePets;
            }
        }
        return true;
    }
}