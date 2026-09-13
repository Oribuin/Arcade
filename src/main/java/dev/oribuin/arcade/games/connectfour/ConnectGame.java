package dev.oribuin.arcade.games.connectfour;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import dev.oribuin.arcade.ArcadePlugin;
import dev.oribuin.arcade.api.game.ArcadeGame;
import dev.oribuin.arcade.config.Messages;
import dev.oribuin.arcade.games.connectfour.participant.ConnectPlayer;
import dev.oribuin.arcade.games.connectfour.token.ConnectToken;
import dev.oribuin.arcade.games.connectfour.token.TokenColour;
import dev.oribuin.arcade.scheduler.PluginScheduler;
import dev.oribuin.arcade.scheduler.task.ScheduledTask;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static dev.oribuin.arcade.games.connectfour.token.ConnectToken.TOKEN_SIZE;

public class ConnectGame extends ArcadeGame<ConnectPlayer> implements Listener {

    public static final NamespacedKey CONNECT_TOKEN = NamespacedKey.fromString("connect_token", ArcadePlugin.getInstance());
    public static final NamespacedKey CONNECT_BOARD = NamespacedKey.fromString("connect_board", ArcadePlugin.getInstance());
    public static final NamespacedKey CONNECT_ROW = NamespacedKey.fromString("connect_row", ArcadePlugin.getInstance());

    private final int gridWidth;
    private final int gridHeight;
    private final float tokenGap;
    private final Table<Integer, Integer, ConnectToken> tokens; // Row, Column, TokenColour
    private final Map<UUID, Interaction> rows;
    private BlockDisplay displayBoard;
    private ScheduledTask task;
    private Integer glowRow;
    private UUID currentTurn;
    private UUID lastTurn;
    private boolean alreadyWon; // todo temp
    private List<TokenColour> playable;

    /**
     * Creates a new arcade game for the plugin
     */
    public ConnectGame() {
        super("connect_four");
        this.gridWidth = 7;
        this.gridHeight = 6;
        this.tokenGap = 0.125F;
        this.tokens = HashBasedTable.create(this.gridHeight, this.gridWidth);
        this.rows = new HashMap<>();
        this.alreadyWon = false;
        this.glowRow = null;
        this.playable = new ArrayList<>(TokenColour.COLORS.values());
    }

    public boolean isRow(Entity entity) {
        assert CONNECT_ROW != null;
        return entity.getPersistentDataContainer().has(CONNECT_ROW);
    }

