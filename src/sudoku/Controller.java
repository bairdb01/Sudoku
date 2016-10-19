package sudoku;

import com.sun.javafx.collections.ObservableIntegerArrayImpl;
import javafx.collections.ArrayChangeListener;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * TODO: Generate a random board, Gen Board double press should repaint board
 */
public class Controller {
    private int[][] board = new int[9][9];
    private int[][] solvedBoard = new int [9][9];

    @FXML
    Canvas canvas;

    /**
     * Generates a random board
     */
    public void genBoard() {
        // Generate random board
        board = randBoard();
        initGrid(9, 9);
        drawBoard();
    }

    /**
     * Draws the initial grid
     *
     * @param width  width of grid
     * @param height height of grid
     */
    private void initGrid(int width, int height) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0,0,canvas.getWidth(), canvas.getHeight());
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);

        // Draw outline of the grid
        gc.strokeLine(0, 0, 0, canvas.getHeight());
        gc.strokeLine(0, canvas.getHeight(), canvas.getWidth(), canvas.getHeight());
        gc.strokeLine(canvas.getWidth(), canvas.getHeight(), canvas.getWidth(), 0);
        gc.strokeLine(canvas.getWidth(), 0, 0, 0);

        // Draw rows
        for (int i = 0; i < height; i++) {
            if (i % 3 == 0)
                gc.setLineWidth(5);
            else
                gc.setLineWidth(2);
            double curHeight = i * canvas.getHeight() / height;
            gc.strokeLine(0, curHeight, canvas.getWidth(), curHeight);
            gc.setFill(Color.BLACK);
            gc.setLineWidth(10);
        }

        // Draw columns
        for (int i = 1; i < width; i++) {
            if (i % 3 == 0)
                gc.setLineWidth(5);
            else
                gc.setLineWidth(2);

            double curWidth = i * canvas.getWidth() / width;
            gc.strokeLine(curWidth, 0, curWidth, canvas.getHeight());
        }
//        drawBoard();
    }

    /**
     * Draws the board's numbers on the grid
     */
    private void drawBoard (){
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Font myFont = new Font("TimesRoman", canvas.getHeight() / 9);
        gc.setFont(myFont);
        gc.setFill(Color.BLACK);
        double cellHeight = canvas.getHeight() / 9 - 15;
        double cellWidth = canvas.getWidth() / 9 / 3 - 12;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                Double[] cellCoords = getCellCoord(j, i);
                if (board[j][i] > 0)
                    gc.fillText(Integer.toString(board[j][i]), cellCoords[0] + cellWidth, cellCoords[1] + cellHeight);
            }
        }
    }

