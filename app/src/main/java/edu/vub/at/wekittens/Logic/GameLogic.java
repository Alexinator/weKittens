package edu.vub.at.wekittens.Logic;

import java.util.List;

import edu.vub.at.wekittens.Card;
import edu.vub.at.wekittens.Deck;

public class GameLogic {

        private Deck deck;
        private int idStarter;
        private int nbPlayers;
        private int roundNb;
        private List<Card> cards;

        public GameLogic(int idStarter, int nbPlayers){
                this.idStarter = idStarter;
                this.nbPlayers = nbPlayers;
                this.roundNb = 0; // initial first round
                this.deck = new Deck(nbPlayers); // initialise deck
        }
}
