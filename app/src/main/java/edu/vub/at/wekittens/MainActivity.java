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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import edu.vub.at.android.util.IATAndroid;

public class MainActivity extends AppCompatActivity implements HandAction {

    private CardViewAdapter adapter;

    private DrawingView drawingview;
    private Deck cardDeck;
    private TextView txtExplosion;
    private Button btnPlayerTop, btnPlayerLeft, btnPlayerRight;
    private Animation animExplosionTop, animExplosionBottom, animExplosionLeft, animExplosionRight;

    // added attributes
    private GameLogic gameLogic;
    public static MainActivity INSTANCE;
    private int playerId;
    private List<List<Integer>> playersCards;
    private List<Card> myCards;
    private int playerCount = 0;
    private int leftPlayer = -1;
    private int topPlayer = -1;
    private int rightPlayer = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        INSTANCE = this;

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("bundle");
        ArrayList<String> players = (ArrayList<String>) bundle.getSerializable("players");
        Boolean hasStartedTheGame = (Boolean) bundle.getBoolean("hasStartedTheGame");
        this.playerCount = players.size();
        this.gameLogic = GameLogic.INSTANCE;

        this.playersCards = gameLogic.getPlayersCards(); // get players cards
        this.cardDeck = gameLogic.getDeck(); // retrieve the deck
        this.playerId = gameLogic.getPlayerId(); //retrieve player id
        this.myCards = this.cardDeck.listToCardsList(this.playersCards.get(this.playerId)); //retrieve player cards
        System.out.println("Player ["+this.playerId+"] cards: "+this.myCards);

        // setup players placement on the board
        for(int i = 0; i < playerCount; i++){
            if(i != this.playerId){
                if(leftPlayer == -1){
                    this.leftPlayer = i;
                }
                else if(topPlayer == -1){
                    this.topPlayer = i;
                }
                else{
                    rightPlayer = i;
                }
            }
        }

        updateTitle();

        // set up hand stack
        List<Card> cards = this.myCards;

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
        drawingview.playCard(cardDeck.peekTopCard());

        // setup other player hands
        if(playerCount == 2){
            drawingview.setLeftPlayerCount(this.playersCards.get(leftPlayer).size());
            drawingview.setTopPlayerCount(0); // no player
            drawingview.setRightPlayerCount(0); // no player
        }
        else if(playerCount == 3){
            drawingview.setLeftPlayerCount(this.playersCards.get(leftPlayer).size());
            drawingview.setTopPlayerCount(this.playersCards.get(topPlayer).size());
            drawingview.setRightPlayerCount(0); // no player
        }
        else{
            drawingview.setLeftPlayerCount(this.playersCards.get(leftPlayer).size());
            drawingview.setTopPlayerCount(this.playersCards.get(topPlayer).size());
            drawingview.setRightPlayerCount(this.playersCards.get(rightPlayer).size());
        }

        // create new card deck
        System.out.println("NB PLAYERS >>>>"+ playerCount);
        //cardDeck = new Deck(playerCount); //TODO: do this when a new round has started, pass the number of players

        // TODO: you may have to create a way to sync decks between devices when a round has started
        // Try to modify the deck class so that you can easily serialize and deserialize a decks content
        // in order to share it with AmbientTalk and the rest of the connected devices through AmbientTalk


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
        //cards.add(cardDeck.takeCard(Card.CardType.defuse)); // every player takes at least one defuse

        //for (int i=0; i<playerCount-1; i++)
        //    cardDeck.takeCard(Card.CardType.defuse);  // simulate removing the defuse cards for the other players

        //drawCards(7);
        //cardDeck.addExplodingKittens();

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
        System.out.println("OK PRINT CARD CALLED");
        //boolean canPlayThisCard =
        String returnMessage = this.gameLogic.playCard(card,this.playerId, 1); //TODO hardcoded
        System.out.println("return message: "+returnMessage);
        System.out.println(returnMessage.equals("ok"));
        if(!returnMessage.equals("ok")){
            printToast(returnMessage, Toast.LENGTH_SHORT);
            return false;
        }

        //TODO: don't do this if card is not valid, maybe show a toast indicating that the move is invalid and return false
        drawingview.playCard(card);
        drawingview.invalidate();
        return true;
    }

    /**
     * Update the view when cards have been played by other players
     */
    public void updateView(){
        System.out.println("ok j'update la view");
        this.cardDeck = this.gameLogic.getDeck(); // retrieve the new deck
        this.myCards = this.cardDeck.listToCardsList(this.playersCards.get(this.playerId)); // retrieve my cards
        // update other players' cards

        drawingview = findViewById(R.id.drawingview);
        drawingview.playCard(this.cardDeck.peekTopCard());
        drawingview.invalidate();
    }

    /**
     * Update title to let the player know if this is their turn or not
     */
    private void updateTitle(){
        if (this.gameLogic.getPlayersStates().get(this.playerId) == this.gameLogic.PLAY) {
            setTitle("Your turn");
        } else if(this.gameLogic.getPlayersStates().get(this.playerId) == this.gameLogic.WAIT) {
            setTitle("Not your turn");
        }
        else if(this.gameLogic.getPlayersStates().get(this.playerId) == this.gameLogic.DEAD){
            setTitle("You are dead");
        }
    }

    /**
     * Print Toast
     * @param message: the message
     * @param duration: message's duration
     */
    private void printToast(String message, int duration){
        Toast.makeText(getApplicationContext(),message,duration).show();
    }


}
