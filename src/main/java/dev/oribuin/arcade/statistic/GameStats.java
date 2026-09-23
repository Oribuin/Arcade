package dev.oribuin.arcade.statistic;

public class GameStats {

    private String identifier;
    private int wins; // In most cases, this is a win but sometimes a score
    private int losses; // In most cases, these are just losses 
    private int plays; // Times the user has played the game\

    /**
     * Get the user's current stats for the game
     *
     * @param wins   The amount of wins (or high score)
     * @param losses The amount of losses (or 0 if high scored)
     * @param plays  The plays for the
     */
    public GameStats(int wins, int losses, int plays) {
        this.wins = wins;
        this.losses = losses;
        this.plays = plays;
    }

    /**
     *
     * Get the user's current stats for the game
     */
    public GameStats() {
        this.wins = 0;
        this.losses = 0;
        this.plays = 0;
    }

    /**
     * Add a win to the user's stats
     */
    public void addWin() {
        this.wins++;
        this.plays++;
    }

    /**
     * Set's the user's high score
     *
     * @param score The player's score
     */
    public void setHighScore(int score) {
        if (this.wins < score) this.wins = score;
        this.plays++;
    }

    /**
     * Add a loss to the player
     */
    public void addLoss() {
        this.plays++;
        this.losses++;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public int getPlays() {
        return plays;
    }

    public void setPlays(int plays) {
        this.plays = plays;
    }
    
}
