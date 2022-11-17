package edu.vub.at.wekittens;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import edu.vub.at.wekittens.Card.CardType;

public class Deck {

    private LinkedList<Card> cards;
    private int playerCount;

    public Deck(int players) {
        this.playerCount = players;

        makeSharingDeck();
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

    private static void addCard(LinkedList<Card> deck, CardType card, String variant, int count) {
        for (int i=0; i<count; i++)
            deck.add(new Card(card, variant));
    }

    private static void addCard(LinkedList<Card> deck, CardType card, String variant, int count, int current, int max) {
        if (current < max)
            addCard(deck, card, variant, count);
    }

        // deck without exploding kittens
    private void makeSharingDeck() {
        LinkedList<Card> deck = new LinkedList<>();
        //Nope
        addCard(deck, CardType.nope, "card", 5);

        //Attack
        addCard(deck, CardType.attack, "mine", 2);
        addCard(deck, CardType.attack, "space", 2);

        //Defuse
        addCard(deck, CardType.defuse, "banjo", 2);
        addCard(deck, CardType.defuse, "catnip", 2);
        addCard(deck, CardType.defuse, "laser", 2);

        //Cats
        addCard(deck, CardType.cat, "beard", 4);
        addCard(deck, CardType.cat, "potato", 4);
        addCard(deck, CardType.cat, "taco", 4);
        addCard(deck, CardType.cat, "rainbow", 4);
        addCard(deck, CardType.cat, "cattermelon", 4);

        //See the future
        addCard(deck, CardType.future, "bear", 1);
        addCard(deck, CardType.future, "goggles", 1);
        addCard(deck, CardType.future, "mantis", 1);
        addCard(deck, CardType.future, "pig", 1);
        addCard(deck, CardType.future, "pigacorn", 1);

        //Favor
        addCard(deck, CardType.favor, "card", 4);

        //Skip
        addCard(deck, CardType.skip, "nap", 2);
        addCard(deck, CardType.skip, "sprint", 1);
        addCard(deck, CardType.skip, "bunnyraptor", 1);

        //Shuffle
        addCard(deck, CardType.shuffle, "litterbox", 2);
        addCard(deck, CardType.shuffle, "scratch", 2);

        Collections.shuffle(deck);

        this.cards = deck;
    }

    public void addExplodingKittens() {
        int explodingCount = 0;

        for (int i=0; i<2; i++) {
            addCard(this.cards, CardType.exploding, "a", 1, explodingCount++, playerCount - 1);
            addCard(this.cards, CardType.exploding, "b", 1, explodingCount++, playerCount - 1);
        }

        Collections.shuffle(this.cards);
    }

}
