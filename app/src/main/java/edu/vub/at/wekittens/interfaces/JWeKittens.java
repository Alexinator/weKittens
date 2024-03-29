package edu.vub.at.wekittens.interfaces;

import java.util.HashMap;
import java.util.List;

import edu.vub.at.wekittens.Card;
import edu.vub.at.wekittens.Deck;

public interface JWeKittens {
    // ################
    // ## AT to Java ##
    // ################

    JWeKittens registerATApp(ATWeKittens weKittens);

    public void foundNewPlayer(String newPlayer, Integer playerId);

    public void startGameAT(int playerId, List<Integer> deck, List<List<Integer>> playersCards, List<Integer> playersStates, int nbPlayers, List<Integer> playersIdsList);

    public void handleTuple(int cardId, int from, int to, int roundNb, boolean personal, List<Integer> deck, List<Integer> states, Integer favorCardId, Integer nopeCardId);

    public void playerDisconnected(int playerId);

    public void removePlayerFromGame(int playerId);

    public void removePlayerFromLobby(int player);
}
