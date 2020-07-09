package com.coffee.minimalistnotesaver.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;
import com.coffee.minimalistnotesaver.Model.NoteModel;
import com.coffee.minimalistnotesaver.NotesActivity;
import com.coffee.minimalistnotesaver.OpenedNote;
import com.coffee.minimalistnotesaver.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder>
        implements DraggableItemAdapter<NoteAdapter.NoteViewHolder>, FastScrollRecyclerView.SectionedAdapter {

    private View emptyView;
    private FastScrollRecyclerView recyclerView;

    public static boolean multiSelect = false;

    public static ArrayList<NoteModel> selectedItems = new ArrayList<>();
    public static ArrayList<Long> selectItemId = new ArrayList<>();
    private ArrayList<NoteModel> noteList;

    private Context context;

    private MyCallBack myCallback;

    private int colorPrimary;
    private int colorSelected;

    public interface MyCallBack {
        void onLongRecyclerClick(int position);
    }

    class NoteViewHolder extends AbstractDraggableItemViewHolder {
        long id;
        TextView noteTitle, noteDate;
        View dragHandle;
        CardView cardView;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTitle = itemView.findViewById(R.id.noteTitle);
            noteDate = itemView.findViewById(R.id.noteDate);
            dragHandle = itemView.findViewById(R.id.drag_handle);
            cardView = itemView.findViewById(R.id.noteCards);
        }

        private void selectItem(NoteModel item) {
            if (multiSelect) {
                if (selectedItems.contains(item)) {
                    selectedItems.remove(item);
                    selectItemId.remove(item.getId());
                    NotesActivity.actionMode.setTitle("Selected: " + NoteAdapter.selectedItems.size());
                    Log.d("TAG", "Removed::cardViews:: ");
                } else {
                    selectedItems.add(item);
                    selectItemId.add(item.getId());
                    NotesActivity.actionMode.setTitle("Selected: " + NoteAdapter.selectedItems.size());
                    Log.d("TAG", "Selected::cardViews:: ");
                }
            }
            notifyDataSetChanged();
        }
    }

    public NoteAdapter(Context context, ArrayList<NoteModel> noteList,
                       MyCallBack myCallBack, FastScrollRecyclerView rv, View ev) {
        this.context = context;
        this.noteList = noteList;
        myCallback = myCallBack;
        this.recyclerView = rv;
        this.emptyView = ev;
        setHasStableIds(true);
        colorPrimary = ContextCompat.getColor(context, R.color.colorPrimary);
        colorSelected = ContextCompat.getColor(context, R.color.colorAccent);
        RecyclerView.AdapterDataObserver dataObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkIfEmpty();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                checkIfEmpty();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                checkIfEmpty();
            }
        };
        registerAdapterDataObserver(dataObserver);
    }

    /**
     * Check if Layout is empty and show the appropriate view
     */
    private void checkIfEmpty() {
        //Log.d("TAG", "checkIfEmpty: called");
        if (emptyView != null && recyclerView.getAdapter() != null) {
            boolean emptyViewVisible = recyclerView.getAdapter().getItemCount() == 0;
            emptyView.setVisibility(emptyViewVisible ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(emptyViewVisible ? View.GONE : View.VISIBLE);
        }
    }


    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_layout, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        final NoteModel currentItem = noteList.get(position);
        holder.id = currentItem.getId();
        holder.noteTitle.setText(currentItem.getNoteTitle());
        holder.noteDate.setText(currentItem.getNoteDate());
        holder.itemView.setOnClickListener(v -> {
            if (multiSelect) {
                holder.selectItem(currentItem);
            } else {
                openCurrentNote(currentItem.getNoteTitle() + ".txt");
            }

        });
        holder.itemView.setOnLongClickListener(v -> {
            myCallback.onLongRecyclerClick(position);
            holder.selectItem(currentItem);
            return true;
        });

        if (selectItemId.contains(currentItem.getId())) {
            holder.cardView.setCardBackgroundColor(colorSelected);
            holder.dragHandle.setBackgroundTintList(ColorStateList.valueOf(colorPrimary));
        } else {
            holder.cardView.setCardBackgroundColor(colorPrimary);
            holder.dragHandle.setBackgroundTintList(ColorStateList.valueOf(colorSelected));
        }
    }

    private void openCurrentNote(String file) {
        Intent intent = new Intent(context, OpenedNote.class);
        intent.putExtra("noteTitle", file);
        context.startActivity(intent);
    }

    @Override
    public boolean onCheckCanStartDrag(@NonNull NoteViewHolder holder, int position, int x, int y) {
        View dragHandle = holder.dragHandle;

        int handleWidth = dragHandle.getWidth();
        int handleHeight = dragHandle.getHeight();
        int handleLeft = dragHandle.getLeft();
        int handleTop = dragHandle.getTop();

        return (x >= handleLeft) && (x < handleLeft + handleWidth) &&
                (y >= handleTop) && (y < handleTop + handleHeight);
    }

    @Nullable
    @Override
    public ItemDraggableRange onGetItemDraggableRange(@NonNull NoteViewHolder holder, int position) {
        return null;
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        NoteModel removed = noteList.remove(fromPosition);
        noteList.add(toPosition, removed);
    }

    @Override
    public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
        return false;
    }

    @Override
    public void onItemDragStarted(int position) {
        notifyDataSetChanged();
    }

    @Override
    public void onItemDragFinished(int fromPosition, int toPosition, boolean result) {
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return noteList == null ? 0 : noteList.size();
    }

    @Override
    public long getItemId(int position) {
        return noteList.get(position).getId();
    }

    @NonNull
    @Override
    public String getSectionName(int i) {
        return "";
    }
}