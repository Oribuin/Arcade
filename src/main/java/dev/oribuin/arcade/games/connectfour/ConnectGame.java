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
import dev.oribuin.arcade.util.ArcadeUtils;
import dev.oribuin.arcade.util.Placeholders;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static dev.oribuin.arcade.games.connectfour.token.ConnectToken.TOKEN_SIZE;

public class ConnectGame extends ArcadeGame<ConnectPlayer> {

    public static final NamespacedKey CONNECT_TOKEN = new NamespacedKey(ArcadePlugin.getInstance(), "connect_token");
    public static final NamespacedKey CONNECT_BOARD = new NamespacedKey(ArcadePlugin.getInstance(), "connect_board");
    public static final NamespacedKey CONNECT_ROW = new NamespacedKey(ArcadePlugin.getInstance(), "connect_row");

    private final int gridWidth;
    private final int gridHeight;
    private final float tokenGap;
    private final Table<Integer, Integer, ConnectToken> tokens; // Row, Column, TokenColour
    private final Map<UUID, Interaction> rows;
    private final Queue<UUID> turnQueue;
    private BlockDisplay displayBoard;
    private UUID infoBoard;
    private ScheduledTask task;
    private Integer glowRow;
    private List<TokenColour> playable;
    private int turns;

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
        this.glowRow = null;
        this.turnQueue = new ArrayDeque<>();
        this.playable = new ArrayList<>(TokenColour.COLORS.values());

