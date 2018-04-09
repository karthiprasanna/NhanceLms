package com.nhance.android.db.datamanagers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.models.entity.BoardModel;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.managers.LocalManager;
import com.nhance.android.pojos.BoardTree;
import com.nhance.android.pojos.VedantuDBResult;
import com.nhance.android.utils.SQLDBUtil;

public class BoardDataManager extends AbstractDataManager {

    public static final int    NO_BRD_ID = -1;
    public static final String TABLE     = "board";
    private final String       TAG       = "BoardDatamanager";

    public BoardDataManager(Context context) {
        super(context);
    }

    public List<BoardTree> toBoardTree(List<BoardModel> boards) {

        List<BoardTree> boardTree = new ArrayList<BoardTree>();
        Map<Integer, BoardTree> boardMap = new LinkedHashMap<Integer, BoardTree>();
        for (BoardModel b : boards) {
            BoardTree bt = new BoardTree(b);
            boardMap.put(Integer.valueOf(b._id), bt);
        }
        for (Entry<Integer, BoardTree> entry : boardMap.entrySet()) {
            Integer parentKey = Integer.valueOf(entry.getValue().parentId);
            if (entry.getValue().parentId < 0) {
                boardTree.add(entry.getValue());
            } else if (boardMap.get(parentKey) != null) {
                boardMap.get(parentKey).addChild(entry.getValue());
            }
        }
        boardMap.clear();
        Log.v(TAG, "returning boardTree: " + boardTree);
        return boardTree;
    }

    public List<BoardTree> getBoardTree(Content content) {

        String brdIds = content.brdIds;
        if (TextUtils.isEmpty(brdIds)) {
            return null;
        }
        String[] ids = TextUtils.split(brdIds, SQLDBUtil.SEPARATOR);
        List<String> updateIds = new ArrayList<String>();
        for (String id : ids) {
            try {
                if (!TextUtils.isEmpty(id)) {
                    updateIds.add(id);
                }
            } catch (Exception e) {}
        }
        List<BoardTree> boardTree = toBoardTree(getBoards(updateIds));
        return boardTree;
    }

    public List<BoardModel> getBoards(List<String> ids) {

        List<BoardModel> boards = new ArrayList<BoardModel>();
        StringBuilder sb = new StringBuilder();

        addSelectQuery(sb, TABLE);
        sb.append("WHERE ");

        sb.append(ConstantGlobal._ID).append(" IN (")
                .append(LocalManager.join(ids, SQLDBUtil.SEPARATOR_COMMA)).append(") ");
        Cursor cursor = rawQuery(sb.toString(), null);

        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                BoardModel board = SQLDBUtil.convertToValues(cursor, BoardModel.class, null);
                boards.add(board);
                cursor.moveToNext();
            }
        }
        closeCursor(cursor);
        Log.v(TAG, "returning boards:" + boards);

        return boards;
    }

    public BoardModel getBoard(String id, String type) {

        BoardModel board = null;
        if (TextUtils.isEmpty(id)) {
            return board;
        }
        StringBuilder sb = new StringBuilder();

        addSelectQuery(sb, TABLE);
        sb.append("WHERE ");
        addStringEqualSQLQuery(ConstantGlobal.ID, id, sb, false);
        if (!TextUtils.isEmpty(type)) {
            sb.append(SQL_KEYWORD_AND);
            addStringEqualSQLQuery(ConstantGlobal.TYPE, type, sb, false);
        }
        Cursor cursor = rawQuery(sb.toString(), null);

        if (cursor != null && cursor.moveToFirst()) {
            board = SQLDBUtil.convertToValues(cursor, BoardModel.class, null);
        }
        closeCursor(cursor);
        Log.v(TAG, "returning board:" + board);

        return board;
    }

    public VedantuDBResult<BoardModel> getChildBoards(int parentId, String orderBy,
            String sortOrder, int start, int size) {

        VedantuDBResult<BoardModel> boards = new VedantuDBResult<BoardModel>(0);
        StringBuilder sb = new StringBuilder();

        addSelectQuery(sb, TABLE);
        sb.append("WHERE ");
        addIntEqualSQLQuery(ConstantGlobal.PARENT_ID, parentId, sb);
        addLimitFilter(sb, orderBy, sortOrder, start, size);
        Cursor cursor = rawQuery(sb.toString(), null);

        if (cursor != null && cursor.moveToFirst()) {
            boards.totalHits = cursor.getCount();
            while (!cursor.isAfterLast()) {
                BoardModel board = SQLDBUtil.convertToValues(cursor, BoardModel.class, null);
                boards.entities.add(board);
                cursor.moveToNext();
            }
        }
        closeCursor(cursor);
        Log.v(TAG, "returning boards:" + boards);

        return boards;
    }

    /**
     * @param board
     * @throws Exception
     */
    public BoardModel upsertBoard(BoardModel board) throws Exception {

        BoardModel pBoard = getBoard(board.id, board.type);
        if (pBoard != null) {
            Log.e(TAG, "board: " + pBoard.name + ", alredy present");
            if (!pBoard.name.equals(board.name)) {
                Log.d(TAG, "board: " + pBoard.name + " name has changed to " + board.name);
                ContentValues values = board.toContentValues();
                update(TABLE, values, "_id=" + pBoard._id, null);
                pBoard.name = board.name;
                pBoard.code = board.code;
            }
            return pBoard;
        }
        Log.v(TAG, "inserting into board table");
        ContentValues values = board.toContentValues();
        long id = insert(TABLE, values);
        board._id = (int) id;
        return board;
    }

    public static void createTable(List<String> createTables) {

        StringBuilder sb = new StringBuilder();

        addCreateTableQuery(sb, TABLE);
        addAbstractAbstractDataModelFeildsRow(sb);
        sb.append(ConstantGlobal.NAME).append(" text not null,");
        sb.append(ConstantGlobal.ID).append(" text not null,");
        sb.append(ConstantGlobal.TYPE).append(" text not null,");
        sb.append(ConstantGlobal.CODE).append(" text ,");
        sb.append(ConstantGlobal.PARENT_ID).append(" integer");

        endCreateTableQuery(sb);
        createTables.add(sb.toString());
        createTables.add(createIndexQuery(TABLE, "board_index", true, ConstantGlobal.ID,
                ConstantGlobal.TYPE));
    }

    @Override
    public void createDummyData() {

    }

    @Override
    public void cleanData() {

    }

}
