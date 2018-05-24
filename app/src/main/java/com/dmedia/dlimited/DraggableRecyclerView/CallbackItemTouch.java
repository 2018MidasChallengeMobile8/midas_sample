package com.dmedia.dlimited.DraggableRecyclerView;

/**
 * Created by xema0 on 2016-11-21.
 */


/**
 * Interface to listen move in ItemTouchHelper.Callback
 * Created by Alessandro on 15/01/2016.
 */
public interface CallbackItemTouch {

    /**
     * Called when an item has been dragged
     * @param oldPosition start position
     * @param newPosition end position
     */
    void itemTouchOnMove(int oldPosition,int newPosition);
}