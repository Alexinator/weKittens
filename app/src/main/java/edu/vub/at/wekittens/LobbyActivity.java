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

import edu.vub.at.IAT;
import edu.vub.at.android.util.IATAndroid;
import edu.vub.at.wekittens.interfaces.ATWeKittens;
import edu.vub.at.wekittens.interfaces.JWeKittens;


public class LobbyActivity extends AppCompatActivity implements JWeKittens {

    // AT attributes
    private static IAT iat;
    private static final int _ASSET_INSTALLER_ = 0;
    // static so we can easily use it in MainActivity (TODO change)
    public static ATWeKittens atws;
    private ArrayList<String> players;
    private ListView listView;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        // first create iat
        if (iat == null) {
            Intent i = new Intent(this, WeKittensAssetInstaller.class);
            //Bundle bundle = new Bundle();
            //bundle.putString("basedir",getExternalFilesDir(null).getAbsolutePath());
            //i.putExtras(bundle);
            startActivityForResult(i, _ASSET_INSTALLER_);
        }
        players = new ArrayList<>();
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
        atws.addNewPlayer(playerName);
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
     * AT
     * Return the name of a new player when it has been found with AT
     * @param newPlayer: the new player's name
     */
    @Override
    public void foundNewPlayer(String newPlayer){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                printToast("New player found ! "+newPlayer, Toast.LENGTH_SHORT);
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
     * Change activity to start the game and notify all players
     */
    private void startGame(){
        atws.startGame();
        changeActivity();
    }

    /**
     * Start the game when one of the player has pressed the start button
     */
    @Override
    public void startGameAT(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //printToast("NEW GAME STARTED",Toast.LENGTH_LONG);
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

}
