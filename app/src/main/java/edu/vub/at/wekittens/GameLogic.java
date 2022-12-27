package edu.vub.at.wekittens;

import java.util.ArrayList;
import java.util.List;

import edu.vub.at.wekittens.Card;
import edu.vub.at.wekittens.Deck;
import edu.vub.at.wekittens.interfaces.ATWeKittens;

public class GameLogic {

        public static GameLogic INSTANCE = null;
        private ATWeKittens atws = LobbyActivity.atws;
        private MainActivity mainActivity = null;

        public static int TOTAL_HAND_CARDS = 8; //maximum 8 cards per hand
        public static int ALIVE = 1; // player is alive
        public static int DEAD = 2; // player is dead
        public static int PLAY = 3; // player's time to play
        public static int WAIT = 4; // player must wait

        private Deck deck;
        private int playerId;
        private static int nbPlayers;
        private int roundNb;
        private List<List<Integer>> playersCards;
        private List<Integer> playersStates;

        /**
         * Constructor for the starter player
         * @param idStarter: starter id (generally 0)
         * @param nbPlayers: the number of players
         */
        public GameLogic(int idStarter, int nbPlayers){
                System.out.println("Game started logic >>"+idStarter+ " "+nbPlayers);
                this.playerId = idStarter;
                this.nbPlayers = nbPlayers;
                this.roundNb = 0; // initial first round
                this.deck = new Deck(nbPlayers); // initialise deck
                this.playersStates = new ArrayList<>(); // initialise the players' states (alive, dead, time to play, ...)
                createHands();
                INSTANCE = this;
                System.out.println("\n###### GAME LOGIC CREATION OK \n######");
        }

        /**
         * Constructor for other players so they can create their GameLogic object
         * @param playerId: the given player id
         * @param deck: the given deck
         * @param playersCards: the given cards
         */
        public GameLogic(int playerId, List<Integer> deck, List<List<Integer>> playersCards,List<Integer> playersStates, int nbPlayers){
                this.playerId = playerId;
                this.deck = new Deck(deck);
                this.playersCards = playersCards;
                this.playersStates = playersStates;
                this.roundNb = 0;
                this.nbPlayers = nbPlayers;
                INSTANCE = this; // make the GameLogic class callable everywhere
        }

        /**
         * Create hands for each player and prepare the deck for the game
         */
        public void createHands(){
                this.playersCards = new ArrayList<>(nbPlayers); // create a list to save cards
                for(int i = 0; i < nbPlayers; i++){
                        this.playersCards.add(new ArrayList<>(TOTAL_HAND_CARDS)); // for each player, initialise his cards list
                        for(int j = 0; j < TOTAL_HAND_CARDS-1; j++){
                                this.playersCards.get(i).add(this.deck.drawCard().getId()); // add a random card to player's cards
                        }
                        this.playersCards.get(i).add(this.deck.takeCard(Card.CardType.defuse).getId()); // add a defuse card (rules)
                        // in total 8 cards have been added to his hand
                }
                this.deck.addExplodingKittens(); // add exploding kittens based on the number of players (rules)

                playersStates = new ArrayList<>(nbPlayers); // now define the state of each player
                this.playersStates.add(this.playerId,PLAY); // starter is the first player
                for(int i = 1; i < nbPlayers; i++) {
                        this.playersStates.add(i, WAIT); // others have to wait
                }
                System.out.println("PLAUERSSTATES>>>>>"+this.playersStates);
        }

        /**
         * Tell if the player can play this round
         * @return a boolean
         */
        public boolean canPlay(){
                return this.playersStates.get(this.playerId) == PLAY;
        }

        /**
         * Play a card
         * @param card the card to play
         * @param from the source player id
         * @param to the destination player id
         */
        public String playCard(Card card, int from, int to){
                System.out.println("Sending "+card.getId()+ " "+from+" "+to);
                this.atws.sendTuple(card.getId(),from,to, this.getRoundNb());
                if(this.playersStates.get(from) == WAIT){
                        return "This is not your turn !";
                }
                else if(this.playersStates.get(from) == DEAD){
                        return "You are dead !";
                }
                else { // alive and his turn, treat the card
                        return stealCard(); //
                }
        }

        private String stealCard(){
                setRoundNb(getRoundNb()+1); // update the round number
                return "ok";
        }

        /**
         * Handle incoming tuples from AT
         * @param cardId the card id
         * @param from the emitter
         * @param to the receiver
         */
        public void handleTuple(int cardId, int from, int to, int roundNb){
                if(mainActivity == null){ // if MainActivity instance is null, retrieve it
                        this.mainActivity = MainActivity.INSTANCE;
                }
                System.out.println("HANDLING TUPLE "+this.playerId);
                System.out.println("cardid: "+cardId + "from "+  from + "to "+ to);
                setRoundNb(roundNb); // update the round number in the game logic too
                addCardToDeck(cardId);
                this.mainActivity.updateView();
        }

        /**
         * Add a received card to the deck
         * @param cardId the card id that has been received
         */
        private void addCardToDeck(int cardId){
                this.deck.addCardToDeck(cardId);

        }

        /**
         * Used to make sure that AT can call this class with jlobby
         */
        public static void test(){
                System.out.println("test ok");
        }

        /**
         * Generated getters and setters
         */
        public Deck getDeck() {
                return this.deck;
        }

        public List<Integer> getDeckList(){
                return this.deck.deckToList();
        }

        public void setDeck(Deck deck) {
                this.deck = deck;
        }

        public int getPlayerId() {
                return this.playerId;
        }

        public void setPlayerId(int idStarter) {
                this.playerId = idStarter;
        }

        public static int getNbPlayers() {
                return nbPlayers;
        }

        public void setNbPlayers(int nbPlayers) {
                this.nbPlayers = nbPlayers;
        }

        public int getRoundNb() {
                return this.roundNb;
        }

        public void setRoundNb(int roundNb) {
                this.roundNb = roundNb;
        }

        public List<List<Integer>> getPlayersCards() {
                return this.playersCards;
        }

        public void setPlayersCards(List<List<Integer>> playersCards) {
                this.playersCards = playersCards;
        }

        public List<Integer> getPlayersStates() {
                return this.playersStates;
        }

        public void setPlayersStates(List<Integer> playersStates) {
                this.playersStates = playersStates;
        }


}
