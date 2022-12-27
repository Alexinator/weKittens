package edu.vub.at.wekittens.interfaces;

import java.util.List;

import edu.vub.at.wekittens.Card;
import edu.vub.at.wekittens.Deck;

public interface JWeKittens {
    // ################
    // ## AT to Java ##
    // ################

    JWeKittens registerATApp(ATWeKittens weKittens);

    public void foundNewPlayer(String newPlayer);

    public void startGameAT(int playerId, List<Integer> deck, List<List<Integer>> playersCards,List<Integer> playersStates, int nbPlayers);

    public void handleTuple(int cardId, int from, int to, int roundNb);
}
