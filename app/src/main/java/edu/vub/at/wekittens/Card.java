package edu.vub.at.wekittens;

import java.lang.reflect.Field;

public class Card {

    public static enum CardType {
        back, //just the back side
        exploding,
        nope,
        defuse,
        attack,
        shuffle,
        favor,
        skip,
        future,
        cat
    }


    private CardType type;
    private String variant;
    private int cardId; // use id for easier AT

    private int id = 0;

    // added -1 in DrawingView for card creation
    public Card(CardType type, String variant, int cardId) {
        this.type = type;
        this.variant = variant;
        this.cardId = cardId;
    }

    public int getId(){
        return this.cardId;
    }

    public Card(Card c) {
        this.type = c.getType();
        this.variant = c.getVariant();
    }

    public int getResourceId() {
        if (id != 0)
            return id;

        try {
            Class d = R.drawable.class;
            Field en = d.getDeclaredField(this.toString());
            id = en.getInt(null);

            return id;
        } catch (Exception e) {
            return R.drawable.back_card;
        }
    }

    @Override
    public String toString() {
        return type.toString() + "_" + variant;
    }

    public CardType getType() {
        return this.type;
    }
    public String getVariant() {
        return this.variant;
    }
}