    /**
     * Functionality provided for when the game is placed down in the world.
     *
     * @param location  The location of the game
     * @param direction The direction the board is facing
     */
    public void place(@NotNull Location location, @NotNull BlockFace direction) {
        this.unload();
        this.location = location;

        float rotation = this.getRotation(direction);

        // region Spawn the tokens into the world

        double distance = TOKEN_SIZE + this.tokenGap;
        float totalWidth = (float) (distance * this.gridWidth) - this.tokenGap;
        float totalHeight = (float) (distance * this.gridHeight) - this.tokenGap;

        Location center = this.location.toCenterLocation().clone();
        center.setY(center.getBlockY());

        for (int row = 0; row < this.gridWidth; row++) {
            for (int col = 0; col < this.gridHeight; col++) {

                double localX = -(totalWidth / 2F - TOKEN_SIZE / 2F) + (row * distance);
                double localY = this.tokenGap + (col * distance);

                double rotatedX = localX * Math.cos(rotation);
                double rotatedZ = -localX * Math.sin(rotation);

                Location tokenLocation = center.clone().add(
                        rotatedX,
                        localY,
                        rotatedZ
                );

                ConnectToken token = this.createEmptyToken(tokenLocation);
                token.getDisplay().setRotation((float) Math.toDegrees(rotation), 0);
                this.tokens.put(row, col, token);
            }
        }

        // endregion

        // region Blue board that envelops the game
        double cos = Math.cos(rotation);
        double sin = Math.sin(rotation);

        float widthScale = totalWidth + this.tokenGap * 2F;
        float heightScale = totalHeight + this.tokenGap * 2F;
        float localX = -(widthScale / 2F);
        float localZ = -0.5F;

        float rotatedX = (float) (localX * cos + localZ * sin);
        float rotatedZ = (float) (-localX * sin + localZ * cos);

        this.displayBoard = center.getWorld().spawn(center, BlockDisplay.class, CreatureSpawnEvent.SpawnReason.CUSTOM, x -> {
            x.setBlock(Material.BLUE_STAINED_GLASS.createBlockData());
            x.setBrightness(new Display.Brightness(15, 15));
            x.setTransformation(new Transformation(
                    new Vector3f(rotatedX, 0F, rotatedZ),
                    new AxisAngle4f(rotation, 0F, 1F, 0F),
                    new Vector3f(widthScale, heightScale, 1f),
                    new AxisAngle4f()
            ));
            x.setBillboard(Display.Billboard.FIXED);

            PersistentDataContainer container = x.getPersistentDataContainer();
            container.set(ConnectGame.CONNECT_BOARD, PersistentDataType.INTEGER, 0);
        });
        // endregion

        // region Spawn the interaction modifiers for the board 
        for (int row = 0; row < this.gridWidth; row++) {
            int currentRow = row;
            double rowLocalX = -(totalWidth / 2F - TOKEN_SIZE / 2F) + (row * distance);
            double rowRotatedX = rowLocalX * Math.cos(rotation);
            double rowRotatedZ = -rowLocalX * Math.sin(rotation);

            Location rowLocation = center.clone().add(
                    rowRotatedX,
                    TOKEN_SIZE,
                    rowRotatedZ
            );
            rowLocation.setRotation((float) Math.toDegrees(rotation), 0);
            Interaction interaction = center.getWorld().spawn(
                    rowLocation,
                    Interaction.class,
                    CreatureSpawnEvent.SpawnReason.CUSTOM, x -> {
                        x.setInteractionWidth(0.35f);
                        x.setInteractionHeight(totalHeight);
                        x.setResponsive(true);
                        x.setGlowing(true);

                        PersistentDataContainer container = x.getPersistentDataContainer();
                        container.set(ConnectGame.CONNECT_ROW, PersistentDataType.INTEGER, currentRow);
                    });

            this.rows.put(interaction.getUniqueId(), interaction);
        }
        // endregion
    }

    /**
     * Start the game for the participating players
     */
    @Override
    public void start() {
        this.wipeBoard();
        this.playable = new ArrayList<>(TokenColour.COLORS.values());
        this.active = true;

        // Selects the last player
        UUID[] uuids = this.participants.keySet().toArray(new UUID[0]);
        this.currentTurn = uuids[0];
        this.lastTurn = uuids[uuids.length - 1];

        Bukkit.getPluginManager().registerEvents(this, ArcadePlugin.getInstance());
        this.task = PluginScheduler.get().runTaskTimerAtLocation(this.location, () -> {
            ConnectPlayer participant = this.participants.get(this.currentTurn);
            if (participant == null) return;

            Player player = participant.getPlayer();
            RayTraceResult entity = player.rayTraceEntities(5);
            if (entity == null || !(entity.getHitEntity() instanceof Interaction interaction)) {
                this.unapplyGlow();
                return;
            }

            Integer row = interaction.getPersistentDataContainer().get(CONNECT_ROW, PersistentDataType.INTEGER);
            if (row == null || !this.rows.containsKey(interaction.getUniqueId())) {
                this.unapplyGlow();
                return;
            }

            this.applyGlow(participant.getTokenColour(), row);
        }, 100, 150, TimeUnit.MILLISECONDS);
    }

    /**
     * Stop the game from continuing
     *
     * @param cancelled Whether the game was cancelled
     */
    @Override
    public void stop(boolean cancelled) {
        this.active = false;
        this.participants.clear();
        this.wipeBoard();
    }

