package com.example.stockapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;

public class MyItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final ItemTouchHelperAdapter adapter;
    Context mContext;
    private final Paint mClearPaint;
    private final ColorDrawable mBackground;
    private final int backgroundColor;
    private final Drawable deleteDrawable;
    private final int intrinsicWidth;
    private final int intrinsicHeight;

    public MyItemTouchHelperCallback(Context context, ItemTouchHelperAdapter adapter) {
        mContext = context;
        mBackground = new ColorDrawable();
        backgroundColor = Color.parseColor("#b80f0a");
        mClearPaint = new Paint();
        mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        deleteDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_baseline_delete_24);
        intrinsicWidth = deleteDrawable.getIntrinsicWidth();
        intrinsicHeight = deleteDrawable.getIntrinsicHeight();
        this.adapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.LEFT;

        if ( viewHolder instanceof MainListAdapter.ItemViewHolder ){
            int section1 = ((MainListAdapter.ItemViewHolder) viewHolder).getSection(0);
            int section2 = ((MainListAdapter.ItemViewHolder) viewHolder).getSection(1);
            if ( ( viewHolder.getAdapterPosition() < section1 + section2 + 2 )
                    && ( viewHolder.getAdapterPosition() > section1 + 1 ) ) {
                return makeMovementFlags(dragFlags, swipeFlags);
            }else if ( viewHolder.getAdapterPosition() < section1 + 1 ) {
                return makeMovementFlags(dragFlags, 0);
            }
        }

        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        if ( viewHolder instanceof MainListAdapter.ItemViewHolder ){
            int section1 = ((MainListAdapter.ItemViewHolder) viewHolder).getSection(0);
            int section2 = ((MainListAdapter.ItemViewHolder) viewHolder).getSection(1);
            if ( ( viewHolder.getAdapterPosition() < section1 + section2 + 2 )
                    && ( viewHolder.getAdapterPosition() > section1 + 1 ) ) {
                    adapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition() , 1);
            }else if ( viewHolder.getAdapterPosition() < section1 + 1 ) {
                adapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition(), 0);
            }
        }

        return true;
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder instanceof MainListAdapter.ItemViewHolder) {
                if ( ( viewHolder.getAdapterPosition() > ((MainListAdapter.ItemViewHolder)viewHolder).getSection(0) + 1 )
                        || ( viewHolder.getAdapterPosition() < ((MainListAdapter.ItemViewHolder)viewHolder).getSection(0) + 1 )
                        || ( viewHolder.getAdapterPosition() > 0 ) ) {
                    adapter.onSelectView((MainListAdapter.ItemViewHolder) viewHolder);
                }
            }
        }
        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        if ( viewHolder instanceof MainListAdapter.ItemViewHolder ) {
            if ( viewHolder.getAdapterPosition() >=
                    ((MainListAdapter.ItemViewHolder)viewHolder).getSection(0) + 2 ) {
                adapter.onItemDismiss( viewHolder.getAdapterPosition() );

            }
        }
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        if (viewHolder instanceof MainListAdapter.ItemViewHolder) {
            adapter.onClearView((MainListAdapter.ItemViewHolder) viewHolder);
        }

    }

    public interface ItemTouchHelperAdapter {

        void onItemMove(int fromPosition, int toPosition, int section);
        void onItemDismiss(int position);
        void onClearView(MainListAdapter.ItemViewHolder viewHolder);
        void onSelectView(MainListAdapter.ItemViewHolder viewHolder);
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

        if ( viewHolder instanceof MainListAdapter.ItemViewHolder ) {
            if ( viewHolder.getAdapterPosition() > ((MainListAdapter.ItemViewHolder)viewHolder).getSection(0) + 1 ) {

                super.onChildDraw(c, recyclerView, viewHolder, dX , dY, actionState, isCurrentlyActive);

                View itemView = viewHolder.itemView;
                int itemHeight = itemView.getHeight();

                boolean isCancelled = dX == 0 && !isCurrentlyActive;

                if (isCancelled) {
                    clearCanvas(c, itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    return;
                }

                mBackground.setColor(backgroundColor);
                mBackground.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                mBackground.draw(c);

                int deleteIconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                int deleteIconMargin = (itemHeight - intrinsicHeight) / 2;
                int deleteIconLeft = itemView.getRight() - deleteIconMargin - intrinsicWidth;
                int deleteIconRight = itemView.getRight() - deleteIconMargin;
                int deleteIconBottom = deleteIconTop + intrinsicHeight;

                deleteDrawable.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
                deleteDrawable.draw(c);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
            else if ( ( viewHolder.getAdapterPosition() < ((MainListAdapter.ItemViewHolder)viewHolder).getSection(0) + 1 )
                    && ( viewHolder.getAdapterPosition() > 0 ) ) {

                super.onChildDraw(c, recyclerView, viewHolder, 0, dY, actionState, isCurrentlyActive);

            }

        }
    }

    private void clearCanvas(Canvas c, Float left, Float top, Float right, Float bottom) {
        c.drawRect(left, top, right, bottom, mClearPaint);
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return 0.7f;
    }
}