//    public void drawCell(int row, int col){
//        GraphicsContext gc = canvas.getGraphicsContext2D();
//        Font myFont = new Font("TimesRoman", canvas.getHeight() / 9);
//        gc.setFont(myFont);
//        double cellHeight = canvas.getHeight() / 9 - 15;
//        double cellWidth = canvas.getWidth() / 9 / 3 - 12;
//        Double[] cellCoords = getCellCoord(row, col);
//        gc.setFill(Color.WHITE);
//        gc.fillRect(cellCoords[0], cellCoords[1], canvas.getWidth()/9, canvas.getHeight()/9);
//        gc.setFill(Color.BLACK);
//        gc.fillText(Integer.toString(board[col][row]), cellCoords[0] + cellWidth, cellCoords[1] + cellHeight);
//    }

    /**
     * Checks for any conflicts after placing a number
     * @param board board to check
     * @param row row number
     * @param col column number
     * @return true if conflicted
     */
    private boolean hasConflict(int [][] board, int row, int col){
        if (board[row][col] == 0) {
//            System.out.println("Empty cell " + col + " "  + row);
            return false;
        }

        // Check the column for conflicts
        for (int i = 0; i < 9; i++) {
            if (i == row)
                continue;
            if (board[i][col] == board[row][col]) {
//                System.out.println("Column Conflict " + row + " " + col);
                return true;
            }
        }
        // Check the row for conflicts
        for (int i = 0; i < 9; i++) {
            if (i == col)
                continue;
            if (board[row][i] == board[row][col]) {
//                System.out.println("Row Conflict " + row + ' ' + col);
                return true;
            }
        }

        // Check the sub square for conflicts
        int curCol = 3*(col/3);
        int curRow = 3*(row/3);
        ArrayList<Integer> foundNums = new ArrayList<>();

        // Located at upper left of square
        for(int vert = curRow; vert < (curRow+3);vert++) {
            for (int horz = curCol; horz < (curCol + 3); horz++) {
                //Found number in sub-square
                if (foundNums.contains(board[vert][horz])) {
//                    System.out.println("Duplicate Number " + " " + board[vert][horz]);
                    return true;
                } else {
                    if (board[vert][horz] > 0)
                        foundNums.add(board[vert][horz]);
                }
            }
        }
//        System.out.println("Clear");
        return false;
    }

    /**
     * Wrapper class for GUI, calls the real solveBoard method
     */
    public void solve(){
        this.solvedBoard = this.board;
        solveBoard();
        drawBoard();
    }

    private void printBoard(int [][] board){
        System.out.println();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
    }

    private boolean solveBoard() {
        Random rn = new Random();
        ArrayList<Integer> toPlace = new ArrayList<>(10);
        for (int i = 1; i < 10; i++)
            toPlace.add(i);

        // Go through each space and try to find a number
        for (int i = 0; i < 9; i++) {
            for(int j = 0; j < 9; j++) {
                // Check if spot is empty or not
                if (solvedBoard[i][j] > 0)
                    continue;

                if (toPlace.size() == 0)
                    return false;
                int index = rn.nextInt(toPlace.size());
                solvedBoard[i][j] = toPlace.get(index);

                // Conflict occurs (i.e., a duplicate in any row/column/sub-square),
                if (hasConflict(solvedBoard, i, j)) {
                    // Remove the number and backtrack
                    solvedBoard[i][j] = 0;
                    j--;
                    toPlace.remove(index);
                    continue;
                }

                // Repeat step with new number if there was a conflict
                if (!solveBoard()) {
                    solvedBoard[i][j] = 0;
                    toPlace.remove(index);
                    j--;
                    continue;
                }
            }
        }
        if (!hasConflict(solvedBoard,8,8))
            return true;
        return false;
    }

    /**
     * Generates a random board and it's solution
     * @return the random board
     */
    private int [][] randBoard(){
        int [][] temp;

        temp = new int[9][9];
        Random rn = new Random();

        temp[rn.nextInt(9)][rn.nextInt(9)] = rn.nextInt(9);

        for (int i = 0; i < 9; i++)
            for (int j = 0; j < 9; j++)
                solvedBoard[i][j] = temp[i][j];

        // Loop until found a solveable board
        while(solveBoard() != true) {
            temp = new int[9][9];
            temp[rn.nextInt(9)+ 1][rn.nextInt(9) + 1] = rn.nextInt(9) + 1;
            for (int i = 0; i < 9; i++)
                for (int j = 0; j < 9; j++)
                    solvedBoard[i][j] = temp[i][j];
        }

        // Copy solved board
        for (int i = 0; i < 9; i++)
            for (int j = 0; j < 9; j++)
                temp[i][j] = solvedBoard[i][j];

        // Remove some numbers from solution
        for (int i =0; i < rn.nextInt(70) + 90; i++)
            temp[rn.nextInt(9)][rn.nextInt(9)] = 0;

        printBoard(solvedBoard);
        return temp;
    }

    /**
     * Gets cell coordinate in the canvas
     * @param x cell column
     * @param y cell width
     * @return canvas coordinates
     */
    private Double[] getCellCoord(int x, int y) {
        Double [] cell = new Double[2];
        cell[0] = y*(canvas.getWidth()/9);
        cell[1] = x*(canvas.getHeight()/9);
        return cell;
    }
}
