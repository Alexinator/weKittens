package edu.vub.at.wekittens.interfaces;

import edu.vub.at.objects.coercion.Async;

public interface ATWeKittens {
    // ################
    // ## Java to AT ##
    // ################

    @Async
    void addNewPlayer(String newPlayer);
    // add a new player to the game lobby

    @Async
    void ping(JWeKittens gui);
    // ping AT that the gui is ready

    @Async
    void startGame();
    //start the game when one of the player has pressed the start game button

    @Async
    void sendTuple(int cardId, int from, int to, int roundNb);


}
