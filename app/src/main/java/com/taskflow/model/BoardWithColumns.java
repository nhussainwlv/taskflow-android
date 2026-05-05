package com.taskflow.model;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

/**
 * BoardWithColumns - Relationship class for Board with its Columns.
 * 
 * Used for loading a complete board structure with all columns.
 */
public class BoardWithColumns {

    @Embedded
    private Board board;

    @Relation(
        parentColumn = "id",
        entityColumn = "board_id"
    )
    private List<Column> columns;

    // ========================================
    // GETTERS & SETTERS
    // ========================================
    
    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    /**
     * Get the column count for this board
     */
    public int getColumnCount() {
        return columns != null ? columns.size() : 0;
    }
}