    /**
     * Unload the game from the world it's in
     */
    @Override
    public void unload() {
        this.stop(true);

        HandlerList.unregisterAll(this);
        if (this.task != null) this.task.cancel();

        this.rows.entrySet().removeIf(entry -> {
            entry.getValue().remove();
            return true;
        });

        for (ConnectToken token : this.tokens.values()) {
            BlockDisplay display = token.getDisplay();
            if (display == null || !display.isValid() || display.isDead()) continue;

            display.remove();
        }

        if (this.displayBoard != null) this.displayBoard.remove();
        this.tokens.clear();
    }

    /**
     * Creates a new participant instance for the game
     *
     * @param target The player participant
     * @return The resulting participant
     */
    @Override
    public Supplier<ConnectPlayer> createParticipant(Player target) {
        return () -> new ConnectPlayer(target, this.getRandom());
    }

    public TokenColour getRandom() {
        TokenColour colour = this.playable.get((int) (Math.random() * this.playable.size()));
        this.playable.remove(colour);
        return colour;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInteract(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        if (!(event.getRightClicked() instanceof Interaction interaction)) return;

        // Check whether the player is a participant
        ConnectPlayer participant = this.participants.get(player.getUniqueId());
        if (participant == null || CONNECT_ROW == null) return;
        if (!this.rows.containsKey(interaction.getUniqueId())) return;
        if (!this.active) return;

        // Make sure the interaction is a row
        Integer row = interaction.getPersistentDataContainer().get(CONNECT_ROW, PersistentDataType.INTEGER);
        if (row == null) return;

        if (this.dropToken(player, participant.getTokenColour(), row)) {
            boolean winningToken = this.checkBoard(participant.getTokenColour());
            if (!this.alreadyWon && winningToken) {
                this.alreadyWon = true;
                this.active = false;

                this.sendMessage(Component.text(player.getName() + " has won the game ! GOODBYE!"));
                PluginScheduler.get().runTaskAtLocationLater(this.location, this::unload, 3 * 60);
            }
        } else {
            player.sendMessage("You cannot place a token in this row");
        }
    }

    /**
     * Get the rotation of the game based on the blockface
     *
     * @param face The block face
     * @return The game rotation
     */
    private float getRotation(@NotNull BlockFace face) {
        return switch (face) {
            case EAST -> (float) (Math.PI / 2F);
            case NORTH -> (float) Math.PI;
            case WEST -> (float) (-Math.PI / 2F);
            default -> 0F;
        };
    }

    /**
     * Create an empty token within the plugin
     *
     * @param position The position
     * @return The token of the player
     */
    @NotNull
    private ConnectToken createEmptyToken(@NotNull Location position) {
        BlockDisplay display = position.getWorld().spawn(position, BlockDisplay.class, CreatureSpawnEvent.SpawnReason.CUSTOM);
        ConnectToken token = new ConnectToken(TokenColour.EMPTY, display);
        token.update();
        return token;
    }

    public void wipeBoard() {
        if (this.task != null) this.task.cancel();

        for (ConnectToken token : this.tokens.values()) {
            BlockDisplay display = token.getDisplay();
            if (display == null || !display.isValid() || display.isDead()) continue;

            token.setColour(TokenColour.EMPTY);
            token.update();
        }
    }

    /**
     * Drops a token into the game allowing the user to
     *
     * @param colour The colour token to drop
     * @param row    The row to drop the token into
     */
    public boolean dropToken(Player who, TokenColour colour, int row) {

        // Check if the user has already placed a token previously
        if (!who.getUniqueId().equals(this.currentTurn)) {
            Messages.get().getNotUsersTurn().send(who);
            return false;
        }

        Map<Integer, ConnectToken> rowTokens = this.tokens.row(row);

        int minimum = Collections.min(rowTokens.keySet());
        int maximum = Collections.max(rowTokens.keySet()) + 1;

        int available = minimum;
        for (int i = minimum; i < maximum; i++) {
            ConnectToken connectToken = rowTokens.get(i);
            available = i;

            if (connectToken != null && connectToken.getColour() == TokenColour.EMPTY) break;
        }

        if (available == maximum) return false;

        ConnectToken connectToken = rowTokens.get(available);
        connectToken.setColour(colour);
        connectToken.update();

        this.tokens.put(row, available, connectToken);
        this.currentTurn = this.lastTurn; // swap turns
        this.lastTurn = who.getUniqueId();
        this.unapplyGlow();
        return true;
    }

    /**
     * Check whether a line is available at a specified token
     *
     * @param colour The colour of the token
     * @return Whether the player has connected
     */
    public boolean checkBoard(TokenColour colour) {

        // region Check for horizontal matches on the board
        for (int x = 0; x < this.gridWidth; x++) {
            for (int y = 0; y < this.gridHeight; y++) {
                ConnectToken centre = this.tokens.get(x, y);
                ConnectToken second = this.getToken(x, y + 1);
                ConnectToken third = this.getToken(x, y + 2);
                ConnectToken fourth = this.getToken(x, y + 3);
                if (this.isTypeOf(colour, centre, second, third, fourth)) return true;
            }
        }
        // endregion

        // region Check for vertical matches on the board
        for (int x = 0; x < this.gridWidth; x++) {
            for (int y = 0; y < this.gridHeight; y++) {
                ConnectToken centre = this.tokens.get(x, y);
                ConnectToken second = this.getToken(x + 1, y);
                ConnectToken third = this.getToken(x + 2, y);
                ConnectToken fourth = this.getToken(x + 3, y);
                if (this.isTypeOf(colour, centre, second, third, fourth)) return true;
            }
        }
        // endregion

        // region Check for upward diagonal matches on the board
        for (int x = 0; x < this.gridWidth; x++) {
            for (int y = 0; y < this.gridHeight; y++) {
                ConnectToken centre = this.tokens.get(x, y);
                ConnectToken second = this.getToken(x - 1, y + 1);
                ConnectToken third = this.getToken(x - 2, y + 2);
                ConnectToken fourth = this.getToken(x - 3, y + 3);
                if (this.isTypeOf(colour, centre, second, third, fourth)) return true;
            }
        }
        // endregion

        // region Check for downward diagonal matches on the board
        for (int x = 0; x < this.gridWidth; x++) {
            for (int y = 0; y < this.gridHeight; y++) {
                ConnectToken centre = this.tokens.get(x, y);
                ConnectToken second = this.getToken(x + 1, y + 1);
                ConnectToken third = this.getToken(x + 2, y + 2);
                ConnectToken fourth = this.getToken(x + 3, y + 3);
                if (this.isTypeOf(colour, centre, second, third, fourth)) return true;
            }
        }
        // endregion

        return false;
    }

    /**
     * Apply a temporary glow to an entity for a player
     *
     * @param viewer The viewer to see the glow
     * @param entity The entity who's going to glow
     */
    public void applyGlow(@NotNull TokenColour colour, Integer row) {
        if (Objects.equals(row, this.glowRow)) return;
        if (this.glowRow != null) { // literally not always true intellij
            this.tokens.row(glowRow).values().forEach(x -> {
                x.getDisplay().setGlowColorOverride(Color.WHITE);
                x.getDisplay().setGlowing(false);
            });
        }

        this.tokens.row(row).values().forEach(x -> {
            x.getDisplay().setGlowColorOverride(colour.color());
            x.getDisplay().setGlowing(true);
        });

        this.glowRow = row;
    }

    /**
     * Apply a temporary glow to an entity for a player
     */
    public void unapplyGlow() {
        if (this.glowRow == null) return;

        this.tokens.row(this.glowRow).values().forEach(x -> {
            x.getDisplay().setGlowColorOverride(Color.WHITE);
            x.getDisplay().setGlowing(false);
        });
        this.glowRow = null;
    }

    /**
     * Get a token from a specified column
     *
     * @param row    The row the token is on
     * @param column The column the token is on
     * @return The resulting token
     */
    public ConnectToken getToken(int row, int column) {
        return this.tokens.get(row, column);
    }

    /**
     * Check whether a token is a specified colour
     *
     * @param colour The colour to check
     * @param tokens The tokens to check
     * @return Whether the token is the same colour
     */
    public boolean isTypeOf(TokenColour colour, ConnectToken... tokens) {
        return Arrays.stream(tokens).allMatch(token -> token != null && token.getColour() == colour);
    }

}
 