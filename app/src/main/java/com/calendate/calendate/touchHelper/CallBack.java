package com.calendate.calendate.touchHelper;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.calendate.calendate.R;
import com.calendate.calendate.UserListFragment;
import com.calendate.calendate.models.EventRow;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CallBack extends ItemTouchHelper.SimpleCallback {


    private FirebaseRecyclerAdapter adapter; // this will be your recycler adapter

    private DatabaseReference root = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    /**
     * Make sure you pass in your RecyclerAdapter to this class
     */
    public CallBack(int dragDirs, int swipeDirs, FirebaseRecyclerAdapter adapter) {
        super(dragDirs, swipeDirs);
        this.adapter = adapter;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        if (direction == ItemTouchHelper.LEFT) {
            int position = viewHolder.getAdapterPosition(); // this is how you can get the position
            final EventRow eventRow = (EventRow) adapter.getItem(position); // You will have your own class ofcourse.

            AlertDialog.Builder builder = new AlertDialog.Builder(viewHolder.itemView.getContext());
            builder.setTitle(R.string.delete_title)
                    .setMessage(R.string.confirm_delete)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // then you can delete the object
                            root.child("all_events").child(user.getUid()).child(eventRow.getEventUID()).removeValue();
                            dialogInterface.dismiss();
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).show();
            adapter.notifyItemChanged(position);
        }
        if (direction == ItemTouchHelper.RIGHT){
            int position = viewHolder.getAdapterPosition();
            final EventRow eventRow = (EventRow) adapter.getItem(position);

            UserListFragment userListFragment = new UserListFragment();

            if (viewHolder.itemView.getContext() instanceof FragmentActivity) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("model", eventRow);
                userListFragment.setArguments(bundle);
                userListFragment.show(((FragmentActivity) viewHolder.itemView.getContext()).getSupportFragmentManager(), "fragment");
            }
            adapter.notifyItemChanged(position);

        }
    }

    private Paint p = new Paint();

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

        Bitmap icon;
        if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){

            View itemView = viewHolder.itemView;
            float height = (float) itemView.getBottom() - (float) itemView.getTop();
            float width = height / 3;

            if(dX > 0){
                p.setColor(Color.parseColor("#388E3C"));
                RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX,(float) itemView.getBottom());
                c.drawRect(background,p);
                icon = BitmapFactory.decodeResource(itemView.getResources(), R.mipmap.ic_share);
                RectF icon_dest = new RectF((float) itemView.getLeft() + width ,(float) itemView.getTop() + width,(float) itemView.getLeft()+ 2*width,(float)itemView.getBottom() - width);
                c.drawBitmap(icon,null,icon_dest,p);
            } else {
                p.setColor(Color.parseColor("#D32F2F"));
                RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(),(float) itemView.getRight(), (float) itemView.getBottom());
                c.drawRect(background,p);
                icon = BitmapFactory.decodeResource(itemView.getResources(), R.mipmap.ic_delete);
                RectF icon_dest = new RectF((float) itemView.getRight() - 2*width ,(float) itemView.getTop() + width,(float) itemView.getRight() - width,(float)itemView.getBottom() - width);
                c.drawBitmap(icon,null,icon_dest,p);
            }
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

}