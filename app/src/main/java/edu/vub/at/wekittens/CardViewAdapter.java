package edu.vub.at.wekittens;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

public class CardViewAdapter extends RecyclerView.Adapter<CardViewAdapter.ViewHolder> implements CardActionCompletionContract {

    private final HandAction mHandActionHandler;
    private List<Card> mCards;
    private LayoutInflater mInflater;

    // data is passed into the constructor
    CardViewAdapter(Context context, List<Card> cards, HandAction handActionHandler ) {
        this.mInflater = LayoutInflater.from(context);
        this.mCards = cards;
        this.mHandActionHandler = handActionHandler;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the view and textview in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Card card = mCards.get(position);
        if (card != null)
            holder.myView.setImageResource(card.getResourceId());
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mCards.size();
    }

    @Override
    public void onViewMoved(int oldPosition, int newPosition) {
        Card targetcard = mCards.get(oldPosition);
        Card card = new Card(targetcard);
        mCards.remove(oldPosition);
        mCards.add(newPosition, card);
        notifyItemMoved(oldPosition, newPosition);
    }

    @Override
    public void onViewSwiped(int position) {
        Card targetcard = mCards.get(position);

        mCards.remove(position);
        notifyItemRemoved(position);

        if (!mHandActionHandler.cardPlayed(targetcard)) {
            Card card = new Card(targetcard);
            mCards.add(position, card);
            notifyItemInserted(position);
        }
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView myView;

        ViewHolder(View itemView) {
            super(itemView);
            myView = itemView.findViewById(R.id.card);
        }
    }

    // convenience method for getting data at click position
    public Card getItem(int id) {
        return mCards.get(id);
    }

    public void addCard(Card card) {
        int position = getItemCount();
        mCards.add(position, card);
        notifyItemInserted(position);
    }


}

