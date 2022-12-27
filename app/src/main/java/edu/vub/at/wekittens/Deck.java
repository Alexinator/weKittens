package edu.vub.at.wekittens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import edu.vub.at.wekittens.Card.CardType;

public class Deck {

    private LinkedList<Card> cards;
    private int playerCount;
    private HashMap<Integer,String> mapIdToVariant;
    private HashMap<Integer,Card.CardType> mapIdToType;


    public Deck(int players) {
        this.playerCount = players;
        prepareMaps();
        makeSharingDeck();
    }

    /**
     * Create a deck based on a list of cards ids
     * @param list: the list of cards ids
     */
    public Deck(List<Integer> list){
        System.out.println("CREATE NEW DECK>>"+list);
        prepareMaps();
        this.cards = listToCards(list);
    }

    /**
     * Deck to integer list transformation
     * @return a list of cards id
     */
    public List<Integer> deckToList(){
        List<Integer> list = new ArrayList<>();
        for(Card c : this.cards){
            list.add(c.getId());
        }
        return list;
    }

    /**
     * Return a list of cards id to a LinkedList
     * @param list: the list of cards id
     * @return a LinkedList of cards
     */
    public LinkedList<Card> listToCards(List<Integer> list){
        LinkedList<Card> toReturn = new LinkedList<>();
        for(int i = 0; i < list.size(); i++){
            int cardId = list.get(i);
            toReturn.add(new Card(mapIdToType.get(cardId), mapIdToVariant.get(cardId),cardId));
        }
        return toReturn;
    }

    /**
     * Return a list of cards id to a List
     * @param list: the list of cards id
     * @return a List of cards
     */
    public List<Card> listToCardsList(List<Integer> list){
        List<Card> toReturn = new LinkedList<>();
        for(int i = 0; i < list.size(); i++){
            int cardId = list.get(i);
            toReturn.add(new Card(mapIdToType.get(cardId), mapIdToVariant.get(cardId),cardId));
        }
        return toReturn;
    }

    public Card peekTopCard() {
        final Iterator<Card> itr = cards.iterator();
        Card lastElement = itr.next();
        while(itr.hasNext()) {
            lastElement = itr.next();
        }
        return lastElement;
    }

    public Card drawCard() {
        return cards.poll();
    }

    public Card takeCard(CardType type) {
        // assumes the card exists...
        int pos = 0;
        Card card;

        while ((card = cards.get(pos)).getType() != type) {
            pos++;
        }
        cards.remove(pos);

        return card;
    }

    private static void addCard(LinkedList<Card> deck, CardType card, String variant, int count, int id) {
        for (int i=0; i<count; i++)
            deck.add(new Card(card, variant, id));
    }

    private static void addCard(LinkedList<Card> deck, CardType card, String variant, int count, int current, int max, int id) {
        if (current < max)
            addCard(deck, card, variant, count, id);
    }

        // deck without exploding kittens
    private void makeSharingDeck() {
        LinkedList<Card> deck = new LinkedList<>();
        //Nope
        addCard(deck, CardType.nope, "card", 5, 0);

        //Attack
        addCard(deck, CardType.attack, "mine", 2, 1);
        addCard(deck, CardType.attack, "space", 2,2);

        //Defuse
        addCard(deck, CardType.defuse, "banjo", 2,3);
        addCard(deck, CardType.defuse, "catnip", 2,4);
        addCard(deck, CardType.defuse, "laser", 2,5);

        //Cats
        addCard(deck, CardType.cat, "beard", 4,6);
        addCard(deck, CardType.cat, "potato", 4,7);
        addCard(deck, CardType.cat, "taco", 4,8);
        addCard(deck, CardType.cat, "rainbow", 4,9);
        addCard(deck, CardType.cat, "cattermelon", 4,10);

        //See the future
        addCard(deck, CardType.future, "bear", 1,11);
        addCard(deck, CardType.future, "goggles", 1,12);
        addCard(deck, CardType.future, "mantis", 1,13);
        addCard(deck, CardType.future, "pig", 1,14);
        addCard(deck, CardType.future, "pigacorn", 1,15);

        //Favor
        addCard(deck, CardType.favor, "card", 4,16);

        //Skip
        addCard(deck, CardType.skip, "nap", 2,17);
        addCard(deck, CardType.skip, "sprint", 1,18);
        addCard(deck, CardType.skip, "bunnyraptor", 1,19);

        //Shuffle
        addCard(deck, CardType.shuffle, "litterbox", 2,20);
        addCard(deck, CardType.shuffle, "scratch", 2,21);

        Collections.shuffle(deck);

        this.cards = deck;
    }

