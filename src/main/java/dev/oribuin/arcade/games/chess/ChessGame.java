package dev.oribuin.arcade.games.chess;

import dev.oribuin.arcade.api.game.ArcadeGame;
import dev.oribuin.arcade.games.chess.board.BoardPosition;
import dev.oribuin.arcade.games.chess.board.ChessBoard;
import dev.oribuin.arcade.games.chess.participant.ChessMaster;
import dev.oribuin.arcade.games.chess.piece.ChessPiece;
import dev.oribuin.arcade.games.chess.piece.PieceTeam;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * chess... in minecraft :)
 */
public class ChessGame extends ArcadeGame<ChessMaster> {

    public static final float SQUARE_SIZE = 0.65f;
    public static final float PIECE_SIZE = 0.55f;

    private ChessBoard board;
    private Queue<PieceTeam> teams;
    private Map<UUID, UUID> boardDisplays;

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

        // I'm stressed out so lets start simple. a checker pattern of blocks
        boolean white = true;
        BlockData whiteConcrete = Material.WHITE_CONCRETE.createBlockData();
        BlockData blackConcrete = Material.BLACK_CONCRETE.createBlockData();
        for (int x = 0; x < size; x++) {
            white = !white; // stops it from being a barcode
            for (int z = 0; z < size; z++) {
                double localX = -(totalWidth / 2F - SQUARE_SIZE / 2F) + (x * SQUARE_SIZE);
                double localZ = -(totalWidth / 2F - SQUARE_SIZE) + (z * SQUARE_SIZE);

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
                        });


                Interaction squareInteraction = squareLoc.getWorld().spawn(
                        squareLoc,
                        Interaction.class,
                        CreatureSpawnEvent.SpawnReason.CUSTOM, interaction -> {
                            this.applyIdentifier(interaction);
                            interaction.setResponsive(true);
                            interaction.setInteractionWidth(SQUARE_SIZE);
                            interaction.setInteractionHeight(SQUARE_SIZE);
                        });

                this.boardDisplays.put(squareInteraction.getUniqueId(), square.getUniqueId());

                // Place the piece associated with the board
                BoardPosition position = new BoardPosition(x + 1, z + 1);
                ChessPiece piece = this.board.getPiece(position);
                if (piece != null) piece.place(this, squareLoc.clone().add(0, 0.45f, 0), direction);
            }
        }

    }

    /**
     * Start the game for the participating players
     */
    @Override
    public void start() {
    }

    /**
     * Stop the game from continuing
     *
     * @param cancelled Whether the game was cancelled
     */
    @Override
    public void stop(boolean cancelled) {

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
    }

    /**
     * Creates a new participant instance for the game
     *
     * @param target The player participant
     * @return The resulting participant
     */
    @Override
    public Supplier<ChessMaster> createParticipant(Player target) {
        return () -> new ChessMaster(target, this.teams.poll());
    }

    /**
     * Handle interacting with the player for the game
     *
     * @param event The event to start
     */
    public void handleInteraction(PlayerInteractAtEntityEvent event) {
//        Player player = event.getPlayer();
//        if (!(event.getRightClicked() instanceof Interaction interaction)) return;
//
//        // Check whether the player is a participant
//        ConnectPlayer participant = this.participants.get(player.getUniqueId());
//        if (participant == null) return;
//        if (!this.rows.containsKey(interaction.getUniqueId())) return;
//
//        // Make sure the interaction is a row
//        Integer row = interaction.getPersistentDataContainer().get(CONNECT_ROW, PersistentDataType.INTEGER);
//        if (row == null) return;
//
//        if (this.dropToken(player, participant.getTokenColour(), row)) {
//            this.turns++;
//            boolean winningToken = this.checkBoard(participant.getTokenColour());
//            if (winningToken) {
//                this.active = false;
//                this.applyUniversalGlow(participant.getTokenColour()); // User won so the whole game should light up
//
//                List<ConnectPlayer> losers = this.participants.values().stream()
//                        .filter(x -> x.getUniqueId() != player.getUniqueId())
//                        .collect(Collectors.toList());
//
//                String loserNames = losers.stream()
//                        .map(x -> x.getPlayer().getName())
//                        .collect(Collectors.joining(", "));
//
//                if (losers.isEmpty()) loserNames = "N/A";
//
//                Messages.get().getPlayerWon().send(this,
//                        "game", ArcadeUtils.niceify(this.name),
//                        "winner", player.getName(),
//                        "losers", loserNames
//                );
//
//                DataManager manager = ArcadePlugin.getInstance().getDataManager();
//                // Update the winner and loser's stats
//                manager.updateStat(player.getUniqueId(), this, GameStats::addWin);
//                losers.forEach(x -> manager.updateStat(x.getUniqueId(), this, GameStats::addLoss));
//
//                PluginScheduler.get().runTaskAtLocationLater(this.location, () -> this.stop(false), 3 * 60);
//                return;
//            }
//
//            if (this.turns >= (this.gridHeight * this.gridWidth)) this.stop(true);
//        } else {
//            Messages.get().getConnectFour().getNotUsersTurn().send(player);
//        }
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

}