        // Register playable events
        this.registerListener(PlayerInteractAtEntityEvent.class, this::handleInteraction);
    }

    /**
     * Functionality provided for when the game is placed down in the world.
     *
     * @param location  The location of the game
     * @param direction The direction the board is facing
     */
    public void place(@NotNull Location location, @NotNull BlockFace direction) {
        this.location = location;
        this.direction = direction;

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

                Location tokenLocation = center.clone().add(rotatedX, localY, rotatedZ);
                tokenLocation.setRotation((float) Math.toDegrees(rotation), 0);

                BlockDisplay display = tokenLocation.getWorld().spawn(
                        tokenLocation,
                        BlockDisplay.class,
                        CreatureSpawnEvent.SpawnReason.CUSTOM,
                        x -> {
                            ConnectToken.apply(x, TokenColour.EMPTY);
                            this.applyIdentifier(x);
                        }
                );

                ConnectToken token = new ConnectToken(TokenColour.EMPTY, display.getUniqueId());
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
            x.setTransformation(new Transformation(new Vector3f(rotatedX, 0F, rotatedZ), new AxisAngle4f(rotation, 0F, 1F, 0F), new Vector3f(widthScale, heightScale, 1f), new AxisAngle4f()));
            x.setBillboard(Display.Billboard.FIXED);

            PersistentDataContainer container = x.getPersistentDataContainer();
            container.set(CONNECT_BOARD, PersistentDataType.INTEGER, 0);
            container.set(GAME_ID, PersistentDataType.STRING, this.identifier.toString());
        });
        // endregion

        // region Spawn the interaction modifiers for the board 
        for (int row = 0; row < this.gridWidth; row++) {
            int currentRow = row;
            double rowLocalX = -(totalWidth / 2F - TOKEN_SIZE / 2F) + (row * distance);
            double rowRotatedX = rowLocalX * Math.cos(rotation);
            double rowRotatedZ = -rowLocalX * Math.sin(rotation);

            Location rowLocation = center.clone().add(rowRotatedX, this.tokenGap, rowRotatedZ);
            rowLocation.setRotation((float) Math.toDegrees(rotation), 0);
            Interaction interaction = center.getWorld().spawn(rowLocation, Interaction.class, CreatureSpawnEvent.SpawnReason.CUSTOM, x -> {
                x.setInteractionWidth(0.35f);
                x.setInteractionHeight(totalHeight);
                x.setResponsive(true);
                x.setGlowing(true);

                PersistentDataContainer container = x.getPersistentDataContainer();
                container.set(CONNECT_ROW, PersistentDataType.INTEGER, currentRow);
                container.set(GAME_ID, PersistentDataType.STRING, this.identifier.toString());
            });

            this.rows.put(interaction.getUniqueId(), interaction);
        }
        // endregion

        // region Add the info board for the game
        Location infoLoc = location.clone().add(0, totalHeight + 0.25, 0);
        TextDisplay textDisplay = center.getWorld().spawn(infoLoc, TextDisplay.class, CreatureSpawnEvent.SpawnReason.CUSTOM, x -> {
            x.setShadowed(true);
            x.setAlignment(TextDisplay.TextAlignment.CENTER);
            x.setBillboard(Display.Billboard.CENTER);
            x.setDisplayWidth(totalWidth);
            x.text(ArcadeUtils.kyorify(
                    Messages.get().getInactiveInfoBoard(),
                    Placeholders.of("remaining", this.playerCount - this.participants.size())
            ));
            this.applyIdentifier(x);
        });
        this.infoBoard = textDisplay.getUniqueId();
        // endregion
    }

    /**
     * A request from a player to join a game
     *
     * @param player The player who sent the game
     */
    @Override
    public boolean join(Player player) {
        boolean result = super.join(player);
        if (this.playerCount - this.participants.size() != 0) {
            this.updateText(ArcadeUtils.kyorify(
                    Messages.get().getInactiveInfoBoard(),
                    Placeholders.of("remaining", this.playerCount - this.participants.size())
            ));
        }
        return result;
    }

    /**
     * A request from the player to leave the game
     *
     * @param player     The player leaving the game
     * @param isRagequit Whether the player ragequit from the game
     * @return Whether the player successfully left
     */
    @Override
    public boolean leave(Player player, boolean isRagequit) {
        boolean result = super.leave(player, isRagequit);
        if (this.playerCount - this.participants.size() != 0) {
            this.updateText(ArcadeUtils.kyorify(
                    Messages.get().getInactiveInfoBoard(),
                    Placeholders.of("remaining", this.playerCount - this.participants.size())
            ));
        }
        return result;
    }

    /**
     * Start the game for the participating players
     */
    @Override
    public void start() {
        this.wipeBoard();
        this.playable = new ArrayList<>(TokenColour.COLORS.values());
        this.active = true;
        this.turns = 0;

        // Selects the last player
        List<UUID> shuffled = new ArrayList<>(this.participants.keySet());
        Collections.shuffle(shuffled);
        this.turnQueue.clear();
        this.turnQueue.addAll(shuffled);

        String joining = Messages.get().getActiveInfoStart() + this.participants.values()
                .stream()
                .map(x -> "<" + x.getTokenColour().asHex() + ">" + x.getName())
                .collect(Collectors.joining(
                        Messages.get().getActiveInfoJoiner()
                ));

        this.updateText(ArcadeUtils.kyorify(joining));

        this.task = PluginScheduler.get().runTaskTimerAtLocation(this.location, () -> {
            if (!this.active) return;

            UUID currentTurn = this.turnQueue.peek();
            if (currentTurn == null) return;

            ConnectPlayer active = this.participants.get(currentTurn);
            if (active == null) return;

            this.sendActionBar(Component.text("[" + active.getName() + "'s Turn]"));

            Player player = active.getPlayer();
            Entity entity = player.getTargetEntity(5);
            if (!(entity instanceof Interaction interaction) || !this.rows.containsKey(interaction.getUniqueId())) {
                this.unapplyGlow();
                return;
            }

            Integer row = interaction.getPersistentDataContainer().get(CONNECT_ROW, PersistentDataType.INTEGER);
            if (row == null || !this.rows.containsKey(entity.getUniqueId())) {
                this.unapplyGlow();
                return;
            }

            this.applyGlow(active.getTokenColour(), row);
        }, 100, 150, TimeUnit.MILLISECONDS);
    }

    /**
     * Stop the game from continuing
     *
     * @param cancelled Whether the game was cancelled
     */
    @Override
    public void stop(boolean cancelled) {

        if (this.task != null) this.task.cancel();
        this.task = null;

        this.active = false;
        this.turns = 0;
        this.participants.clear();
        this.turnQueue.clear();
        this.wipeBoard();

        this.updateText(ArcadeUtils.kyorify(
                Messages.get().getInactiveInfoBoard(),
                Placeholders.of("remaining", this.playerCount - this.participants.size())
        ));
    }

    /**
     * Unload the game from the world it's in
     */
    @Override
    public void unload() {
        this.stop(true);

        this.rows.keySet().forEach(uuid -> {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity != null) entity.remove();
        });
        this.rows.clear();

        this.tokens.values().forEach(token -> {
            BlockDisplay display = token.getDisplay();
            if (display != null) display.remove();
        });
        this.tokens.clear();

        Entity board = Bukkit.getEntity(this.displayBoard.getUniqueId());
        if (board != null) board.remove();

        Entity infoBoard = Bukkit.getEntity(this.infoBoard);
        if (infoBoard != null) infoBoard.remove();
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


    /**
     * Handle interacting with the player for the game
     *
     * @param event The event to start
     */
    public void handleInteraction(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        if (!(event.getRightClicked() instanceof Interaction interaction)) return;

        // Check whether the player is a participant
        ConnectPlayer participant = this.participants.get(player.getUniqueId());
        if (participant == null) return;
        if (!this.rows.containsKey(interaction.getUniqueId())) return;

        // Make sure the interaction is a row
        Integer row = interaction.getPersistentDataContainer().get(CONNECT_ROW, PersistentDataType.INTEGER);
        if (row == null) return;

        if (this.dropToken(player, participant.getTokenColour(), row)) {
            this.turns++;
            boolean winningToken = this.checkBoard(participant.getTokenColour());
            if (winningToken) {
                this.active = false;
                this.applyUniversalGlow(participant.getTokenColour()); // User won so the whole game should light up

                String losers = this.participants.values().stream()
                        .filter(x -> x.getUniqueId() != player.getUniqueId())
                        .map(x -> x.getPlayer().getName())
                        .collect(Collectors.joining(", "));

                if (losers.isEmpty()) losers = "N/A";

                Messages.get().getPlayerWon().send(this,
                        "game", ArcadeUtils.niceify(this.name),
                        "winner", player.getName(),
                        "losers", losers
                );
                PluginScheduler.get().runTaskAtLocationLater(this.location, () -> this.stop(false), 3 * 60);
                return;
            }

            if (this.turns >= (this.gridHeight * this.gridWidth)) this.stop(true);
        } else {
            Messages.get().getConnectFour().getNotUsersTurn().send(player);
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
     * Apply a universal glow to the colour, Typically used when someone has won
     *
     * @param colour The colour of the token
     */
    private void applyUniversalGlow(@NotNull TokenColour colour) {
        this.tokens.rowKeySet().forEach(row -> setGlowColour(row, colour));
    }

    public void wipeBoard() {
        if (this.task != null) this.task.cancel();

        for (Table.Cell<Integer, Integer, ConnectToken> cell : new ArrayList<>(this.tokens.cellSet())) {
            ConnectToken token = cell.getValue();
            BlockDisplay display = token.getDisplay();
            if (display == null || !display.isValid() || display.isDead()) continue;

            display.setGlowing(false);
            token.setColour(TokenColour.EMPTY);
            token.update(display);
            this.tokens.put(cell.getRowKey(), cell.getColumnKey(), token);
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
        if (this.participants.size() > 1) { // It's hard to test against myself okay
            if (!who.getUniqueId().equals(this.turnQueue.peek())) {
                Messages.get().getNotUsersTurn().send(who);
                return false;
            }
        }

        Map<Integer, ConnectToken> rowTokens = this.tokens.row(row);

        int minimum = Collections.min(rowTokens.keySet());
        int maximum = Collections.max(rowTokens.keySet()) + 1;

        int available = minimum;
        for (int i = minimum; i <= maximum; i++) {
            ConnectToken connectToken = rowTokens.get(i);
            available = i;

            if (connectToken != null && connectToken.getColour() == TokenColour.EMPTY) break;
        }

        if (available == maximum) return false; // row is full

        ConnectToken connectToken = rowTokens.get(available);
        BlockDisplay display = connectToken.getDisplay();
        if (display == null) return false;

        connectToken.setColour(colour);
        connectToken.update(display);

        this.tokens.put(row, available, connectToken);
        this.turnQueue.poll();
        this.turnQueue.add(who.getUniqueId()); // add them back
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
     * Apply a temporary glow of a specified colour
     *
     * @param colour The viewer to see the glow
     * @param row    The entity who's going to glow
     */
    public void applyGlow(@NotNull TokenColour colour, Integer row) {
        if (Objects.equals(row, this.glowRow)) return;
        if (this.glowRow != null) { // literally not always true intellij
            this.setGlowColour(glowRow, TokenColour.EMPTY);
        }

        this.setGlowColour(row, colour);
        this.glowRow = row;
    }

    public void setGlowColour(int row, TokenColour colour) {
        for (Map.Entry<Integer, ConnectToken> cell : new HashMap<>(this.tokens.row(row)).entrySet()) {
            ConnectToken token = cell.getValue();
            BlockDisplay display = token.getDisplay();
            if (display == null || !display.isValid() || display.isDead()) continue;

            display.setGlowing(colour != TokenColour.EMPTY);
            display.setGlowColorOverride(colour.color());
            token.update(display);
            this.tokens.put(row, cell.getKey(), token);
        }
    }

    public void updateText(Component component) {
        if (!(this.location.getWorld().getEntity(this.infoBoard) instanceof TextDisplay display)) return;

        display.text(component);
    }

    /**
     * Apply a temporary glow to an entity for a player
     */
    public void unapplyGlow() {
        if (this.glowRow == null) return;

        this.setGlowColour(this.glowRow, TokenColour.EMPTY);
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
 