    public void addExplodingKittens() {
        //int explodingCount = 0;

        //for (int i=0; i<2; i++) {
        //    addCard(this.cards, CardType.exploding, "a", 1, explodingCount++, playerCount - 1);
        //    addCard(this.cards, CardType.exploding, "b", 1, explodingCount++, playerCount - 1);
        //}
        // the code above is not used in my implementation
        addCard(this.cards, CardType.exploding,"a",playerCount-1, 22);
        Collections.shuffle(this.cards);
    }

    /**
     * Prepare the map to easily retrieve the card variant from id
     */
    private void prepareMaps(){
        this.mapIdToVariant = new HashMap<>();
        this.mapIdToType = new HashMap<>();
        //Error
        this.mapIdToVariant.put(-1,"error"); //if card id is -1, we have an error
        //this.mapIdToType.put(-1,null); //error

        //Nope
        this.mapIdToVariant.put(0,"card");
        this.mapIdToType.put(0,CardType.nope);

        //Attack
        this.mapIdToVariant.put(1,"mine");
        this.mapIdToVariant.put(2,"space");
        this.mapIdToType.put(1,CardType.attack);
        this.mapIdToType.put(2,CardType.attack);

        //Defuse
        this.mapIdToVariant.put(3,"banjo");
        this.mapIdToVariant.put(4,"catnip");
        this.mapIdToVariant.put(5,"laser");
        this.mapIdToType.put(3,CardType.defuse);
        this.mapIdToType.put(4,CardType.defuse);
        this.mapIdToType.put(5,CardType.defuse);

        //Cats
        this.mapIdToVariant.put(6,"beard");
        this.mapIdToVariant.put(7,"potato");
        this.mapIdToVariant.put(8,"taco");
        this.mapIdToVariant.put(9,"rainbow");
        this.mapIdToVariant.put(10,"cattermelon");
        this.mapIdToType.put(6,CardType.cat);
        this.mapIdToType.put(7,CardType.cat);
        this.mapIdToType.put(8,CardType.cat);
        this.mapIdToType.put(9,CardType.cat);
        this.mapIdToType.put(10,CardType.cat);

        //See the future
        this.mapIdToVariant.put(11,"bear");
        this.mapIdToVariant.put(12,"goggles");
        this.mapIdToVariant.put(13,"mantis");
        this.mapIdToVariant.put(14,"pig");
        this.mapIdToVariant.put(15,"pigacorn");
        this.mapIdToType.put(11,CardType.future);
        this.mapIdToType.put(12,CardType.future);
        this.mapIdToType.put(13,CardType.future);
        this.mapIdToType.put(14,CardType.future);
        this.mapIdToType.put(15,CardType.future);

        //Favor
        this.mapIdToVariant.put(16,"card");
        this.mapIdToType.put(16,CardType.favor);

        //Skip
        this.mapIdToVariant.put(17,"nap");
        this.mapIdToVariant.put(18,"sprint");
        this.mapIdToVariant.put(19,"bunnyraptor");
        this.mapIdToType.put(17,CardType.skip);
        this.mapIdToType.put(18,CardType.skip);
        this.mapIdToType.put(19,CardType.skip);

        //Shuffle
        this.mapIdToVariant.put(20,"litterbox");
        this.mapIdToVariant.put(21,"scratch");
        this.mapIdToType.put(20,CardType.shuffle);
        this.mapIdToType.put(21,CardType.shuffle);

        //Exploding
        this.mapIdToVariant.put(22,"a"); //exploding kitten
        this.mapIdToVariant.put(23,"b"); //exploding kitten
        this.mapIdToType.put(22,CardType.exploding);
        this.mapIdToType.put(23,CardType.exploding);
    }

}
