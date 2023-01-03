package edu.vub.at.wekittens;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
        public static int PENDING = 5; // player is waiting for an answer
        public static int RESPONSE = 6; // player must respond to a card
        public static int NEXTTURN = 50; // player's next turn

        private Deck deck;
        private int playerId;
        private static int nbPlayers;
        private int roundNb;
        private List<List<Integer>> playersCards;
        private List<Integer> playersStates;

        private HashMap<Integer, Integer> mapPlayersIds;
        private List<Integer> listPlayerdsIds; // used for AT passing

        private Card lastCardPlayed;
        private int mustRespondToPlayerId;

        /**
         * Constructor for the starter player
         * @param idStarter: starter id (generally 0)
         * @param nbPlayers: the number of players
         */
        public GameLogic(int idStarter, int nbPlayers, int myUserId, Integer[] playersIds){
                System.out.println("Game started logic >>"+idStarter+ " "+nbPlayers+ " "+myUserId);
                System.out.println(playersIds);
                this.playerId = idStarter;
                this.nbPlayers = nbPlayers;
                this.roundNb = -1; // initial first round
                this.deck = new Deck(nbPlayers); // initialise deck
                this.playersStates = new ArrayList<>(); // initialise the players' states (alive, dead, time to play, ...)
                createHands();
                INSTANCE = this;
                System.out.println("OKOKKKOK");
                mapPlayersIds = new HashMap<>();
                mapPlayersIds.put(myUserId, idStarter); // save user id in hashmap
                listPlayerdsIds = new ArrayList<>();
                listPlayerdsIds.add(myUserId); // save user id in list
                for(int i = 0; i < nbPlayers-1; i++){ // -1 because we do not take the player 0 into loop account
                        System.out.println("CCCC");
                        mapPlayersIds.put(playersIds[i], i+1); // save other players ids in hashmap
                        listPlayerdsIds.add(playersIds[i]); // save other players in list
                }
                System.out.println("\n###### GAME LOGIC CREATION OK \n######");
        }

        /**
         * Constructor for other players so they can create their GameLogic object
         * @param playerId: the given player id
         * @param deck: the given deck
         * @param playersCards: the given cards
         *                    //TODO PARAMS
         */
        public GameLogic(int playerId, List<Integer> deck, List<List<Integer>> playersCards,List<Integer> playersStates, int nbPlayers, List<Integer> playersIdsList){
                this.playerId = playerId;
                this.deck = new Deck(deck);
                this.playersCards = playersCards;
                this.playersStates = playersStates;
                this.roundNb = 0;
                this.nbPlayers = nbPlayers;
                System.out.println("NB PLAYERS >>>>:"+nbPlayers);
                this.mapPlayersIds = new HashMap<>();
                for(int i = 0; i < nbPlayers; i++){
                        this.mapPlayersIds.put(playersIdsList.get(i),i); // complete the hashmap
                }
                INSTANCE = this; // make the GameLogic class callable everywhere
        }

        public void mainActivityIsReady(MainActivity mainActivity){
                this.mainActivity = mainActivity;
        }

        /**
         * Create hands for each player and prepare the deck for the game
         */
        public void createHands(){
                this.playersCards = new ArrayList<>(nbPlayers); // create a list to save cards
                for(int i = 0; i < nbPlayers; i++){
                        this.playersCards.add(new ArrayList<>(TOTAL_HAND_CARDS)); // for each player, initialise his cards list
                        //TODO debug
                        //for(int j = 0; j < TOTAL_HAND_CARDS-1; j++){
                        //        this.playersCards.get(i).add(this.deck.drawCard().getId()); // add a random card to player's cards
                        //}
                        //this.playersCards.get(i).add(this.deck.takeCard(Card.CardType.defuse).getId()); // add a defuse card (rules)
                        this.playersCards.get(i).add(0); // nope
                        this.playersCards.get(i).add(1); // attack
                        this.playersCards.get(i).add(3); // defuse
                        this.playersCards.get(i).add(6); // cat
                        this.playersCards.get(i).add(11); // future
                        this.playersCards.get(i).add(16); // favor
                        this.playersCards.get(i).add(17); // skip
                        this.playersCards.get(i).add(20); // shuffle

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
                return (this.playersStates.get(this.playerId) == PLAY) || (this.playersStates.get(this.playerId) == RESPONSE);
        }

        /**
         * Play a card
         * @param card the card to play
         * @param from the source player id
         * @param to the destination player id
         */
        public String playCard(Card card, int from, int to){
                System.out.println("Sending "+card.getId()+ " "+from+" "+to);
                if(this.playersStates.get(from) == WAIT){
                        return "This is not your turn !";
                }
                else if(this.playersStates.get(from) == DEAD){
                        return "You are dead !";
                }
                else if(this.playersStates.get(from) == PENDING){
                        return "Pending for an answer, please wait !";
                }
                else if(this.playersStates.get(from) == RESPONSE){ // current player response from a card
                        System.out.println("favor card response called");
                        return favorCardResponse(card, from, mustRespondToPlayerId);
                }
                else { // alive and his turn, treat the card
                        removeCardFromPlayerHand(this.playerId, card.getId()); // remove card from player's hand
                        if (card.getType() == Card.CardType.shuffle) {
                                return shuffleCard(card, from, to);
                        } else if (card.getType() == Card.CardType.future) {
                                return futureCard(card, from, to);
                        } else if (card.getType() == Card.CardType.favor) {
                                if (to == -1 || this.playersStates.get(to) != WAIT) { // if the player is not (anymore) in the game
                                        return "Not a valid player";
                                }
                                return favorCard(card, from, to);
                        }
                        else if(card.getType() == Card.CardType.nope){
                                return nopeCard(card, from, to);
                        }
                        return "Error";
                }
        }

        private String shuffleCard(Card card, int from, int to){
                lastCardPlayed = card;
                this.deck.shuffleDeck(); // first shuffle the deck
                this.deck.addCardToDeck(card.getId()); // then add the card
                sendTuple(card.getId(), from, to, false, this.deck, null,-1, -1);
                printToast("The deck has been shuffled !",Toast.LENGTH_SHORT);
                return "ok";
        }

        private String futureCard(Card card, int from, int to){
                lastCardPlayed = card;
                String str = this.deck.get3FirstCards();
                System.out.println(str);
                printToast(str,Toast.LENGTH_LONG);
                sendTuple(card.getId(), from, to, false, null, null,-1, -1);
                return "ok";
        }

        private String favorCard(Card card, int from, int to){
                lastCardPlayed = card;
                this.playersStates.set(from, PENDING); // from is in pending
                this.playersStates.set(to, RESPONSE); // to must respond
                sendTuple(card.getId(),from,to,false,null,this.playersStates, -1, -1);
                this.mainActivity.updateTitle(); // update the title
                return "ok";
        }

        private String favorCardResponse(Card card, int from, int to){
                removeCardFromPlayerHand(from, card.getId()); // remove card from player's hand
                this.playersStates.set(from, WAIT); // from is wait
                this.playersStates.set(to, PLAY); // to is play
                sendTuple(lastCardPlayed.getId(), from, to, false, null, this.playersStates,card.getId(), -1); // respond to a favor card
                if(card.getType() == Card.CardType.nope){ // if nope card played, draw it on the deck
                        this.deck.addCardToDeck(card.getId());
                }
                this.mainActivity.updateView(); // update the view
                return "favor";
        }

        private String nopeCard(Card card, int from, int to){

        }

        /**
         * Handle incoming tuples from AT
         * @param cardId the card id
         * @param from the emitter
         * @param to the receiver
         */
        public void handleTuple(int cardId, int from, int to, int roundNb, boolean personal, List<Integer> deck, List<Integer> states, Integer favorCardId, Integer nopeCardId){
                System.out.println("HANDLING TUPLE "+this.playerId);
                System.out.println("cardid: "+cardId + "from "+  from + "to "+ to);
                setRoundNb(roundNb); // update the round number in the game logic too
                if(cardId == NEXTTURN){ // next turn
                        System.out.println("NEXT TURN CALLED >>>>"+ from+ " "+to);
                        handleNextRound(from, to, states);
                }
                else if(from == to && personal == true){ // player drawn a card
                        System.out.println("Player "+from+" drawn a card !");
                        this.playersCards.get(from).add(cardId);
                        this.deck.drawCard(); // draw a card from the deck (for player that drawn it)
                }
                else{
                        Card cardPlayed = this.deck.idToCard(cardId);
                        if(cardPlayed.getType() == Card.CardType.shuffle){
                                handleShuffle(cardId, from, deck);
                        }
                        else if(cardPlayed.getType() == Card.CardType.future){
                                handleFuture(cardId, from);
                        }
                        else if(cardPlayed.getType() == Card.CardType.favor){
                                handleFavor(cardId, from, to, states,favorCardId);
                        }
                }

                this.mainActivity.updateView();
        }

        /**
         * Handle shuffle card from another player
         * @param deck the shuffled deck
         */
        private void handleShuffle(int cardId, int from, List<Integer> deck){
                this.deck.setCards(this.deck.listToCards(deck));
                removeCardFromPlayerHand(from, cardId);
                printToast("Player "+from+" has shuffled the deck !",Toast.LENGTH_SHORT);
        }

        private void handleFuture(int cardId, int from){
                this.deck.addCardToDeck(cardId); // add card to deck
                removeCardFromPlayerHand(from, cardId); // remove card from hand
                printToast("Player "+from+" has seen the future !", Toast.LENGTH_SHORT); // print message
        }

        private void handleFavor(int cardId, int from, int to, List<Integer> states, Integer favorCardId){
                System.out.println("handle favor from: "+from+ " to: "+to);
                if(this.playersStates.get(to) == PENDING){ // i know that the player was pending for an answer
                        Card receivedCard = this.deck.idToCard(favorCardId);
                        if(receivedCard.getType() == Card.CardType.nope){ // if nope has been played, do not add card
                                this.deck.addCardToDeck(favorCardId);
                                this.playersStates = states;
                                removeCardFromPlayerHand(from, favorCardId);
                                printToast("Player "+from+" played a nope !",Toast.LENGTH_SHORT);
                        }
                        else{ // card has been received
                                lastCardPlayed = null; // remove last card from memory
                                System.out.println("YES I WAS WAITING FOR PENDING");
                                this.playersCards.get(to).add(favorCardId); // add the card to player's hand
                                removeCardFromPlayerHand(from, favorCardId); // remove card from player's hand
                                this.playersStates = states;
                        }
                }
                else{
                        mustRespondToPlayerId = from;
                        this.deck.addCardToDeck(cardId); // add card to deck
                        this.playersStates = states; // update states
                        lastCardPlayed = this.deck.idToCard(cardId); // save the card to be send to pending player
                        printToast("Player "+from+" wants a favor from player "+to, Toast.LENGTH_SHORT);
                }
        }

        /**
         * Handle next round call tuple
         */
        private void handleNextRound(int from, int to, List<Integer> states){
                System.out.println(to+ " turn !");
                this.playersStates = states;
                System.out.println(states);
                this.mainActivity.updateView();
        }

        /**
         * AT call, tells us that a player has been disconnected
         * @param id the player's id
         */
        public void playerDisconnected(int id){
                printToast("Player "+this.mapPlayersIds.get(id)+" has disconnected !",Toast.LENGTH_LONG);
        }

        /**
         * After 30 seconds disconnected, the player is removed from the game
         * @param id the player's id
         */
        public void removePlayer(int id){
                printToast("Player "+this.mapPlayersIds.get(id)+" will never come back", Toast.LENGTH_SHORT);
                nbPlayers = nbPlayers - 1;
                if(nbPlayers == 2){ // TODO CHANGE 1
                        //TODO win screen
                }
                else{
                      int playId = this.mapPlayersIds.get(id);
                      int nextPlayer = getNextPlayerAlive(playId);
                      //if(nextPlayer == playId){ // no other player ALIVE has been found
                      //        // TODO win screen
                      //}
                        this.playersStates.set(playId,DEAD); // set the disconnected player to dead
                        this.playersCards.set(playId,new ArrayList<>()); // empty his hand
                        mainActivity.removePlayer(playId);
                        // TODO next player's turn
                }
        }

        /**
         * Method to get the next player who's alive
         * @param id the current player id
         * @return the next player id
         */
        private int getNextPlayerAlive(int id){
                int nextPlayer = id;
                boolean hasBeenFound = false;
                while(!hasBeenFound){
                        nextPlayer += 1;
                        nextPlayer = nextPlayer % nbPlayers;
                        if(nextPlayer == 4){ // 4th player is 0 player
                                nextPlayer = 0;
                        }
                        else if(nextPlayer == id){
                                break; // no player has been found
                        }
                        try{
                                if(this.playersStates.get(nextPlayer) == WAIT){ // get the next player who's waiting
                                        hasBeenFound = true;
                                }
                        } catch (Exception e){ // no player at this position
                                break;
                        }
                }
                return nextPlayer;
        }

        //public void sendDataRequested()

        private void printToast(String message, int duration){
                this.mainActivity.printToast(message, duration);
        }

        /**
         * Remove a card from a player's hand
         * @param playId the player id
         * @param cardId the card id
         */
        private void removeCardFromPlayerHand(int playId, int cardId){
                List<Integer> list = this.playersCards.get(playId);
                for(int i = 0; i < this.playersCards.get(playId).size(); i++){
                        if(this.playersCards.get(playId).get(i) == cardId){
                                list.remove(i);
                        }
                }
                this.playersCards.set(playId, list);
        }

        /**
         * Add a received card to the deck
         * @param cardId the card id that has been received
         */
        private void addCardToDeck(int cardId){
                this.deck.addCardToDeck(cardId);

        }

        /**
         * End player's turn, draw a card.
         * If exploding kitten, force the player to play a defuse card.
         * If there is no defuse card, player is dead.
         */
        public void endPlayerTurn(){
                Card drawnCard = this.deck.drawCard();
                if((drawnCard.getType() == Card.CardType.exploding)){ // if exploding card, check if player has defuse
                        this.deck.addCardToDeck(drawnCard.getId()); // add exploding card to deck

                        int hasDefuse = checkPlayerHasDefuse(this.playerId); // check if player has defuse card
                        if(hasDefuse == -1){ // if no defuse, player is dead
                                this.playersStates.add(playerId,DEAD);
                                printToast("BOOOM",Toast.LENGTH_SHORT);
                                // TODO send boom + add cards to deck (oublie pas que les cartes sont ajoutées à la fin du deck donc no soucis)
                        }
                        else{
                                this.playersCards.get(this.playerId).remove(hasDefuse); // remove defuse card from player's hand
                                this.deck.addCardToDeck(hasDefuse); // add card to deck played by player
                                // TODO
                        }
                }
                this.playersCards.get(this.playerId).add(drawnCard.getId()); // add drawn card
                System.out.println("NEW HANDS: >>>>"+this.playersCards.get(this.playerId));
                sendTuple(drawnCard.getId(), this.playerId, this.playerId, true, null, null,-1, -1);
                this.playersStates.set(this.playerId, WAIT); // player is now in waiting state
                int nextPlayer = getNextPlayerAlive(this.playerId); // TODO CHECK IF STILL PLAYERS
                this.playersStates.set(nextPlayer, PLAY);
                sendTuple(NEXTTURN, this.playerId, nextPlayer, false, null, this.playersStates,-1, -1);
                this.mainActivity.updateView();
                // TODO bug: update de la pile déconne lors d'un skip
        }

        /**
         * Check if player has a defuse card in his hand
         * @param playerId the player id
         * @return a boolean
         */
        private int checkPlayerHasDefuse(int playerId){
                int hasDefuse = -1;
                for(Integer cardId : this.playersCards.get(playerId)){
                        Card card = this.deck.idToCard(cardId);
                        if(card.getType() == Card.CardType.defuse){
                                hasDefuse = card.getId();
                                break;
                        }
                }
                return hasDefuse;
        }

        private void sendTuple(int cardId, int from, int to, boolean personal, Deck deck, List<Integer> states, Integer favorCardId, Integer nopeCardId){
                setRoundNb(getRoundNb()+1);
                if(deck == null){
                        this.atws.sendTuple(cardId, from, to, getRoundNb(), personal, null, states, favorCardId, nopeCardId);
                }
                else{
                        this.atws.sendTuple(cardId, from, to, getRoundNb(), personal, deck.deckToList(), states, favorCardId, nopeCardId);
                }

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

        public HashMap<Integer, Integer> getMapPlayersIds() {
                return mapPlayersIds;
        }

        public List<Integer> getListPlayerdsIds(){
                return this.listPlayerdsIds;
        }


}
