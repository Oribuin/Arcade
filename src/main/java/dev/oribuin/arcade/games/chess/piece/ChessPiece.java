package dev.oribuin.arcade.games.chess.piece;

import com.destroystokyo.paper.profile.PlayerProfile;
import dev.oribuin.arcade.games.chess.ChessGame;
import dev.oribuin.arcade.games.chess.board.BoardPosition;
import dev.oribuin.arcade.games.chess.board.ChessBoard;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import javax.swing.plaf.synth.SynthTableHeaderUI;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static dev.oribuin.arcade.games.chess.ChessGame.PIECE_SIZE;
import static dev.oribuin.arcade.games.chess.ChessGame.SQUARE_SIZE;

/**
 * Represents a piece that will exist on the board for a player to move
 */
public abstract class ChessPiece {

    protected final PieceType type;
    protected final PieceTeam team;
    protected BoardPosition position;
    protected UUID display;

    /**
     * Create a new chess piece to be placed on the board
     *
     * @param type     The type of chess piece
     * @param team     The team the board is on
     * @param position The position of the piece
     */
    public ChessPiece(PieceType type, PieceTeam team, BoardPosition position) {
        this.type = type;
        this.team = team;
        this.position = position;
        this.display = null;
    }

    /**
     * Apply a function to the display entity on the board
     *
     * @return The function to apply
     */
    public abstract Consumer<ItemDisplay> apply();

    /**
     * Place a piece on the board
     *
     * @param location  The location of the piece
     * @param direction The direction of the piece
     */
    public void place(@NotNull ChessGame game, @NotNull Location location, @NotNull BlockFace direction) {
        Transformation transformation = new Transformation(
                new Vector3f(),
//                new Vector3f(-(PIECE_SIZE / 2), 0, -(PIECE_SIZE / 2)),
                new AxisAngle4f(),
                new Vector3f(PIECE_SIZE, PIECE_SIZE, PIECE_SIZE),
                new AxisAngle4f()
        );
        
        ItemDisplay entity = location.getWorld().spawn(
                location,
                ItemDisplay.class,
                CreatureSpawnEvent.SpawnReason.CUSTOM, x -> {
                    game.applyIdentifier(x);
                    x.setBrightness(new Display.Brightness(15, 15));
                    x.setDisplayWidth(PIECE_SIZE);
                    x.setDisplayHeight(PIECE_SIZE);
                    x.setBillboard(Display.Billboard.FIXED);
                    x.setTransformation(transformation);
                    
                    this.apply().accept(x);
                });

        this.display = entity.getUniqueId();
    }

    /**
     * Move the chess piece to the new position
     *
     * @param position The new position of the piece
     */
    public void move(@NotNull BoardPosition position) {
        this.position = position;
        // TODO: move the entity
    }

    /**
     * Remove a chess piece from the board
     */
    public void remove() {
        this.position = null;
        // TODO: Remove the entity
    }

    /**
     * Gets the available positions the piece can move to
     *
     * @param board The board to check against
     * @return The available positions
     */
    @NotNull
    public abstract List<BoardPosition> getAvailable(@NotNull ChessBoard board);

    /**
     * Create a skull texture for the chess piece
     *
     * @param texture The texture for the piece
     * @return The resulting skull texture
     */
    @NotNull
    public static ItemStack createSkullData(@NotNull String texture) {
        ItemStack data = new ItemStack(Material.PLAYER_HEAD);
        try {
            PlayerProfile profile = Bukkit.createProfile(UUID.nameUUIDFromBytes(texture.getBytes()), "");
            PlayerTextures textures = profile.getTextures();

            String decodedTextureJson = new String(Base64.getDecoder().decode(texture));
            String decodedTextureUrl = decodedTextureJson.substring(28, decodedTextureJson.length() - 4);

            textures.setSkin(URI.create(decodedTextureUrl).toURL());
            profile.setTextures(textures);

            data.setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile(profile));
        } catch (MalformedURLException | NullPointerException | IllegalArgumentException ignored) {
        }
        
        return data;
    }

    public UUID getDisplay() {
        return display;
    }

    public PieceType getType() {
        return type;
    }

    public PieceTeam getTeam() {
        return team;
    }

    public BoardPosition getPosition() {
        return position;
    }

    public void setPosition(BoardPosition position) {
        this.position = position;
    }
}
