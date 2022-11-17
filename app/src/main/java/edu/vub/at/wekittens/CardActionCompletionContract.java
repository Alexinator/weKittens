package edu.vub.at.wekittens;

public interface CardActionCompletionContract {
    void onViewMoved(int oldPosition, int newPosition);
    void onViewSwiped(int position);
}
