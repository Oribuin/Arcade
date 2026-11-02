package dev.oribuin.arcade.games.chess;

import dev.oribuin.arcade.ArcadePlugin;
import dev.oribuin.arcade.api.game.ArcadeGame;
import dev.oribuin.arcade.config.Messages;
import dev.oribuin.arcade.games.chess.board.BoardPosition;
import dev.oribuin.arcade.games.chess.board.ChessBoard;
import dev.oribuin.arcade.games.chess.participant.ChessPlayer;
import dev.oribuin.arcade.games.chess.piece.ChessPiece;
import dev.oribuin.arcade.games.chess.piece.PieceTeam;
import dev.oribuin.arcade.scheduler.PluginScheduler;
import dev.oribuin.arcade.scheduler.task.ScheduledTask;
import dev.oribuin.arcade.util.ArcadeUtils;
import dev.oribuin.arcade.util.EntityUtils;
import dev.oribuin.arcade.util.Placeholders;
import io.papermc.paper.persistence.PersistentDataContainerView;
import io.papermc.paper.persistence.PersistentDataKey;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.bukkit.persistence.PersistentDataType.INTEGER;

/**
 * chess... in minecraft :)
 */
public class ChessGame extends ArcadeGame<ChessPlayer> {


    public static PersistentDataKey<Integer> POSITION_ROW = ArcadePlugin.namespace("board_position_row", INTEGER);
    public static PersistentDataKey<Integer> POSITION_COL = ArcadePlugin.namespace("board_position_col", INTEGER);

    public static final float SQUARE_SIZE = 0.65f;
    public static final float PIECE_SIZE = 0.55f;

    private final Queue<PieceTeam> teams;
    private final Map<UUID, UUID> boardDisplays;
    private ChessBoard board;
    private UUID infoBoard;
    private BoardPosition currentGlow;
    private ScheduledTask task;

    /**
     * Creates a new arcade game for the plugin
     */
    public ChessGame() {
        super("chess");

        this.teams = new ArrayDeque<>();
        this.boardDisplays = new HashMap<>();
        this.registerListener(PlayerInteractAtEntityEvent.class, this::handleInteraction);
    }

