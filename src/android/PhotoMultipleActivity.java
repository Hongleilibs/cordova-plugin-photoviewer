package com.Hongleilibs.PhotoViewer;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.senab.photoview.PhotoViewAttacher;

public class PhotoMultipleActivity extends Activity implements OnPageChangeListener {
    private PhotoViewAttacher mAttacher;
    private ImageView photo;
    private ViewPager view_pager;
    private ImageButton closeBtn;
    private ImageButton shareBtn;
    private TextView titleTxt;
    private TextView account;
    private ProgressBar progressBar;
    private JSONObject options;
    private View itemView;
    private JSONArray jsonArray;
    private int current_position = 0;
    private static int select_position = 0;
    static CustomPagerAdapter mCustomPagerAdapter;
    private boolean share = false;
    String TAG = "PhotoMultipleActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy( policy );
        }
        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );

        setContentView( getApplication().getResources().getIdentifier( "activity_multiple_photo", "layout", getApplication().getPackageName() ) );
        closeBtn = (ImageButton) findViewById( getApplication().getResources().getIdentifier( "closeBtn", "id", getApplication().getPackageName() ) );
        shareBtn = (ImageButton) findViewById( getApplication().getResources().getIdentifier( "shareBtn", "id", getApplication().getPackageName() ) );
        closeBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        } );
        try {
            options = new JSONObject( this.getIntent().getStringExtra( "options" ) );
            if (options.has( "share" )) {
                share = options.getBoolean( "share" );
            }
            current_position = Integer.parseInt( this.getIntent().getStringExtra( "title" ) );
            jsonArray = options.optJSONArray( "img_array" );
            Log.e( "PhotoMulitple", "jsonArray----" + jsonArray );
        } catch (JSONException exception) {
        }

        if (share == false) {
            shareBtn.setVisibility( View.GONE );
            TextView title = (TextView) findViewById( getApplication().getResources().getIdentifier( "titleTxt", "id", getApplication().getPackageName() ) );
            title.setPadding( 0, 0, 60, 0 );
        }
        //更新TextView UI
        findViews();
    }

    /**
     * Find and Connect Views
     */
    private Map<String, Object> findViews(View itemView) {
        Map<String, Object> map = new HashMap<String, Object>();

        // Photo Container
        photo = (ImageView) itemView.findViewById( getApplication().getResources().getIdentifier( "photoView", "id", getApplication().getPackageName() ) );
        map.put( "photo",photo );
        mAttacher = new PhotoViewAttacher( photo );

        progressBar = (ProgressBar) itemView.findViewById( getApplication().getResources().getIdentifier( "progressBar1", "id", getApplication().getPackageName() ) );
        map.put( "progressBar",progressBar );
        //Account TextView
        account = (TextView) itemView.findViewById( getApplication().getResources().getIdentifier( "account", "id", getApplication().getPackageName() ) );
        // Title TextView
        titleTxt = (TextView) itemView.findViewById( getApplication().getResources().getIdentifier( "titleTxt", "id", getApplication().getPackageName() ) );
        return map;
    }

    private void findViews() {
        view_pager = (ViewPager) findViewById( getApplication().getResources().getIdentifier( "view_pager", "id", getApplication().getPackageName() ) );
        if (jsonArray != null && jsonArray.length() > 0) {
            mCustomPagerAdapter = new CustomPagerAdapter( PhotoMultipleActivity.this, getApplication().getResources().getIdentifier( "activity_multiple_photo", "layout", getApplication().getPackageName() ) );
            view_pager.setAdapter( mCustomPagerAdapter );
            view_pager.setOnPageChangeListener( this );
            view_pager.setOffscreenPageLimit( view_pager.getAdapter().getCount() );
            if (current_position == 0) {
                onPageSelected( 0 );
            } else {
                view_pager.setCurrentItem( current_position );
            }
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    //ViewPager翻页结束，页面稳定之后被调用
    @Override
    public void onPageSelected(int position) {
        select_position = position;
        String actTitle = jsonArray.optJSONObject( position ).optString( "title" );
        TextView titleTxt = (TextView) findViewById( getApplication().getResources().getIdentifier( "titleTxt", "id", getApplication().getPackageName() ) );
        if (!actTitle.equals( "" )) {
            Spannable sp = new SpannableString( actTitle );
            sp.setSpan( new AbsoluteSizeSpan( 18, true ), 0, actTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE );
            titleTxt.setText( sp );
        } else {
            titleTxt.setText( "" );
        }
        account = (TextView) findViewById( getApplication().getResources().getIdentifier( "account", "id", getApplication().getPackageName() ) );
        String description = jsonArray.optJSONObject( position ).optString( "description" );
        if (!description.equals( "" )) {
            account.setText( description );
        } else {
            account.setText( "" );
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public Bitmap getImageBitmap(String url) {
        URL imgUrl = null;
        Bitmap bitmap = null;
        try {
            imgUrl = new URL( url );
            HttpURLConnection conn = (HttpURLConnection) imgUrl
                    .openConnection();
            conn.setDoInput( true );
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream( is );
            is.close();
        } catch (Exception e) {
            bitmap = null;
            e.printStackTrace();
        }
        return bitmap;
    }

    class CustomPagerAdapter extends PagerAdapter {
        Context mContext;
        LayoutInflater mLayoutInflater;
        int activityPhotoId;
        private View currentView;
        private int lastPosition = -1;
        private Handler handler;
        int i = 0;

        public CustomPagerAdapter(Context context, int activityPhotoId) {
            mContext = context;
            mLayoutInflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        }

        @Override
        public void setPrimaryItem(View container, int position, Object object) {
            Log.d( TAG, "setPrimaryItem: " + position );
            if (lastPosition != position) {
                ++i;
                System.out.println("图片浏览次数："+i);
                lastPosition = position;
                currentView = (View) object;
                findViews( currentView );
                new asyncTask().executeOnExecutor( AsyncTask.THREAD_POOL_EXECUTOR ,jsonArray.optJSONObject( position ).optString( "url" ) ,currentView);
            };
        }

        private class asyncTask extends AsyncTask<Object,Object,Bitmap> {
            private ImageView ImageView;
            private ProgressBar progressBar2;

            @Override
            protected Bitmap doInBackground(Object... strings) {
                View view = (View) strings[1];
                Bitmap bitmap = getImageBitmap( strings[0].toString() );
                publishProgress(bitmap,view);
                return bitmap;
            }

            @Override
            protected void onProgressUpdate(Object... values) {
                super.onProgressUpdate(values);
                View view = (View) values[1];
                Map Image = findViews( view );
                ImageView =  (ImageView)Image.get( "photo" );
                progressBar2 =  (ProgressBar)Image.get( "progressBar" );
                Bitmap bitmap = (Bitmap)values[0];
                progressBar2.setVisibility( View.GONE );
                if (bitmap == null) {
                    Drawable drawable = getResources().getDrawable( getApplication().getResources().getIdentifier( "image_failed", "drawable", getApplication().getPackageName() ) );
                    BitmapDrawable bmpDraw = (BitmapDrawable) drawable;
                    bitmap = bmpDraw.getBitmap();
                    ImageView.setScaleX( 0.4f );
                    ImageView.setScaleY( 0.4f );
                    ImageView.setEnabled( false );
                }
                ImageView.setImageBitmap( bitmap );
                ImageView.invalidate();
            }
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return jsonArray.length();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            itemView = mLayoutInflater.inflate(
                    getApplication().getResources().getIdentifier( "activity_photo", "layout", getApplication().getPackageName() ),
                    container, false );
            findViews( itemView );
            try {
                Log.e( "PhotoMulitple", "position----" + position );
                if (jsonArray != null && jsonArray.length() > 0) {
                    container.addView( itemView );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView( (FrameLayout) object );
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((FrameLayout) object);
        }
    }
}