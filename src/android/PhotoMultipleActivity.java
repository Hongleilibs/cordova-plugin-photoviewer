package com.Hongleilibs.PhotoViewer;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
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
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.cordova.hellocordova.R;
import uk.co.senab.photoview.PhotoViewAttacher;

public class PhotoMultipleActivity extends Activity {
    private PhotoViewAttacher mAttacher;
    private ImageView photo;
    private ViewPager view_pager;
    private ImageButton closeBtn;
    private ImageButton shareBtn;
    private TextView titleTxt;
    private TextView account;
    private JSONObject options;
    private JSONArray jsonArray;
    private int current_position = 0;
    CustomPagerAdapter mCustomPagerAdapter;
    private Handler handler;
    private boolean share = false;
    List<Bitmap> bitmapList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView( getApplication().getResources().getIdentifier("activity_multiple_photo", "layout", getApplication().getPackageName()));
        closeBtn =  (ImageButton) findViewById(getApplication().getResources().getIdentifier("closeBtn", "id", getApplication().getPackageName()));
        shareBtn =  (ImageButton) findViewById(getApplication().getResources().getIdentifier("shareBtn", "id", getApplication().getPackageName()));
 
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        try {
            options = new JSONObject(this.getIntent().getStringExtra("options"));
            if( options.has( "share" )){
                share = options.getBoolean( "share" );
            }
            current_position = Integer.parseInt(this.getIntent().getStringExtra("title"));
            jsonArray = options.optJSONArray("img_array");
            Log.e("PhotoMulitple", "jsonArray----" + jsonArray);
        } catch (JSONException exception) {
        }

        if (share == false){
            shareBtn.setVisibility( View.GONE );
        }
        //在主线程中实例化Handler　　
        handler = new Handler() {
            @Override
            //收到消息时该做的事情
            public void handleMessage(Message msg) {
            super.handleMessage(msg);
                // 取消掉"加载中"的框框
                ProgressBar gifImageView1 = (ProgressBar) findViewById(getApplication().getResources().getIdentifier("progressBar1", "id", getApplication().getPackageName()));
                gifImageView1.setVisibility( View.GONE );
                //更新TextView UI
                findViews();
        }
    };

        new Thread(new Runnable() {
            @Override
            public void run() {
                //访问网络
                if (jsonArray != null && jsonArray.length() > 0) {
                    for(int i = 0 ; i < jsonArray.length() ; i++){
                        final String imageUrl = jsonArray.optJSONObject(i).optString("url");
                        System.out.println("url: "+imageUrl);
                        Bitmap bitmap = getImageBitmap(imageUrl);
                    }
                }
                //给Handler发消息
                Message ok = new Message();
                handler.sendMessage(ok);
            }
        }).start();
    }

    /**
     * Find and Connect Views
     */
    private void findViews(View itemView) {
        // Photo Container
        photo = (ImageView) itemView.findViewById(getApplication().getResources().getIdentifier("photoView", "id", getApplication().getPackageName()));

        mAttacher = new PhotoViewAttacher(photo);

        //Account TextView
        account = (TextView) itemView.findViewById(getApplication().getResources().getIdentifier("account", "id", getApplication().getPackageName()));
        // Title TextView
        titleTxt = (TextView) itemView.findViewById(getApplication().getResources().getIdentifier("titleTxt", "id", getApplication().getPackageName()));
    }

    private void findViews() {
        view_pager = (ViewPager) findViewById(getApplication().getResources().getIdentifier("view_pager", "id", getApplication().getPackageName()));
        if (jsonArray != null && jsonArray.length() > 0) {
            mCustomPagerAdapter = new CustomPagerAdapter(PhotoMultipleActivity.this, getApplication().getResources().getIdentifier("activity_multiple_photo", "layout", getApplication().getPackageName()));
            view_pager.setAdapter(mCustomPagerAdapter);
            view_pager.setCurrentItem(current_position);
        }
    }

    /**
     * Get the current Activity
     *
     * @return
     */
    private Activity getActivity() {
        return this;
    }

    public Bitmap getImageBitmap(String url) {
        URL imgUrl = null;
        Bitmap bitmap = null;
        try {
            imgUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imgUrl
                    .openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            bitmapList.add(bitmap);
            is.close();
        } catch (Exception e) {
            bitmap = null;
            bitmapList.add(bitmap);
            e.printStackTrace();
        }
        return bitmap;
    }


    class CustomPagerAdapter extends PagerAdapter {
        Context mContext;
        LayoutInflater mLayoutInflater;
        int activityPhotoId;
        public CustomPagerAdapter(Context context, int activityPhotoId) {
            mContext = context;
            mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            View itemView = mLayoutInflater.inflate(
                    getApplication().getResources().getIdentifier("activity_photo", "layout", getApplication().getPackageName()),
                    container, false);
            findViews(itemView);
            Log.e("PhotoMulitple", "instantiateItem----");
            try {
                Log.e("PhotoMulitple", "position----" + position);
                if (jsonArray != null && jsonArray.length() > 0) {
                    Bitmap bitmap =  bitmapList.get(position);
                    if (bitmap == null){
                        Drawable drawable = getResources().getDrawable(R.drawable.image_failed);
                        BitmapDrawable bmpDraw = (BitmapDrawable)drawable;
                        bitmap = bmpDraw.getBitmap();
                        photo.setScaleX( 0.4f );
                        photo.setScaleY( 0.4f );
                        photo.setEnabled( false );
                    }
                    photo.setImageBitmap( bitmap );
                    photo.setVisibility(View.VISIBLE);
                    mAttacher.update();
                    String actTitle = jsonArray.optJSONObject(position).optString("title");
                    if (!actTitle.equals("")) {
                        Spannable sp = new SpannableString(actTitle);
                        sp.setSpan(new AbsoluteSizeSpan(18,true),0,actTitle.length(),Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        titleTxt.setText(sp);
                    }
                    String description = jsonArray.optJSONObject(position).optString("description");
                    if (!description.equals("")) {
                        account.setText(description);
                    }
                    container.addView(itemView);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((FrameLayout) object);
        }
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((FrameLayout) object);
        }
    }
}
