package com.nhance.android.pojos;

import java.util.ArrayList;
import java.util.List;

import com.nhance.android.db.models.entity.BoardModel;

public class BoardTree {

    public int             _id;
    public String          name;
    public String          id;
    public String          code;
    public String          type;
    public int             parentId;
    public List<BoardTree> children;

    public BoardTree(BoardModel board) {

        super();
        this._id = board._id;
        this.name = board.name;
        this.id = board.id;
        this.code = board.code;
        this.type = board.type;
        this.parentId = board.parentId;
    }

    public void addChild(BoardModel board) {

        if (children == null) {
            children = new ArrayList<BoardTree>();
        }
        children.add(new BoardTree(board));
    }

    public void addChild(BoardTree board) {

        if (children == null) {
            children = new ArrayList<BoardTree>();
        }
        children.add(board);
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BoardTree other = (BoardTree) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{_id:").append(_id).append(", name:").append(name).append(", id:")
                .append(id).append(", code:").append(code).append(", type:").append(type)
                .append(", children:").append(children).append("}");
        return builder.toString();
    }

}