    /**
     * Functionality provided for when the game is placed down in the world
     *
     * @param location  The location of the game
     * @param direction The direction the player was facing
     */
    @Override
    public void place(@NotNull Location location, @NotNull BlockFace direction) {
        this.location = location;
        this.direction = direction;
        this.board = new ChessBoard();
        this.teams.clear();
        this.teams.addAll(List.of(PieceTeam.WHITE, PieceTeam.BLACK));

        int size = 8;
        float totalWidth = SQUARE_SIZE * size;
        Location center = this.location.toCenterLocation();
        center.setY(center.getBlockY());

        // region I'm stressed out so lets start simple. a checker pattern of blocks
        boolean white = true;
        BlockData whiteConcrete = Material.WHITE_CONCRETE.createBlockData();
        BlockData blackConcrete = Material.BLACK_CONCRETE.createBlockData();
        for (int x = 0; x < size; x++) {
            white = !white; // stops it from being a barcode
            for (int z = 0; z < size; z++) {
                double localX = -(totalWidth / 2F - SQUARE_SIZE / 2F) + (x * SQUARE_SIZE);
                double localZ = -(totalWidth / 2F - SQUARE_SIZE) + (z * SQUARE_SIZE);
                BoardPosition position = new BoardPosition(x + 1, z + 1);

                Location squareLoc = center.clone().add(localX, 0, localZ);
                Transformation transformation = new Transformation(
                        new Vector3f(-(SQUARE_SIZE / 2), 0, -(SQUARE_SIZE / 2)),
                        new AxisAngle4f(),
                        new Vector3f(SQUARE_SIZE, 0.1f, SQUARE_SIZE),
                        new AxisAngle4f()
                );
                white = !white;
                BlockData type = white ? whiteConcrete : blackConcrete;
                BlockDisplay square = squareLoc.getWorld().spawn(
                        squareLoc,
                        BlockDisplay.class,
                        CreatureSpawnEvent.SpawnReason.CUSTOM, block -> {
                            this.applyIdentifier(block);
                            block.setBlock(type);
                            block.setBrightness(new Display.Brightness(15, 15));
                            block.setDisplayWidth(SQUARE_SIZE);
                            block.setDisplayHeight(0.1f);
                            block.setTransformation(transformation);
                            block.setBillboard(Display.Billboard.FIXED);

                            this.setContainerPos(block.getPersistentDataContainer(), position);
                        });


                Interaction squareInteraction = squareLoc.getWorld().spawn(
                        squareLoc,
                        Interaction.class,
                        CreatureSpawnEvent.SpawnReason.CUSTOM, interaction -> {
                            this.applyIdentifier(interaction);
                            interaction.setResponsive(true);
                            interaction.setInteractionWidth(SQUARE_SIZE * 0.75f);
                            interaction.setInteractionHeight(SQUARE_SIZE * 0.75f);

                            this.setContainerPos(interaction.getPersistentDataContainer(), position);
                        });

                this.boardDisplays.put(squareInteraction.getUniqueId(), square.getUniqueId());
                this.board.setSquare(position, square.getUniqueId());
                this.board.getBoardPositions().put(position, squareLoc.clone().add(0, 0.45f, 0));

                // Place the piece associated with the board
                ChessPiece piece = this.board.getPiece(position);
                if (piece != null) {
                    piece.place(this, squareLoc.clone().add(0, 0.45f, 0));
                    this.board.set(piece);
                }
            }
        }
        // endregion
        // region Add the info board for the game
        Location infoLoc = location.clone().add(0, 2.25, 0);
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
            this.updateTextInfo(ArcadeUtils.kyorify(
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
            this.updateTextInfo(ArcadeUtils.kyorify(
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
        this.active = true;
        this.teams.clear();
        this.teams.addAll(List.of(PieceTeam.WHITE, PieceTeam.BLACK));

        this.task = PluginScheduler.get().runTaskTimerAtLocation(this.location, () -> {
            if (!this.active) return;

            PieceTeam currentTurn = this.teams.peek();
            if (currentTurn == null) return;

            ChessPlayer active = this.getTeamPlayer(currentTurn);
            if (active == null) return;

            // Update text info for the game
            this.sendActionBar(Component.text("[" + active.getName() + "'s Turn]"));
            this.updateTextInfo(ArcadeUtils.kyorify(
                    Messages.get().getChess().getCurrentTeamInfo(), Placeholders.of(
                            "team", StringUtils.capitalize(currentTurn.name().toLowerCase()),
                            "time", "67h"
                    )));
        }, 100, 150, TimeUnit.MILLISECONDS);
    }

    /**
     * Stop the game from continuing
     *
     * @param cancelled Whether the game was cancelled
     */
    @Override
    public void stop(boolean cancelled) {
        // Remove all glow effects from the board
        this.boardDisplays.values().forEach(uuid -> EntityUtils.safelyModify(BlockDisplay.class,
                uuid,
                entity -> {
                    entity.setGlowing(false);
                    entity.setGlowColorOverride(Color.WHITE);
                }));
    }

    /**
     * Unload the game from the world it's in
     */
    @Override
    public void unload() {
        this.teams.clear();
        this.teams.addAll(List.of(PieceTeam.WHITE, PieceTeam.BLACK));

        this.boardDisplays.forEach((interact, display) -> {
            Entity interactEntity = this.location.getWorld().getEntity(interact);
            Entity displayEntity = this.location.getWorld().getEntity(display);

            if (interactEntity != null) interactEntity.remove();
            if (displayEntity != null) displayEntity.remove();
        });

        this.boardDisplays.clear();

        this.board.getPieces().values().forEach(piece -> {
            Entity boardPiece = this.location.getWorld().getEntity(piece.getDisplay());
            if (boardPiece != null) boardPiece.remove();
        });

        this.board.getPieces().clear();

        EntityUtils.safelyModify(this.infoBoard, Entity::remove);
    }

    /**
     * Creates a new participant instance for the game
     *
     * @param target The player participant
     * @return The resulting participant
     */
    @Override
    public Supplier<ChessPlayer> createParticipant(Player target) {
        return () -> new ChessPlayer(target, this.teams.poll());
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
        ChessPlayer participant = this.participants.get(player.getUniqueId());
        if (participant == null) return;

        BoardPosition position = this.getContainerPos(interaction.getPersistentDataContainer());
        if (position == null) return;

        // Check if the user has already placed a token previously
        if (this.participants.size() > 1) { // It's hard to test against myself okay
            if (!participant.getTeam().equals(this.teams.peek())) {
                Messages.get().getNotUsersTurn().send(player);
                return;
            }
        }

        // User doesn't have a piece selected, select one
        BoardPosition selected = participant.getSelected();
        if (selected == null) {
            ChessPiece piece = this.board.getPiece(position);
            if (piece == null || !participant.canInteract(piece)) {
                Messages.get().getChess().getCannotUse().send(player);
                return; // User cannot interact with that piece
            }

            if (piece.getDisplay() == null) return; // Piece does not exist? how did you click on it....
            if (piece.getAvailable(this.board).isEmpty()) {
                Messages.get().getChess().getNoAvailableMoves().send(player);
                return; // Cannot select a piece that doesn't have any moves
            }

            participant.setSelected(position);
            this.participants.put(player.getUniqueId(), participant);

            Messages.get().getChess().getSelectedPiece().send(player, "piece", piece.getType(), "position", piece.getPosition());

            // Mark the entity as glowing
            EntityUtils.safelyModify(
                    ItemDisplay.class,
                    piece.getDisplay(),
                    entity -> {
                        entity.setGlowing(true);
                        entity.setGlowColorOverride(Color.LIME);
                    });

            // make the available pieces glow green
            for (BoardPosition available : piece.getAvailable(this.board)) {
                UUID square = this.board.getSquare(available);
                if (square == null) continue;

                // Make all available squares glow yellow
                EntityUtils.safelyModify(BlockDisplay.class, square, entity -> {
                    entity.setGlowing(true);
                    entity.setGlowColorOverride(Color.YELLOW);
                });
            }

            return;
        }

        // Piece is selected, deselect it
        ChessPiece selectedPiece = this.board.getPiece(participant.getSelected());

        if (selectedPiece != null && selected.equals(position)) {
            // Remove all glow effects from the board
            this.resetSelectionGlow(participant);

            participant.setSelected(null);
            this.participants.put(player.getUniqueId(), participant);

            Messages.get().getChess().getDeselectedPiece().send(player);
            return;
        }

        if (selectedPiece == null) return; // Selected piece does not exist

        // If the move is available, select the piece

        PieceTeam currentTeam = this.teams.peek();
        if (this.board.isAvailable(selectedPiece, position) ) {
            Messages.get().getChess().getMovedPiece().send(this,
                    "player", player.getName(),
                    "piece", selectedPiece.getType(),
                    "current", selectedPiece.getPosition(),
                    "target", position
            );

            this.resetSelectionGlow(participant);

            // deselect the board
            participant.setSelected(null);
            this.participants.put(player.getUniqueId(), participant);
            
            if (this.board.move(selectedPiece, position)) {
                // swap the turn for the player
                this.teams.poll();
                this.teams.add(currentTeam); // add them back
                // TODO: Check if the opposing team's king cannot move
            }
        }
    }

    /**
     * Get the player for the associated team
     *
     * @param team The team
     * @return The player if available
     */
    @Nullable
    public ChessPlayer getTeamPlayer(@NotNull PieceTeam team) {
        return this.participants.values().stream()
                .filter(x -> x.getTeam() == team)
                .findFirst()
                .orElse(null);
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
     * Update the text display above the game
     *
     * @param component The component to set
     */
    public void updateTextInfo(@NotNull Component component) {
        EntityUtils.safelyModify(
                TextDisplay.class,
                this.infoBoard,
                display -> display.text(component)
        );
    }

    /**
     * Reset the selection glow for the player
     *
     * @param participant The participant with the selected piece
     */
    public void resetSelectionGlow(@NotNull ChessPlayer participant) {
        ChessPiece selectedPiece = this.board.getPiece(participant.getSelected());
        if (selectedPiece != null) {
            EntityUtils.safelyModify(
                    ItemDisplay.class,
                    selectedPiece.getDisplay(),
                    entity -> {
                        entity.setGlowing(false);
                        entity.setGlowColorOverride(Color.WHITE);
                    });
        }

        this.boardDisplays.values().forEach(uuid -> EntityUtils.safelyModify(BlockDisplay.class,
                uuid,
                entity -> {
                    entity.setGlowing(false);
                    entity.setGlowColorOverride(Color.WHITE);
                }));
    }

    public void updatePiece(@NotNull ChessPiece piece) {
        this.board.set(piece);
    }

    /**
     * Apply a board position to an entity
     *
     * @param container The container to modify
     * @param position  The position to apply
     */
    public void setContainerPos(@NotNull PersistentDataContainer container, @NotNull BoardPosition position) {
        container.set(POSITION_ROW, position.getRow());
        container.set(POSITION_COL, position.getColumn());
    }

    /**
     * Get a board position from an entity
     *
     * @param container The container to read
     */
    @Nullable
    public BoardPosition getContainerPos(@NotNull PersistentDataContainerView container) {
        Integer row = container.get(POSITION_ROW);
        Integer col = container.get(POSITION_COL);
        if (row == null || col == null) return null;
        return new BoardPosition(row, col);
    }

    public ChessBoard getBoard() {
        return board;
    }
}
