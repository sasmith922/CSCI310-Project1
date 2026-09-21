package com.example.gridlayout;

public class Cell {
    public boolean hasMine;
    public boolean revealed;
    public boolean flagged;
    public int adjacentMines;

    public Cell() {
        hasMine = false;
        revealed = false;
        flagged = false;
        adjacentMines = 0;
    }
}
