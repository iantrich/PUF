package com.example.gesturesapp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends Activity
{

    private TextView tv;
    private TextView CurrSeedTv;
    private TextView TotalSeedTv;
    private long currSeed;
    private long seedsCreated;
    private PufDrawView pufDrawView;
    private SharedPreferences prefs;
    private Random rng;


    OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener()
    {
        public void onSharedPreferenceChanged(SharedPreferences prefs,
                String key)
        {
            // handle changing preferences
            if (key.equals("CurrSeed"))
            {
                currSeed = Long.parseLong(prefs.getString("CurrSeed", "1"));
                CurrSeedTv.setText("Seed: " + Long.toString(currSeed));
                TotalSeedTv.setText("Total: " + Long.toString(seedsCreated));
                rng.setSeed(currSeed);
                ArrayList<Point> challenge = generateChallenge();
                pufDrawView.giveChallenge(challenge.toArray(new Point[challenge
                        .size()]));
            }
        }
    };

    public void informOfResponse()
    {
        if (prefs.getBoolean("AutoIncSeed", true))
        {
            Editor prefEditor = prefs.edit();
            if(currSeed >= 8)
            {
                currSeed = 0;
                seedsCreated++;
            }
            prefEditor.putString("CurrSeed", Long.toString(currSeed + 1));
            prefEditor.commit();
            listener.onSharedPreferenceChanged(prefs, "CurrSeed");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        currSeed = Long.parseLong(prefs.getString("CurrSeed", "1"));

        rng = new Random(currSeed);

        // Initialize Views
        CurrSeedTv = (TextView) findViewById(R.id.currentSeedTV);
        TotalSeedTv = (TextView) findViewById(R.id.totalSeedTV);

        CurrSeedTv.setText("Seed: " + Long.toString(currSeed));
        TotalSeedTv.setText("Total: " + Long.toString(seedsCreated));

        tv = (TextView) findViewById(R.id.textview);

        pufDrawView = (PufDrawView) findViewById(R.id.pufDrawView);
        pufDrawView.setTV(tv);

        // Done Initializing Views

        ArrayList<Point> challenge = generateChallenge();
        pufDrawView
                .giveChallenge(challenge.toArray(new Point[challenge.size()]));
    }

    private ArrayList<Point> generateChallenge()
    {
        if(currSeed >= 2000)
        {
            return challGenMethod3();
        }
        else if(currSeed >= 1000)
        {
            return challGenMethod2();
        }
        else
        {
            return challGenMethod1();
        }
    }

    /*
     * This was my first attempt at generating paths
     */
    private ArrayList<Point> challGenMethod1()
    {
        ArrayList<Point> challenge = new ArrayList<Point>();
        challenge.add(randPntInBnds());
        challenge.add(randPntInBnds());
        challenge.add(randPntInBnds());
        challenge.add(randPntInBnds());
        int rejectionPoint = 50;

        // ensure that none of the points are too close to each other
        boolean badPath = false;
        for (int i = 0; i < challenge.size() && !badPath; i++)
        {
            Point point1 = challenge.get(i);
            for (int j = i + 1; j < challenge.size() && !badPath; j++)
            {
                Point point2 = challenge.get(j);
                if (Math.abs((point1.x - point2.x)) < rejectionPoint
                        && Math.abs((point1.y - point2.y)) < rejectionPoint)
                {
                    badPath = true;
                }
            }
        }

        if (badPath)
        {
            return generateChallenge(); // TODO: probably the laziest way to do
                                        // this.
        }
        else
        {
            return challenge;
        }
    }

    /*
     * This method generates longer paths
     */
    private ArrayList<Point> challGenMethod2()
    {
        ArrayList<Point> challenge = new ArrayList<Point>();
        challenge.add(randPntInBnds());
        challenge.add(randPntInBnds());
        challenge.add(randPntInBnds());
        challenge.add(randPntInBnds());
        challenge.add(randPntInBnds());
        challenge.add(randPntInBnds());
        challenge.add(randPntInBnds());
        int rejectionPoint = 100;

        // ensure that none of the points are too close to eachother
        boolean badPath = false;
        for (int i = 0; i < challenge.size() && !badPath; i++)
        {
            Point point1 = challenge.get(i);
            for (int j = i + 1; j < challenge.size() && !badPath; j++)
            {
                Point point2 = challenge.get(j);
                if (Math.abs((point1.x - point2.x)) < rejectionPoint
                        && Math.abs((point1.y - point2.y)) < rejectionPoint)
                {
                    badPath = true;
                }
            }
        }

        if (badPath)
        {
            return generateChallenge(); // TODO: probably the laziest way to do
                                        // this.
        }
        else
        {
            return challenge;
        }
    }

    /*
     * This method generates paths with only a single line
     */
    private ArrayList<Point> challGenMethod3()
    {
        ArrayList<Point> challenge = new ArrayList<Point>();
        challenge.add(randPntInBnds());
        challenge.add(randPntInBnds());
        int rejectionPoint = 400;

        // ensure that none of the points are too close to eachother
        boolean badPath = false;
        for (int i = 0; i < challenge.size() && !badPath; i++)
        {
            Point point1 = challenge.get(i);
            for (int j = i + 1; j < challenge.size() && !badPath; j++)
            {
                Point point2 = challenge.get(j);
                if (Math.abs((point1.x - point2.x)) < rejectionPoint
                        && Math.abs((point1.y - point2.y)) < rejectionPoint)
                {
                    badPath = true;
                }
            }
        }

        if (badPath)
        {
            return generateChallenge(); // TODO: probably the laziest way to do
                                        // this.
        }
        else
        {
            return challenge;
        }
    }

    private Point randPntInBnds()
    {
        int x = 100 + rng.nextInt(550); // x ranges from 100 to 650
        int y = 100 + rng.nextInt(800); // y ranges from 100 to 900
        return new Point(x, y, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int itemId = item.getItemId();
        if (itemId == R.id.action_settings)
        {
            // Display Settings page
            Intent preferenceIntent = new Intent(this, SettingsActivity.class);
            startActivity(preferenceIntent);
            return true;
        }
        else if (itemId == R.id.action_resetSeed)
        {
            Editor prefEditor = prefs.edit();
            long initialSeed = Long.parseLong(prefs.getString("InitialSeed",
                    "1"));
            prefEditor.putString("CurrSeed", Long.toString(initialSeed));
            prefEditor.commit();
            listener.onSharedPreferenceChanged(prefs, "CurrSeed");
            return true;
        }
        else if (itemId == R.id.action_emailcsvs)
        {
            // Send emails
            sendCSVEmail();
            return true;
        }
        else
        {
            return super.onOptionsItemSelected(item);
        }
    }

    private void sendCSVEmail()
    {
        String to = "nmont@iastate.edu";
        String subject = "PUF Authentication Testing";
        String message = "";

        Intent i = new Intent(Intent.ACTION_SEND_MULTIPLE);
        i.setType("plain/text");
        i.putExtra(Intent.EXTRA_EMAIL, new String[] { to });
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
        i.putExtra(Intent.EXTRA_TEXT, message);

        /*
         * Date dateVal = new Date(); String filename = dateVal.toString(); data
         * = File.createTempFile("Report", ".csv"); FileWriter out =
         * (FileWriter) GenerateCsv.generateCsvFile( data, "Name,Data1");
         */

        File baseDir = new File(Environment.getExternalStorageDirectory(),
                "581Proj");
        if (!baseDir.exists())
        {
            baseDir.mkdirs();
        }
        List<File> files = getListFiles(baseDir);

        startActivity(Intent.createChooser(i, "E-mail"));

        ArrayList<Uri> uris = new ArrayList<Uri>();
        // convert from paths to Android friendly Parcelable Uri's
        for (File fileIn : files)
        {
            Uri u = Uri.fromFile(fileIn);
            uris.add(u);
        }
        i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        startActivity(Intent.createChooser(i, "Send mail..."));
        seedsCreated = 0;
        TotalSeedTv.setText("Total: " + Long.toString(seedsCreated));
    }

    private List<File> getListFiles(File parentDir)
    {
        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = parentDir.listFiles();
        for (File file : files)
        {
            if (file.isDirectory())
            {
                inFiles.addAll(getListFiles(file));
            }
            else
            {
                if (file.getName().endsWith(".csv"))
                {
                    inFiles.add(file);
                }
            }
        }
        return inFiles;
    }
}
