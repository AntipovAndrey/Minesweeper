package minesweeper.model;

/**
 * Created by andrey on 19.07.17.
 */
public class MinesGame {

    private GameField gameField;
    private boolean firstClick;
    private boolean lose;

    public MinesGame(int fieldWidth, int fieldHeight, int minesCount) throws MinesGameException {
        gameField = new GameField(fieldWidth, fieldHeight, minesCount);
        firstClick = true;
    }

    public boolean guessMine(int x, int y) {
        if (firstClick) return false;
        gameField.setFlag(x, y);
        return true;
    }

    public boolean unFlag(int x, int y) {
        if (firstClick) return false;
        gameField.unsetFlag(x, y);
        return true;
    }

    public boolean openCell(int x, int y) {
        if (firstClick) {
            firstClick = false;
            gameField.prepareMines(x, y);
            return true;
        }
        if (!gameField.openCell(x, y)) {
            gameField.openMines();
            gameField.setLoseMine(x, y);
            lose = true;
            return false;
        }
        return true;
    }


    @Override
    public String toString() {
        StringBuilder toReturn = new StringBuilder();
        char[][] charField = gameField.getUserField();
        for (int i = 0; i < charField.length; i++) {
            for (int j = 0; j < charField[0].length; j++) {
                toReturn.append(charField[i][j]);
            }
            toReturn.append('\n');
        }
        toReturn.append("--------------\n");
        return toReturn.append(gameField.toString()).toString();
    }

    public static void main(String[] args) throws MinesGameException {

    }

    public char[][] getUserField() {
        return gameField.getUserField();
    }

    public boolean isWin() {
        return lose || gameField.isWin();
    }
}
