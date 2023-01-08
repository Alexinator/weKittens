package edu.vub.at.wekittens;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.vub.at.IAT;
import edu.vub.at.android.util.IATAndroid;
import edu.vub.at.wekittens.interfaces.ATWeKittens;
import edu.vub.at.wekittens.interfaces.JWeKittens;


public class LobbyActivity extends AppCompatActivity implements JWeKittens {

    // AT attributes
    private static IAT iat;
    private static final int _ASSET_INSTALLER_ = 0;
    // static so we can easily use it in MainActivity
    public static ATWeKittens atws;

    private ArrayList<String> players; // players' names
    private ArrayList<Integer> playersId; // players' ids (useful for removal)
    private ListView listView;
    private ArrayAdapter<String> adapter;
    // allow us to know if the player has started the game
    private boolean hasStartedTheGame = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        // first start AT
        if (iat == null) {
            Intent i = new Intent(this, WeKittensAssetInstaller.class);
            //Bundle bundle = new Bundle();
            //bundle.putString("basedir",getExternalFilesDir(null).getAbsolutePath());
            //i.putExtras(bundle);
            startActivityForResult(i, _ASSET_INSTALLER_);
        }

        players = new ArrayList<>();
        playersId = new ArrayList<>();
        listView = (ListView)findViewById(R.id.players);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, players);
        listView.setAdapter(adapter);

        Button startGame = (Button)findViewById(R.id.startgame);
        startGame.setVisibility(View.INVISIBLE);
        startGame.setEnabled(false);


        Button findGameButton = (Button)findViewById(R.id.findgame);
        findGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView tv = (TextView)findViewById(R.id.playername);
                findGame(tv.getText().toString());
            }
        });

        startGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGame();
            }
        });
    }

    /**
     * Add a new player to the lobby
     * @param playerName: the player's name
     */
    private void findGame(String playerName){
        if(playerName.length() == 0){
            printToast("Your name cannot be empty !", Toast.LENGTH_LONG);
            return;
        }
        Button button = (Button)findViewById(R.id.findgame);
        button.setEnabled(false);
        button.setText("Finding a game... !");
        adapter.add(playerName);
        adapter.notifyDataSetChanged();
        TextView tv = (TextView)findViewById(R.id.textView2);
        tv.setText("Lobby - "+adapter.getCount()+" player(s)");
        this.atws.addNewPlayer(playerName);
    }

    /**
     * AT
     * Return the name of a new player when it has been found with AT
     * @param newPlayer: the new player's name
     */
    @Override
    public void foundNewPlayer(String newPlayer, Integer playerId){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                printToast("New player found ! "+newPlayer, Toast.LENGTH_SHORT);
                playersId.add(playerId);
                adapter.add(newPlayer);
                adapter.notifyDataSetChanged();
                if(players.size() >= 2){
                    TextView tv = (TextView)findViewById(R.id.textView2);
                    tv.setText("Lobby - "+adapter.getCount()+" player(s)");
                    Button startGame = (Button)findViewById(R.id.startgame);
                    startGame.setVisibility(View.VISIBLE);
                    startGame.setEnabled(true);
                }
            }
        });
    }

    /**
     * Used to remove a player from the lobby
     * @param playerId player's id
     */
    @Override
    public void removePlayerFromLobby(int playerId){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int i = playersId.indexOf(playerId)+1; // at index 0, I have my first other player in adapter (so +1)
                System.out.println("Remove from lobby player "+i);
                System.out.println(adapter.getItem(i));
                adapter.remove(adapter.getItem(i));
                adapter.notifyDataSetChanged();
                if(players.size() < 2){ // hide the button
                    Button startGame = (Button)findViewById(R.id.startgame);
                    startGame.setVisibility(View.INVISIBLE);
                    startGame.setEnabled(false);
                }
            }
        });
    }


    /**
     * Print Toast
     * @param message: the message
     * @param duration: message's duration
     */
    private void printToast(String message, int duration){
        Toast.makeText(getApplicationContext(),message,duration).show();
    }

    /**
     * Change activity to start the game and notify all players
     */
    private void startGame(){
        atws.startGame();
        hasStartedTheGame = true;
        changeActivity();
    }

    /**
     * Start the game when one of the player has pressed the start button
     */
    @Override
    public void startGameAT(int playerId, List<Integer> deck, List<List<Integer>> playersCards,List<Integer> playersStates, int nbPlayers, List<Integer> playersIdsList){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //printToast("NEW GAME STARTED",Toast.LENGTH_LONG);
                GameLogic logic = new GameLogic(playerId, deck, playersCards, playersStates, nbPlayers, playersIdsList); // create the GameLogic object
                changeActivity();
            }
        });
    }

    /**
     * Change the activity to MainActivity
     * Send the list of players to MainActivity
     */
    private void changeActivity(){
        Intent intent = new Intent(this,MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean("hasStartedTheGame", hasStartedTheGame);
        bundle.putSerializable("players",(Serializable) players);
        intent.putExtra("bundle",bundle);
        startActivity(intent);
    }

    // ############
    // ##   AT   ##
    // ############

    // Manage AmbientTalk Startup
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v("weKittens", "Return of Asset Installer activity");
        switch (requestCode) {
            case (_ASSET_INSTALLER_):
                if (resultCode == Activity.RESULT_OK) {
                    new StartIATTask().execute();
                }
                break;
        }
    }

    @Override
    public JWeKittens registerATApp(ATWeKittens weKittens) {
        this.atws = weKittens;
        return this;
    }

    /**
     * When gui is ready, ping AT
     */
    protected void onStart(){
        super.onStart();
        if(atws != null){
            atws.ping(this);
        }
    }

    public class StartIATTask extends AsyncTask<Void, String, Void> {

        private ProgressDialog pd;

        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            pd.setMessage(values[0]);
        }

        protected void onPreExecute() {
            super.onPreExecute();
            pd = ProgressDialog.show(LobbyActivity.this, "weKittens", "Starting AmbientTalk");
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            pd.dismiss();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                iat = IATAndroid.create(LobbyActivity.this);

                this.publishProgress("Loading weKittens code");
                iat.evalAndPrint("import /.demo.weKittens.weKittens.makeWeKittens()", System.err);
            } catch (Exception e) {
                Log.e("AmbientTalk", "Could not start IAT", e);
            }
            return null;
        }
    }


    // ############
    // ## Tuples ##
    // ############

    // -> should have done that in another class but I was too far in the project to change my structure
    // -> to be improved -> create another class to correctly handle AT

    /**
     * Handle incoming tuples from AT
     * @param cardId the card id
     * @param from the emitter
     * @param to the receiver
     */
    @Override
    public void handleTuple(int cardId, int from, int to, int roundNb, boolean personal, List<Integer> deck, List<Integer> states, Integer favorCardId, Integer nopeCardId){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GameLogic.INSTANCE.handleTuple(cardId,from,to,roundNb,personal,deck,states,favorCardId,nopeCardId);
            }
        });
    }

    /**
     * Handle when a player disconnect from the game
     * Call the GameLogic class
     * @param playerId the player's id
     */
    @Override
    public void playerDisconnected(int playerId){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GameLogic.INSTANCE.playerDisconnected(playerId);
            }
        });
    }

    /**
     * After 30 seconds, remove the player from the game
     * @param playerId the player's id to remove from the game
     */
    @Override
    public void removePlayerFromGame(int playerId){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GameLogic.INSTANCE.removePlayer(playerId);
            }
        });
    }

}
