package com.example.gridlayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Queue;
import java.util.ArrayDeque;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int BOARD_SIZE = 10;
    private static final int MINE_COUNT = 6;

    private Cell[][] board;
    private TextView[][] cellViews;

    private GridLayout gameGrid;
    private TextView mineCounterText;
    private TextView timerText;

    private boolean flagMode = false;
    private boolean gameEnded = false;
    private boolean waitingForResultsTap = false;
    private boolean playerWon = false;

    private int flagsPlaced = 0;
    private int safeCellsRevealed = 0;
    private int elapsedSeconds = 0;

    private Runnable timerRunnable;

    // save the TextViews of all cells in an array, so later on,
    // when a TextView is clicked, we know which cell it is
    private ArrayList<TextView> cell_tvs;

    private int dpToPixel(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        cell_tvs = new ArrayList<TextView>();
//
//
//
//
//        // add dynamically added cells
//        LayoutInflater li = LayoutInflater.from(this);
//        for (int i = 0; i<BOARD_SIZE; i++) {
//            for (int j=0; j<BOARD_SIZE; j++) {
//                TextView tv = (TextView) li.inflate(R.layout.custom_cell_layout, grid, false);
//                //tv.setText(String.valueOf(i)+String.valueOf(j));
//                tv.setTextColor(Color.GRAY);
//                tv.setBackgroundColor(Color.GRAY);
//                tv.setOnClickListener(this::onClickTV);
//
//                GridLayout.LayoutParams lp = (GridLayout.LayoutParams) tv.getLayoutParams();
//                lp.rowSpec = GridLayout.spec(i);
//                lp.columnSpec = GridLayout.spec(j);
//
//                grid.addView(tv, lp);
//
//                cell_tvs.add(tv);
//            }
//        }

    }

    private void startNewGame() {

    }

    // create empty grid layour of cells
    private void createEmptyBoard() {
        for(int i = 0; i < BOARD_SIZE; i++) {
            for(int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = new Cell();
            }
        }
    }

    // randomly, dynamically assign mines to grid layout
    private void placeMines() {
        Random random = new Random();
        int minesPlaced = 0;

        while(minesPlaced < MINE_COUNT) {
            int row = random.nextInt(BOARD_SIZE);
            int col = random.nextInt(BOARD_SIZE);

            if(!board[row][col].hasMine) {
                board[row][col].hasMine = true;
                minesPlaced++;
            }
        }
    }

    // loop through all cells and count their adjacent mines
    private void countAdjacentMines() {
        for(int i = 0; i < BOARD_SIZE; i++) {
            for(int j = 0; j < BOARD_SIZE; j++) {
                if(board[i][j].hasMine) {
                    continue;
                }

                int mineCount = 0;
                for(int rowDiff = -1; rowDiff <= 1; rowDiff++) {
                    for(int colDiff = -1; colDiff <= 1; colDiff++) {
                        int adjRow = i + rowDiff;
                        int adjCol = j + colDiff;

                        if(isValidCell(adjRow, adjCol) && board[adjRow][adjCol].hasMine) {
                            mineCount++;
                        }
                    }
                }
                board[i][j].adjacentMines = mineCount;

            }
        }
    }

    // creates TextViews for all 10x10 grid cells
    private void buildGridViews() {
        gameGrid.removeAllViews(); // reset

        int cellSize = gameGrid.getWidth() / BOARD_SIZE;

        for(int i = 0; i < BOARD_SIZE; i++) {
            for(int j = 0; j < BOARD_SIZE; j++) {
                final int curr_row = i;
                final int curr_col = j;

                // create cellView
                TextView cellView = new TextView(this);
                cellView.setTextSize(16);
                cellView.setTextColor(Color.BLACK);
                //cellView.setGravity(Gravity.CENTER);
                cellView.setPadding(0, 0, 0, 0);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                        GridLayout.spec(i),
                        GridLayout.spec(j)
                );

                params.width = cellSize;
                params.height = cellSize;
                params.setMargins(1, 1, 1, 1);

                cellView.setLayoutParams(params);
                cellView.setOnClickListener(view -> handleCellTap(curr_row, curr_col));

                // add cellView to grid
                gameGrid.addView(cellView);
                cellViews[i][j] = cellView;

                renderCell(i, j);
            }
        }
    }

    private void handleCellTap(int row, int col) {
        // after a win/loss, tap will open results
        if (waitingForResultsTap) {
            openResultsScreen();
            return;
        }

        if(gameEnded) {
            return;
        }

        Cell currCell = board[row][col];

        if(flagMode) {
            toggleFlag(row, col);
        } else {
            revealCell(row, col);
        }
    }

    private void toggleFlag(int row, int col) {
        Cell currCell = board[row][col];

        if(currCell.revealed) {
            return;
        }

        // toggle flag
        currCell.flagged = !currCell.flagged;
        if(currCell.flagged) {
            flagsPlaced++;
        } else {
            flagsPlaced--;
        }

        updateMineCounter();
        renderCell(row, col);
    }

    // for when clicking on a cell that can be revealed
    private void revealCell(int row, int col) {
        Cell currCell = board[row][col];

        if(currCell.revealed || currCell.flagged) {
            return;
        }

        if(currCell.hasMine) {
            finishGame(false);
            return;
        }

        // cell is safe
        revealSafeCell(row, col);
        if(currCell.adjacentMines == 0) {
            revealEmptyArea(row, col);
        }

        checkForWin(); // always win condition check after revealing new cell
    }

    // for when clicking on cell that is safe
    private void revealSafeCell(int row, int col) {
        Cell currCell = board[row][col];

        if(currCell.revealed || currCell.flagged || currCell.hasMine) { // do nothing
            return;
        }

        currCell.revealed = true;
        safeCellsRevealed++;
        renderCell(row, col);
    }

    // for clearing large safe areas
    private void revealEmptyArea(int row, int col) {
        // perform BFS to see all touching cells that are safe
        Queue<int[]> cellsQueue = new ArrayDeque<>();
        cellsQueue.add(new int[]{row, col}); // add initial cell

        while(!cellsQueue.isEmpty()) {
            int[] coords = cellsQueue.remove();
            int i = coords[0];
            int j = coords[1];

            // check adjacent rows
            for(int rowDiff = -1; rowDiff <= 1; rowDiff++) {
                for (int colDiff = -1; colDiff <= 1; colDiff++) {
                    int adjRow = i + rowDiff;
                    int adjCol = j + colDiff;

                    if(!isValidCell(adjRow, adjCol)) { // skip out of bounds
                        continue;
                    }

                    Cell adjacent = board[adjRow][adjCol];

                    if(adjacent.revealed || adjacent.flagged || adjacent.hasMine) { // skip non-safe cells
                        continue;
                    }

                    revealSafeCell(adjRow, adjCol);
                    if(board[adjRow][adjCol].adjacentMines == 0) {
                        cellsQueue.add(new int[]{adjRow, adjCol}); // recursive element
                    }
                }
            }
        }
    }

    // check for win by seeing if all safe cells have been revealed
    private void checkForWin() {
        int safeCells = BOARD_SIZE * BOARD_SIZE - MINE_COUNT;
        if(safeCells == safeCellsRevealed) {
            finishGame(true);
        }
    }

    // output logic for when a game is finished
    private void finishGame(boolean won) {

    }

    // output logic for when clicking on a cell
    private void renderCell(int row, int col) {


    }

    private boolean isValidCell(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    private void updateMineCounter() {}

    private void openResultsScreen() {}



    private int findIndexOfCellTextView(TextView tv) {
        for (int n=0; n<cell_tvs.size(); n++) {
            if (cell_tvs.get(n) == tv)
                return n;
        }
        return -1;
    }

    public void onClickTV(View view){
        TextView tv = (TextView) view;
        int n = findIndexOfCellTextView(tv);
        int i = n/BOARD_SIZE;
        int j = n%BOARD_SIZE;
        tv.setText(String.valueOf(i)+String.valueOf(j));
        if (tv.getCurrentTextColor() == Color.GRAY) {
            tv.setTextColor(Color.GREEN);
            tv.setBackgroundColor(Color.parseColor("lime"));
        }else {
            tv.setTextColor(Color.GRAY);
            tv.setBackgroundColor(Color.LTGRAY);
        }
    }
}