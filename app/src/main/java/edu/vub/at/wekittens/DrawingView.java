package edu.vub.at.wekittens;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

public class DrawingView extends View {

    private final int TOP_W = 400;
    private final int TOP_H = 400;

    private final int PL_W = TOP_W / 2;
    private final int PL_H = TOP_H / 2;


    private final int STACK_MAX_SIZE=3;
    private final Card BLANK_CARD = new Card(Card.CardType.back, "card" +
            "",-1); // added -1 for blank card
    private final int MAX_ANGLE = 10;


    private Card topCard;
    private Random random;
    private int played = 0;

    private int topPlayerCount   = 3;
    private int leftPlayerCount  = 3;
    private int rightPlayerCount = 3;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        random = new Random();
        topCard = BLANK_CARD;
    }

    public void playCard(Card card) {
        topCard = card;
        played ++;
    }

    public void setTopPlayerCount(int topPlayerCount) {
        this.topPlayerCount = topPlayerCount;
    }

    public void setLeftPlayerCount(int leftPlayerCount) {
        this.leftPlayerCount = leftPlayerCount;
    }

    public void setRightPlayerCount(int rightPlayerCount) {
        this.rightPlayerCount = rightPlayerCount;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int cwidth  = canvas.getWidth();
        int cheight = canvas.getHeight();


        int px = cwidth / 2, py = cheight / 2;


        // Draw players
        for (int i=0; i<Math.min(STACK_MAX_SIZE, topPlayerCount); i++)
            drawCardWithAngle(canvas, BLANK_CARD, PL_W, PL_H, px , PL_H /2, 180 + randomAngle() * 2);

        for (int i=0; i<Math.min(STACK_MAX_SIZE, leftPlayerCount); i++)
            drawCardWithAngle(canvas, BLANK_CARD, PL_W, PL_H, PL_H / 2 , py, 90 + randomAngle() * 2);

        for (int i=0; i<Math.min(STACK_MAX_SIZE, rightPlayerCount); i++)
            drawCardWithAngle(canvas, BLANK_CARD, PL_W, PL_H, cwidth - PL_H /2 , py, -90 + randomAngle() * 2);


        // Draw top card
        if (played > 1) {
            for (int i=0; i<Math.min(STACK_MAX_SIZE, played)-1; i++)
                drawCardWithAngle(canvas, BLANK_CARD, TOP_W, TOP_H, px , py, randomAngle());
        }

        drawCardWithAngle(canvas, topCard, TOP_W, TOP_H, px , py, 0);

    }

    private int randomAngle() {
        return random.nextInt(MAX_ANGLE * 2 ) - MAX_ANGLE;
    }


    private void drawCardWithAngle(Canvas canvas, Card card, int w, int h, int x, int y, int angle) {
        Drawable d = getResources().getDrawable(card.getResourceId(), null);

        Bitmap bmResult = Bitmap.createBitmap(w*2, h*2, Bitmap.Config.ARGB_8888);
        Canvas tempCanvas = new Canvas(bmResult);

        tempCanvas.rotate(angle, w, h);
        d.setBounds(w/2, h/2, w/2+w, h/2+h);
        d.draw(tempCanvas);

        canvas.drawBitmap(bmResult, x-w, y-h, null);
    }

    // added method

    /**
     * Get the card played by the player
     * @return the played card
     */
    public Card getTopCard(){
        return this.topCard;
    }
}
