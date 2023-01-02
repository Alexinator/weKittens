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
    private Button btnPlayerTop, btnPlayerLeft, btnPlayerRight, skipButton;
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
    private boolean favorCardUsed = false;

    private Card lastCard;

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
        this.gameLogic.mainActivityIsReady(this);

        this.playersCards = gameLogic.getPlayersCards(); // get players cards
        this.cardDeck = gameLogic.getDeck(); // retrieve the deck
        this.playerId = gameLogic.getPlayerId(); //retrieve player id
        this.myCards = this.cardDeck.listToCardsList(this.playersCards.get(this.playerId)); //retrieve player cards
        System.out.println("Player ["+this.playerId+"] cards: "+this.myCards);

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

        // setup other player hands + board placement based on the player's id
        if(playerCount == 2){
            if(this.playerId == 0){
                this.leftPlayer = 1;
                drawingview.setLeftPlayerCount(this.playersCards.get(1).size());
                drawingview.setTopPlayerCount(0); // no player
                drawingview.setRightPlayerCount(0); // no player
            }
            else if(this.playerId == 1){
                this.rightPlayer = 0;
                drawingview.setLeftPlayerCount(0); // no player
                drawingview.setTopPlayerCount(0); // no player
                drawingview.setRightPlayerCount(this.playersCards.get(0).size());
            }
        }
        else if(playerCount == 3){
            if(this.playerId == 0){
                this.leftPlayer = 1;
                this.topPlayer = 2;
                drawingview.setLeftPlayerCount(this.playersCards.get(1).size());
                drawingview.setTopPlayerCount(this.playersCards.get(2).size());
                drawingview.setRightPlayerCount(0); // no player
            }
            else if(this.playerId == 1){
                this.leftPlayer = 2;
                this.rightPlayer = 0;
                drawingview.setLeftPlayerCount(this.playersCards.get(2).size());
                drawingview.setTopPlayerCount(0); // no player
                drawingview.setRightPlayerCount(this.playersCards.get(0).size());
            }
            else if(this.playerId == 2) {
                this.rightPlayer = 1;
                this.topPlayer = 0;
                drawingview.setLeftPlayerCount(0); // no player
                drawingview.setTopPlayerCount(this.playersCards.get(0).size());
                drawingview.setRightPlayerCount(this.playersCards.get(1).size());
            }
        }
        else{ // 4 players
            if(this.playerId == 0){
                this.leftPlayer = 1;
                this.topPlayer = 2;
                this.rightPlayer = 3;
                drawingview.setLeftPlayerCount(this.playersCards.get(1).size());
                drawingview.setTopPlayerCount(this.playersCards.get(2).size());
                drawingview.setRightPlayerCount(this.playersCards.get(3).size());
            }
            else if(this.playerId == 1){
                this.leftPlayer = 2;
                this.topPlayer = 3;
                this.rightPlayer = 0;
                drawingview.setLeftPlayerCount(this.playersCards.get(2).size());
                drawingview.setTopPlayerCount(this.playersCards.get(3).size());
                drawingview.setRightPlayerCount(this.playersCards.get(0).size());
            }
            else if(this.playerId == 2){
                this.leftPlayer = 3;
                this.topPlayer = 0;
                this.rightPlayer = 1;
                drawingview.setLeftPlayerCount(this.playersCards.get(3).size());
                drawingview.setTopPlayerCount(this.playersCards.get(0).size());
                drawingview.setRightPlayerCount(this.playersCards.get(1).size());
            }
            else if(this.playerId == 3){
                this.leftPlayer = 0;
                this.topPlayer = 1;
                this.rightPlayer = 2;
                drawingview.setLeftPlayerCount(this.playersCards.get(0).size());
                drawingview.setTopPlayerCount(this.playersCards.get(1).size());
                drawingview.setRightPlayerCount(this.playersCards.get(2).size());
            }
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
        skipButton = findViewById(R.id.skip_button); // skip button


        // TODO: currently clicking on a player stack shows the explosion (boom) animation, but it should beh changed to be shown whenever a player dies
        btnPlayerTop.setOnClickListener(v -> { actionOnPlayer(this.topPlayer); });
        btnPlayerLeft.setOnClickListener(v -> { actionOnPlayer(this.leftPlayer);});
        btnPlayerRight.setOnClickListener(v -> { actionOnPlayer(this.rightPlayer); });
        skipButton.setOnClickListener(v -> {skipButtonPressed();});


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

        if(favorCardUsed){
            printToast("You have to choose a player from last card before playing another one !",Toast.LENGTH_SHORT);
            return false;
        }
        else if(card.getType() == Card.CardType.favor){
            favorCardUsed = true; // TODO HANDLE NOT USE ANOTHER CARD DURING THAT TIME
            printToast("Choose a player to steal !",Toast.LENGTH_LONG);
        }
        else if(!favorCardUsed){ // no waiting action from front
            //boolean canPlayThisCard =
            String returnMessage = this.gameLogic.playCard(card,this.playerId, 1); //TODO hardcoded
            System.out.println("return message: "+returnMessage);
            System.out.println(returnMessage.equals("ok"));
            if(!returnMessage.equals("ok")){
                printToast(returnMessage, Toast.LENGTH_SHORT);
                return false;
            }
        }


        //TODO: don't do this if card is not valid, maybe show a toast indicating that the move is invalid and return false
        drawingview.playCard(card);
        drawingview.invalidate();
        lastCard = card;
        return true;
    }

    private void actionOnPlayer(int playId){
        if(favorCardUsed){ //TODO le reste
            String returnMessage = this.gameLogic.playCard(lastCard, this.playerId, playId);
            if(!returnMessage.equals("ok")){
                // TODO do something
                return;
            }
            favorCardUsed = false; // todo check if we can use on this player
        }
    }

    /**
     * Update the view when cards have been played by other players
     */
    public void updateView(){
        System.out.println("ok j'update la view");
        this.cardDeck = this.gameLogic.getDeck(); // retrieve the new deck
        //this.myCards = this.cardDeck.listToCardsList(this.playersCards.get(this.playerId)); // retrieve my cards
        this.playersCards = this.gameLogic.getPlayersCards(); // update players cards
        this.myCards = this.cardDeck.listToCardsList(this.playersCards.get(this.playerId)); // update player hand cards
        adapter.resetCards(this.myCards); // reset hand cards on view
        updateTitle();
        // update other players' cards

        drawingview = findViewById(R.id.drawingview);
        drawingview.playCard(this.cardDeck.peekTopCard());
        drawingview.invalidate();
    }

    /**
     * Remove a player after a disconnection
     * @param playId the player's id
     */
    public void removePlayer(int playId){
        if(this.playerId == 0){
            if(playId == 1){
                setLeftPlayerCardCount(0);
                startExplosionAnimation(animExplosionLeft);
            }
            else if(playId == 2){
                setTopPlayerCardCount(0);
                startExplosionAnimation(animExplosionTop);
            }
            else if(playId == 3){
                setRightPlayerCardCount(0);
                startExplosionAnimation(animExplosionRight);
            }
        }
        else if(this.playerId == 1){
            if(playId == 0){
                setRightPlayerCardCount(0);
                startExplosionAnimation(animExplosionRight);
            }
            else if(playId == 2){
                setLeftPlayerCardCount(0);
                startExplosionAnimation(animExplosionLeft);
            }
            else if(playId == 3){
                setTopPlayerCardCount(0);
                startExplosionAnimation(animExplosionTop);
            }
        }
        else if(this.playerId == 2){
            if(playId == 0){
                setTopPlayerCardCount(0);
                startExplosionAnimation(animExplosionTop);
            }
            else if(playId == 1){
                setRightPlayerCardCount(0);
                startExplosionAnimation(animExplosionRight);
            }
            else if(playId == 3){
                setLeftPlayerCardCount(0);
                startExplosionAnimation(animExplosionLeft);
            }
        }
        else if(this.playerId == 3){
            if(playId == 0){
                setLeftPlayerCardCount(0);
                startExplosionAnimation(animExplosionLeft);
            }
            else if(playId == 1){
                setTopPlayerCardCount(0);
                startExplosionAnimation(animExplosionTop);
            }
            else if(playId == 2){
                setRightPlayerCardCount(0);
                startExplosionAnimation(animExplosionRight);
            }
        }
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
     * Skip button has been pressed by the player, end of his turn
     */
    private void skipButtonPressed(){
        this.gameLogic.endPlayerTurn();
    }

    /**
     * Print Toast
     * @param message: the message
     * @param duration: message's duration
     */
    public void printToast(String message, int duration){
        Toast.makeText(getApplicationContext(),message,duration).show();
    }


}
