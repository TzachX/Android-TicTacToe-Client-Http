package com.example.user.localttt;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TableLayout;

import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Random;

public class MainActivity extends Activity {

    TextView turn,whoami;
    TableLayout tl;
    boolean gameover = false;
    Handler h;
    int [] [] xo = new int [3][3];
    private boolean isX = true;
    boolean player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        turn = (TextView)findViewById(R.id.turn);
        tl = (TableLayout)findViewById(R.id.game);
        whoami = (TextView)findViewById(R.id.whoami);
        h=new Handler(getMainLooper());
        checkWho();
        turn.setText("It's X turn");
    }

    public void choice(View v) throws InterruptedException {
        if(player == isX) {
            TextView t = (TextView) v;
            if (t.getText().toString() == "") {
                t.setText(isX ? "X" : "O");
                String[] idxs = v.getTag().toString().split(",");
                String row = idxs[0];
                String col = idxs[1];
                sendPostRequest(isX, row, col);
                Thread.sleep(30);
                sendGetRequest();
                Thread.sleep(30);
                if (!turn.getText().toString().equals("GAME OVER")) {
                    isX = !isX;
                    if (isX) {
                        turn.setText("It's X turn");
                    } else {
                        turn.setText("It's O turn");
                    }
                }
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Now isn't your turn!", Toast.LENGTH_SHORT).show();
        }
    }


    private  void sendPostRequest(final boolean isX, final String row, final String col){
        new Thread(){
            public void run() {
                try {
                    //send Http get request to our server
                    JSONObject json= new JSONObject().put("?","move").put("row", row).put("col",col).put("isX",isX);
                    final String res = new HttpRequest("http://10.0.0.1:999/XO").prepare(HttpRequest.Method.POST).withData(json.toString()).sendAndReadString();
                    h.post(new Runnable() {
                        public void run() {
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void sendGetRequest()
    {
        new Thread(){
            public void run() {
                try {
                    //send Http get request to our server
                    final String res = new HttpRequest("http://10.0.0.1:999/XO").prepare(HttpRequest.Method.GET).sendAndReadString();
                    h.post(new Runnable() {
                        public void run() {
                                if(res.trim().equals("gameover"))
                                {
                                    turn.setText("GAME OVER");
                                }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void reFresh(View v)
    {
        new Thread(){
            public void run() {
                try {
                    //send Http get request to our server
                    JSONObject json= new JSONObject().put("?", "refresh");
                    final String res = new HttpRequest("http://10.0.0.1:999/XO").prepare(HttpRequest.Method.POST).withData(json.toString()).sendAndReadString();
                    h.post(new Runnable() {
                        public void run() {
                            String newS = res.trim();
                            if(newS != "n") {
                                String[] allS = newS.split(",");
                                if (allS[0].equals("O")) {
                                    isX = false;
                                    turn.setText("It's O turn");
                                } else {
                                    isX = true;
                                    turn.setText("It's X turn");
                                }
                                if(!allS[1].equals(allS[0]))
                                {
                                    String row = allS[2];
                                    String col = allS[3];
                                    View v = tl.findViewWithTag(row+","+col);
                                    TextView t = (TextView)v;
                                    if(allS[1].equals("X"))
                                    {
                                        t.setText("X");
                                    }
                                    else
                                    {
                                        t.setText("O");
                                    }
                                }
                            }
                        }
                    });
                } catch (IOException e) {
                    System.out.print("CANT CONNECT");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void checkWho()
    {
        new Thread(){
            public void run() {
                try {
                    //send Http get request to our server
                    JSONObject json= new JSONObject().put("?", "whoami");
                    final String res = new HttpRequest("http://10.0.0.1:999/XO").prepare(HttpRequest.Method.POST).withData(json.toString()).sendAndReadString();
                    h.post(new Runnable() {
                        public void run() {
                            if(res.trim().equals("X"))
                            {
                                player = true;
                                whoami.setText("You are X");
                            }
                            else
                            {
                                player = false;
                                whoami.setText("You are O");
                            }
                        }
                    });
                } catch (IOException e) {
                    System.out.print("CANT CONNECT");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}

