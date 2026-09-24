package dev.oribuin.arcade.games.chess.piece;

public enum MoveCheckResult {
    AVAILABLE_SPACE,
    ENEMY,
    ALLY,
    IMMUNE,
    OUT_OF_BOUNDS;

    public boolean isTakeable() {
        return this == AVAILABLE_SPACE || this == ENEMY;
    }


}