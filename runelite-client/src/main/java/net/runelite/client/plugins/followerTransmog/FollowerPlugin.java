package net.runelite.client.plugins.followerTransmog;

import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.coords.Angle;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.Hooks;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.callback.ClientThread;
import javax.inject.Inject;
import java.awt.*;
import java.util.*;
import java.util.List;

@PluginDescriptor(
        name = "FollowerTransmog",
        description = "Alter the appearance of your pets/followers"
)
public class FollowerPlugin extends Plugin {
    @Inject private Client client;
    @Inject private FollowerConfig config;
    @Inject private ClientThread clientThread;
    @Inject private Hooks hooks;

    private boolean hasFollower = false;
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
    private WorldArea[][] world; // The game world
    Map<WorldPoint, List<WorldPoint>> graph = new HashMap<>();

    @Provides
    FollowerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(FollowerConfig.class);
    }

    @Override
    protected void startUp() {
        hooks.registerRenderableDrawListener(drawListener);
        System.out.println("followerTransmog plugin started!");
    }

    @Override
    protected void shutDown() {
        hooks.unregisterRenderableDrawListener(drawListener);
        hasFollower = false;
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
            hasFollower = false;
            transmogInitialized = false;
            isMoving = false;
            previouslyMoved = false;
            transmogObjects = null;
            previousWalkingFrame = -1;
            previousStandingFrame = -1;
            System.out.println("game state reset");
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
                transmogObjects.add(transmogObject);
                transmogObject.setActive(true);

                if (config.selectedNpc() == TransmogData.CUSTOM) {
                    transmogObject.setDrawFrontTilesFirst(config.drawFrontTilesFirst());
                    transmogObject.setRadius(config.transmogRadius());
                }
            }
        }
        return transmogObject;
    }


    private void updateFollowerMovement(NPC follower) {
        LocalPoint currentLocation = follower.getLocalLocation();
        boolean isFollowerMoving = lastFollowerLocation != null && !currentLocation.equals(lastFollowerLocation);

        lastFollowerLocation = currentLocation;
        if (isFollowerMoving) {

            isMoving = true;
            handleWalkingAnimation(follower);
            //           System.out.println("Walking!");
        } else {
            isMoving = false;
            handleStandingAnimation(follower);
        }
    }

    // The number of frames in the walking animation

    private void handleWalkingAnimation(NPC follower) {
        TransmogData selectedNpc = config.selectedNpc();
        if (selectedNpc == null) {
            System.out.println("No NPC selected for walking animation.");
            return;
        }

        int walkingAnimationId = (selectedNpc == TransmogData.CUSTOM)
                ? config.walkingAnimationId()
                : selectedNpc.getWalkAnim();

        Animation walkingAnimation = client.loadAnimation(walkingAnimationId);
        if (walkingAnimation == null) {
            System.out.println("Failed to load walking animation for ID: " + walkingAnimationId);
            return;
        }

        NPC currentFollower = client.getFollower();
        if (currentFollower == null) {
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

    private void handleStandingAnimation(NPC follower) {
        TransmogData selectedNpc = config.selectedNpc();
        if (selectedNpc == null) {
            return;
        }

        int standingAnimationId;

        if (selectedNpc == TransmogData.CUSTOM) {
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

    private void updateTransmogObject(NPC follower) {
        WorldView worldView = client.getTopLevelWorldView();
        LocalPoint followerLocation = follower.getLocalLocation();

        // Get the configured offsets
        int offsetX = config.offsetX() * 128; // Convert tiles to local units
        int offsetY = config.offsetY() * 128; // Convert tiles to local units

        // Calculate the new position with the offsets
        int newX = followerLocation.getX() + offsetX;
        int newY = followerLocation.getY() + offsetY;
        LocalPoint newLocation = new LocalPoint(newX, newY);

        // Calculate the angle between the follower and the player
        Player player = client.getLocalPlayer();
        int dx = player.getLocalLocation().getX() - newX;
        int dy = player.getLocalLocation().getY() - newY;
        int angle = (int) ((Math.atan2(-dy, dx) * config.angleMultiplier()) / (2 * Math.PI) + config.angleShift()) % 2048;

        // Create a new Angle object with the calculated angle
        Angle followerOrientation = new Angle(angle);

        if (transmogObjects == null) {
            System.out.println("null transmogObject");
        }


        if (transmogObjects != null) {
            for (RuneLiteObject transmogObject : transmogObjects) {
                if (transmogObject != null) {
//                    transmogObject.setActive(true);
                    transmogObject.setLocation(newLocation, worldView.getPlane());
                    // Set the follower's orientation to face the player
                    transmogObject.setOrientation(followerOrientation.getAngle());
                }
            }
        }
    }


    public Model createNpcModel() {
        TransmogData selectedNpc = config.selectedNpc();
        List<Integer> modelIds = new ArrayList<>();

        if (selectedNpc == TransmogData.CUSTOM) {
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


    private void renderTileIfValidForMovement(Graphics2D graphics, Actor actor, int dx, int dy)
    {
        WorldArea area = actor.getWorldArea();
        if (area == null)
        {
            return;
        }

        if (area.canTravelInDirection(client.getTopLevelWorldView(), dx, dy))
        {
            LocalPoint lp = actor.getLocalLocation();
            if (lp == null)
            {
                return;
            }

            lp = new LocalPoint(
                    lp.getX() + dx * Perspective.LOCAL_TILE_SIZE + dx * Perspective.LOCAL_TILE_SIZE * (area.getWidth() - 1) / 2,
                    lp.getY() + dy * Perspective.LOCAL_TILE_SIZE + dy * Perspective.LOCAL_TILE_SIZE * (area.getHeight() - 1) / 2);

            // Convert the local point to a world point
            WorldPoint wp = WorldPoint.fromLocal(client, lp);

            // Get the actor's current location
            WorldPoint actorLocation = actor.getWorldLocation();

            // Add the neighboring tile to the graph
            graph.putIfAbsent(actorLocation, new ArrayList<>());
            graph.get(actorLocation).add(wp);
        }
    }

    public class AStar {
        private Map<WorldPoint, WorldPoint> cameFrom = new HashMap<>();
        private Map<WorldPoint, Double> gScore = new HashMap<>();
        private Map<WorldPoint, Double> fScore = new HashMap<>();
        private PriorityQueue<WorldPoint> openSet = new PriorityQueue<>(Comparator.comparingDouble(fScore::get));

        private List<WorldPoint> reconstructPath(WorldPoint current) {
            List<WorldPoint> path = new ArrayList<>();
            WorldPoint node = current;
            while (node != null) {
                path.add(node);
                node = cameFrom.get(node);
            }
            Collections.reverse(path); // Reverse the path to get it from start to goal
            return path;
        }

        public List<WorldPoint> findPath(WorldPoint start, WorldPoint goal) {
            openSet.add(start);
            gScore.put(start, 0.0);
            fScore.put(start, heuristicCostEstimate(start, goal));

            while (!openSet.isEmpty()) {
                WorldPoint current = openSet.poll();

                if (current.equals(goal)) {
                    return reconstructPath(current);
                }

                openSet.remove(current);

                for (WorldPoint neighbor : getNeighbors(current)) {
                    double tentativeGScore = gScore.get(current) + distBetween(current, neighbor);

                    if (!gScore.containsKey(neighbor) || tentativeGScore < gScore.get(neighbor)) {
                        cameFrom.put(neighbor, current);
                        gScore.put(neighbor, tentativeGScore);
                        fScore.put(neighbor, gScore.get(neighbor) + heuristicCostEstimate(neighbor, goal));

                        if (!openSet.contains(neighbor)) {
                            openSet.add(neighbor);
                        }
                    }
                }
            }

            return null; // Failure
        }

        // Other methods (heuristicCostEstimate, distBetween, getNeighbors, reconstructPath, etc.) go here
        private double heuristicCostEstimate(WorldPoint start, WorldPoint goal) {
            return Math.abs(start.getX() - goal.getX()) + Math.abs(start.getY() - goal.getY());
        }

    }

    private double distBetween(WorldPoint current, WorldPoint neighbor) {
        if (current.getX() != neighbor.getX() && current.getY() != neighbor.getY()) {
            return Math.sqrt(2); // Diagonal move
        } else {
            return 1; // Horizontal or vertical move
        }
    }

    private List<WorldPoint> getNeighbors(WorldPoint current) {
        return graph.getOrDefault(current, new ArrayList<>());
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