package edu.vub.at.wekittens;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import edu.vub.at.android.util.IATAndroid;

public class MainActivity extends AppCompatActivity implements HandAction {

    private CardViewAdapter adapter;

    private DrawingView drawingview;
    private Deck cardDeck;
    private TextView txtExplosion;
    private Button btnPlayerTop, btnPlayerLeft, btnPlayerRight;
    private Animation animExplosionTop, animExplosionBottom, animExplosionLeft, animExplosionRight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("bundle");
        ArrayList<String> players = (ArrayList<String>) bundle.getSerializable("players");

        // create new card deck
        int playerCount = players.size();
        cardDeck = new Deck(playerCount); //TODO: do this when a new round has started, pass the number of players

        // TODO: you may have to create a way to sync decks between devices when a round has started
        // Try to modify the deck class so that you can easily serialize and deserialize a decks content
        // in order to share it with AmbientTalk and the rest of the connected devices through AmbientTalk

        // set up hand stack
        ArrayList<Card> cards = new ArrayList<>();

        RecyclerView handView = findViewById(R.id.playerhand);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(MainActivity.this,
                LinearLayoutManager.HORIZONTAL, false);

        handView.setLayoutManager(horizontalLayoutManager);
        adapter = new CardViewAdapter(this, cards, this);
        handView.setAdapter(adapter);

        MoveAndPlaceHelper mh = new MoveAndPlaceHelper(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(mh);
        touchHelper.attachToRecyclerView(handView);

        // setup top card
        drawingview = findViewById(R.id.drawingview);
        //drawingview.playCard(cardDeck.peekTopCard());

        // setup other player hands
        drawingview.setLeftPlayerCount(3); //TODO: set initially to 0
        drawingview.setTopPlayerCount(3);
        drawingview.setRightPlayerCount(3);

        // setup animations
        animExplosionTop    = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.explosion_top);
        animExplosionBottom = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.explosion_bottom);
        animExplosionLeft   = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.explosion_left);
        animExplosionRight  = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.explosion_right);

        // reference the player buttons and text views
        txtExplosion = findViewById(R.id.txtExplosion);
        btnPlayerTop   = findViewById(R.id.btnPlayerTop);
        btnPlayerLeft  = findViewById(R.id.btnPlayerLeft);
        btnPlayerRight = findViewById(R.id.btnPlayerRight);


        // TODO: currently clicking on a player stack shows the explosion (boom) animation, but it should beh changed to be shown whenever a player dies
        btnPlayerTop.setOnClickListener(v -> { startExplosionAnimation(animExplosionTop); });
        btnPlayerLeft.setOnClickListener(v -> { startExplosionAnimation(animExplosionLeft); });
        btnPlayerRight.setOnClickListener(v -> { startExplosionAnimation(animExplosionRight); });


        // TODO: do this when a round has started
        cards.add(cardDeck.takeCard(Card.CardType.defuse)); // every player takes at least one defuse

        for (int i=0; i<playerCount-1; i++)
            cardDeck.takeCard(Card.CardType.defuse);  // simulate removing the defuse cards for the other players

        drawCards(7);
        cardDeck.addExplodingKittens();

        Log.i("dd","d");
    }

    public void startExplosionAnimation(Animation animation){
        runOnUiThread(() -> {
            txtExplosion.setVisibility(View.VISIBLE);
            txtExplosion.startAnimation(animation);
        });
    }

    //TODO: call this whenever the player has to draw cards
    public void drawCards(int n) {
        runOnUiThread(() -> {
            for (int i = 0; i < n; i++)
                adapter.addCard(cardDeck.drawCard());
        });
    }

    //TODO: call these methods from AmbientTalk indicating that another player has died
    public void topExplosionAnimation() {
        startExplosionAnimation(animExplosionTop);
    }
    public void leftExplosionAnimation() {
        startExplosionAnimation(animExplosionLeft);
    }
    public void rightExplosionAnimation() {
        startExplosionAnimation(animExplosionRight);
    }

    //TODO: call these methods from AmbientTalk to set the number of cards for the other players
    public void setTopPlayerCardCount(int n) {
        runOnUiThread(() -> {
            drawingview.setTopPlayerCount(n);
            drawingview.invalidate();
        });
    }
    public void setLeftPlayerCardCount(int n) {
        runOnUiThread(() -> {
            drawingview.setLeftPlayerCount(n);
            drawingview.invalidate();
        });
    }
    public void setRightPlayerCardCount(int n) {
        runOnUiThread(() -> {
            drawingview.setRightPlayerCount(n);
            drawingview.invalidate();
        });
    }

    // this method is called when a user plays a card
    // you should check if this is valid, if not you shouldn't update the drawingview and return false
    @Override
    public boolean cardPlayed(Card card) {

        //TODO: don't do this if card is not valid, maybe show a toast indicating that the move is invalid and return false
        drawingview.playCard(card);
        drawingview.invalidate();

        return true;
    }


}
