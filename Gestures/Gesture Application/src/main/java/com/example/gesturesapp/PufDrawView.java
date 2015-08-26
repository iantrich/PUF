package com.example.gesturesapp;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import au.com.bytecode.opencsv.CSVWriter;

public class PufDrawView extends View
{
    private Paint redPaint;
    private Paint greenPaint;
    private Paint bluePaint;
    private TextView tv;
    private Point[] challenge;
    private ArrayList<Point> response;

    public PufDrawView(Context context, AttributeSet attr)
    {
        super(context, attr);
        init();
    }

    public void setTV(TextView tv)
    {
        this.tv = tv;
    }

    private void init()
    {
        redPaint = new Paint();
        redPaint.setAntiAlias(true);
        redPaint.setStyle(Paint.Style.STROKE);
        redPaint.setColor(Color.RED);
        redPaint.setStrokeWidth(15);

        bluePaint = new Paint();
        bluePaint.setAntiAlias(true);
        bluePaint.setStyle(Paint.Style.STROKE);
        bluePaint.setColor(Color.BLUE);
        bluePaint.setStrokeWidth(10);

        greenPaint = new Paint();
        greenPaint.setAntiAlias(true);
        greenPaint.setStyle(Paint.Style.STROKE);
        greenPaint.setColor(Color.GREEN);
        greenPaint.setStrokeWidth(5);

        response = new ArrayList<Point>();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        if(challenge.length >= 4)
        {
            Path challengeStart = new Path();
            challengeStart.moveTo(challenge[0].x, challenge[0].y);
            challengeStart.lineTo(challenge[1].x, challenge[1].y);

            Path challengeMiddle = new Path();
            challengeMiddle.moveTo(challenge[1].x, challenge[1].y);
            challengeMiddle.lineTo(challenge[2].x, challenge[2].y);

            Path challengePath = new Path();
            for(int i = 2; i < challenge.length; i++)
            {
                if(i == 2)
                {
                    challengePath.moveTo(challenge[i].x, challenge[i].y);
                }
                else
                {
                    challengePath.lineTo(challenge[i].x, challenge[i].y);
                }
            }
            canvas.drawPath(challengePath, redPaint);
            canvas.drawPath(challengeMiddle, bluePaint);
            canvas.drawPath(challengeStart, greenPaint);
        }
        else if(challenge.length == 2)
        {
            Path challengeLine = new Path();
            challengeLine.moveTo(challenge[0].x, challenge[0].y);
            challengeLine.lineTo(challenge[1].x, challenge[1].y);
            canvas.drawPath(challengeLine, redPaint);
            canvas.drawPoint(challenge[0].x, challenge[0].y, greenPaint);
        }
    }

    public void giveChallenge(Point[] challenge)
    {
        this.challenge = challenge;
        this.invalidate();
    }

    @Override
    public boolean onTouchEvent( MotionEvent me )
    {
        switch( me.getAction() )
        {
            case MotionEvent.ACTION_DOWN:
                tv.setText( "(x,y) = (" + Math.round(me.getX()) + ", " + Math.round(me.getY()) + ") - Pressure = " + me.getPressure() );
                response.clear();
                response.add(new Point(me.getX(), me.getY(), me.getPressure()));
                break;
            case MotionEvent.ACTION_UP:
                tv.setText( "No touch detected." );
                //RECORD ACTION FINISH 
                writeResponseCsv();

                //TODO: rather hacky reference to mainactivity that could cause 
                //problems if this code is ever re-used
                ((MainActivity)getContext()).informOfResponse(); 

                break;
            case MotionEvent.ACTION_MOVE:
                tv.setText( "(x,y) = (" + Math.round(me.getX()) + ", " + Math.round(me.getY()) + ") Pressure = " + me.getPressure() );
                response.add(new Point(me.getX(), me.getY(), me.getPressure()));
                break;
            default:
                break;
        }

        return true;
    }

    public void writeResponseCsv()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String testerName = prefs.getString("TesterName", "Ryan Scheel");
        String deviceName = prefs.getString("DeviceName", "nexus-03");
        String currentSeed = prefs.getString("CurrSeed", "1");

        File baseDir = new File(Environment.getExternalStorageDirectory(), "581Proj");
        if (!baseDir.exists())
        {
            baseDir.mkdirs();
        }
        String fileName = currentSeed + ": " + deviceName + " " + testerName + " " + getCurrentLocalTime() + ".csv";
        File f = new File(baseDir, fileName);
        try
        {
            f.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(f));
            String[] challengeHeaders = {"ChallengeX", "ChallengeY", "Tester Name", "Device Name"};
            csvWrite.writeNext(challengeHeaders);
            for( int i = 0; i < challenge.length; i++)
            {
                Point point = challenge[i];
                String[] row = { Float.toString(point.x),
                    Float.toString(point.y),
                    testerName, 
                    deviceName };
                csvWrite.writeNext(row);
            }

            String[] headers = { "X", "Y", "PRESSURE" };
            csvWrite.writeNext(headers);
            for( int i = 0; i < response.size(); i++)
            {
                Point point = response.get(i);
                String[] row = { Float.toString(point.x),
                    Float.toString(point.y),
                    Float.toString(point.pressure) };
                csvWrite.writeNext(row);
            }
            csvWrite.close();
        }
        catch( Exception e)
        {
            Toast.makeText(getContext(), e.toString(), 
                                    Toast.LENGTH_SHORT).show();
        } 
    }

    public String getCurrentLocalTime()
    {
        Calendar c = Calendar.getInstance(); 
        String format = "yyyy-MM-dd hh:mm:ss aa";
        SimpleDateFormat localSdf = new SimpleDateFormat(format);
        return localSdf.format(c.getTime());
    }
}
