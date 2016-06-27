package com.robosoft.archana.rangeseekbarassign;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements RangeSeekBar.OnRangeSeekBarChangeListener{

    private TextView mTextMin,mTextMax;
    private RangeSeekBar mRangeSeekBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void initView(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTextMin = (TextView) findViewById(R.id.minTxt);
        mTextMax= (TextView) findViewById(R.id.maxTxt);
        mRangeSeekBar = (RangeSeekBar) findViewById(R.id.seekbar);
        mRangeSeekBar.setTextAboveThumbsColor(R.color.colorAccent);
        mRangeSeekBar.setRangeValues(0,100);
        mRangeSeekBar.setOnRangeSeekBarChangeListener(this);


    }

    @Override
    public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue) {
        mTextMax.setText("Max Value is"+maxValue);
        mTextMin.setText("Min Value is"+minValue);
       // bar.setBackgroundColor(getResources().getColor(R.color.activeColor));

    }
}
