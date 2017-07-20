package minesweeper.model;

import java.util.Map;

/**
 * Created by andrey on 19.07.17.
 */
public class GameField {

    enum Cell {
        MINE, FLAG, EMPTY(-1), OPENED, N1(1), N2(2), N3(3), N4(4), N5(5), N6(6), N7(7), N8(8), N9(9);

        private Map<Integer, Cell> numbers;
        private int intValue;

        Cell() {
        }

        Cell(int number) {
            intValue = number;
        }

        int getNumberInt() {
            return intValue;
        }

        Cell increment() {
            switch (this.intValue) {
                case 0:
                    return this;
                case -1:
                    return N1;
                case 1:
                    return N2;
                case 2:
                    return N3;
                case 3:
                    return N4;
                case 4:
                    return N5;
                case 5:
                    return N6;
                case 6:
                    return N7;
                case 7:
                    return N8;
                case 8:
                    return N9;
            }
            return null;
        }

    }

    private Cell[][] field;
    private Cell[][] bufferForUnflag;
    private char[][] userField;
    private final int TOTAL_MINES_COUNT;
    private int flagsOnField;
    private int minesGuessed;
    private int unseenCells;


    GameField(int fieldWidth, int fieldHeight, int minesCount) throws MinesGameException {

        if (fieldWidth <= 1 || fieldHeight <= 1) {
            throw new TooFewFieldSizeException();
        }
        if (minesCount >= fieldHeight * fieldWidth) {
            throw new TooManyMinesException();
        }

        TOTAL_MINES_COUNT = minesCount;
        field = new Cell[fieldHeight][fieldWidth];
        bufferForUnflag = new Cell[fieldHeight][fieldWidth];
        userField = new char[fieldHeight][fieldWidth];
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[0].length; j++) {
                field[i][j] = Cell.EMPTY;
                userField[i][j] = 'C';
            }
        }
        unseenCells = fieldHeight * fieldWidth - minesCount;
    }

    void prepareMines(int x, int y) {
        for (int i = 0; i < TOTAL_MINES_COUNT; i++) {
            boolean success = false;
            while (!success) {
                int randomX = getRandomInt(0, field[0].length);
                int randomY = getRandomInt(0, field.length);
                if (randomX == x && randomY == y) continue;

                boolean mineCanPlantedHere = true;
                if (TOTAL_MINES_COUNT < field[0].length * field.length - 9) {
                    for (int xIt = -1; xIt < 2 && mineCanPlantedHere; xIt++) {
                        for (int yIt = -1; yIt < 2 && mineCanPlantedHere; yIt++) {
                            if (randomX + xIt == x && randomY + yIt == y) mineCanPlantedHere = false;
                        }
                    }
                }
                if (!mineCanPlantedHere) continue;

                if (field[randomY][randomX] == Cell.MINE) continue;
                field[randomY][randomX] = Cell.MINE;
                bufferForUnflag[randomY][randomX] = Cell.MINE;
                for (int xIt = -1; xIt < 2; xIt++) {
                    for (int yIt = -1; yIt < 2; yIt++) {
                        if (xIt == 0 && yIt == 0) continue;
                        if (!checkBounds(randomX, randomY, xIt, yIt)) continue;
                        field[randomY + yIt][randomX + xIt] = field[randomY + yIt][randomX + xIt].increment();
                        bufferForUnflag[randomY + yIt][randomX + xIt] = field[randomY + yIt][randomX + xIt];
                    }
                }
                success = true;
            }
        }
        openCell(x, y);
    }

    private int getRandomInt(int start, int end) {
        return (int) (100 * Math.random()) % (end - start);
    }

    boolean openCell(int x, int y) {
        if (field[y][x] == Cell.MINE) return false;
        openCells(x, y);
        return true;
    }

    void setLoseMine(int x, int y) {
        userField[y][x] = 'R';
    }

    private void openCells(int x, int y) {
        if (!checkBounds(x, y, 0, 0)) {
            return;
        }

        if (field[y][x] == Cell.MINE || field[y][x] == Cell.FLAG || field[y][x] == Cell.OPENED) {
            return;
        }
        if (field[y][x].getNumberInt() > 0) {
            if (userField[y][x] == 'C') unseenCells--;
            userField[y][x] = ("" + field[y][x].getNumberInt()).charAt(0);
            return;
        }

        if (field[y][x] == Cell.EMPTY) {
            field[y][x] = Cell.OPENED;
            unseenCells--;
            userField[y][x] = 'O';
        }

        for (int xIt = -1; xIt < 2; xIt++) {
            for (int yIt = -1; yIt < 2; yIt++) {
                if (xIt == 0 && yIt == 0) {
                    continue;
                }
                openCells(x + xIt, y + yIt);
            }
        }

    }

    private boolean checkBounds(int x, int y, int dx, int dy) {
        return !(y + dy < 0 || x + dx < 0 ||
                y + dy >= field.length || x + dx >= field[0].length);
    }

    boolean setMine(int x, int y) {
        if (flagsOnField == TOTAL_MINES_COUNT) return false;
        flagsOnField++;
        boolean guessed = field[y][x] == Cell.MINE;
        userField[y][x] = 'F';
        if (guessed) {
            minesGuessed++;
        }
        return true;
    }

    void unsetMine(int x, int y) {
        field[y][x] = bufferForUnflag[y][x];
        userField[y][x] = 'C';
        flagsOnField--;
    }

    void openMines() {
        int mineFound = 0;
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[0].length; j++) {
                if (field[i][j] == Cell.MINE) {
                    mineFound++;
                    userField[i][j] = 'M';
                }
                if (mineFound == TOTAL_MINES_COUNT) return;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder fieldAsString = new StringBuilder(field.length * (field[0].length + 1));
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[0].length; j++) {
                switch (field[i][j]) {
                    case MINE:
                        fieldAsString.append('M');
                        break;
                    case FLAG:
                        fieldAsString.append('F');
                        break;
                    case EMPTY:
                        fieldAsString.append('-');
                        break;
                    case OPENED:
                        fieldAsString.append('S');
                        break;
                    default:
                        fieldAsString.append(field[i][j].getNumberInt());
                        break;
                }
            }
            fieldAsString.append('\n');
        }
        return fieldAsString.toString();
    }

    char[][] getUserField() {
        return userField;
    }

    boolean isWin() {
        return unseenCells == 0 || minesGuessed == TOTAL_MINES_COUNT;
    }
}
