package com.purposecaller.purposecaller;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DatabaseReference;
import com.purposecaller.purposecaller.activities.WaitingActivity;
import com.purposecaller.purposecaller.activities.WatchVideoActivity;
import com.purposecaller.purposecaller.models.Contact;
import com.purposecaller.purposecaller.models.Player;
import com.purposecaller.purposecaller.models.Room;
import com.purposecaller.purposecaller.windows.ChoosePurposeWindow;
import com.purposecaller.purposecaller.windows.TextViewWindow;

import static android.content.Context.WINDOW_SERVICE;
import static com.firebase.ui.auth.AuthUI.getApplicationContext;
import static com.purposecaller.purposecaller.Constants.INTERACT_VIA_SIM_CALL;
import static com.purposecaller.purposecaller.Constants.PURPOSE_MUTUAL_WATCH;
import static com.purposecaller.purposecaller.Constants.PURPOSE_TEXT_MESSAGE;
import static com.purposecaller.purposecaller.MyApplication.phoneNumber;
import static com.purposecaller.purposecaller.PhoneCallService.params;
import static com.purposecaller.purposecaller.activities.MainActivity.uid;
import static com.purposecaller.purposecaller.adapters.ContactsRecyclerViewAdapter.density;
import static com.purposecaller.purposecaller.models.Player.NOT_JOINED;


public class MainPhoneCallWindow extends FrameLayout implements View.OnClickListener{
    View expandedView,collapsedView,removeExpandedView;

    Integer mPurposeType;
    private Point szWindow = new Point();
    private boolean isLeft = true;
    DatabaseReference mDatabaseReference;
    private long startClickTime;
    private int downEventX;
    private int downEventY;
    private float downLayoutX;
    private float downLayoutY;
    WindowManager mWindowManager;
    View removeFloatingWidgetView;
    ImageView removeImageView;
    boolean mIsScrolling;
    String textWindowMessage;
    public static final int PURPOSE_CHOOSER=100;
    long time_start = 0, time_end = 0;
    public Contact contact;
    public Room room;
    boolean isLongClick = false;//variable to judge if user click long press
    boolean inBounded = false;//variable to judge if floating view is bounded to remove view
    int remove_img_width = 0, remove_img_height = 0;

    Handler handler_longClick = new Handler();
    Runnable runnable_longClick = new Runnable() {
        @Override
        public void run() {
            //On Floating Widget Long Click

            //Set isLongClick as true
            isLongClick = true;

            //Set remove widget view visibility to VISIBLE
            removeFloatingWidgetView.setVisibility(View.VISIBLE);

            onFloatingWidgetLongClick();
        }
    };


    /*  on Floating Widget Long Click, increase the size of remove view as it look like taking focus */
    private void onFloatingWidgetLongClick() {
        //Get remove Floating view params
        WindowManager.LayoutParams removeParams = (WindowManager.LayoutParams) removeFloatingWidgetView.getLayoutParams();

        //get x and y coordinates of remove view
        int x_cord = (szWindow.x - removeFloatingWidgetView.getWidth()) / 2;
        int y_cord = szWindow.y - (removeFloatingWidgetView.getHeight() + getStatusBarHeight());


        removeParams.x = x_cord;
        removeParams.y = y_cord;

        //Update Remove view params
        mWindowManager.updateViewLayout(removeFloatingWidgetView, removeParams);
    }

    public final  String TAG="MainPhoneCallWindow";

    ViewConfiguration vc = ViewConfiguration.get(getContext());
    private int mTouchSlop=vc.getScaledTouchSlop();

    public MainPhoneCallWindow(Context context) {
        super(context);

        mWindowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        init(null, 0);
    }

    public MainPhoneCallWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public MainPhoneCallWindow(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }


    public int[] getScreenDimensions(DisplayMetrics displayMetrics){

        int width=displayMetrics.widthPixels;
        int height=displayMetrics.heightPixels;

        return new int[]{width,height};
    }


    private void init(AttributeSet attrs, int defStyle) {
        View v=inflate(getContext(),R.layout.window_main_window,null);

        FrameLayout.LayoutParams params=new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(v,params);
        collapsedView=v.findViewById(R.id.collapse_view);
        getWindowManagerDefaultDisplay();


    }




    public void setmPurposeType(int mPurposeType) {
        this.mPurposeType = mPurposeType;
       AppCompatImageView imageView=  findViewById(R.id.collapse_view);

        imageView.setBackgroundResource(R.drawable.contact_circle_shape);
        int padding=(int)(density*8);
        imageView.setPadding(padding,padding,padding,padding);
        ViewCompat.setElevation(imageView, density*12);
        Glide.with(imageView).load(R.drawable.ic_call_black_24dp).apply(new RequestOptions().circleCrop()).into(imageView);

        if(expandedView!=null &&expandedView.getVisibility()==VISIBLE){
            removeView(expandedView);
            addExpandedWindow(mPurposeType,true);
        }

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //Get Floating widget view params
        WindowManager.LayoutParams floatingWidgetParams = (WindowManager.LayoutParams)getLayoutParams();
        int x_cord = (int) event.getRawX();
        int y_cord = (int) event.getRawY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startClickTime = System.currentTimeMillis();
                //remember the initial position.
                downEventX = floatingWidgetParams.x;
                downEventY = floatingWidgetParams.y;
                //get the touch location
                downLayoutX = event.getRawX();
                downLayoutY = event.getRawY();

                handler_longClick.postDelayed(runnable_longClick, 400);

                remove_img_width = removeImageView.getLayoutParams().width;
                remove_img_height = removeImageView.getLayoutParams().height;



                if(collapsedView.getVisibility()==VISIBLE){
                    return true;
                }
                else
                    return false;
            case MotionEvent.ACTION_UP:
                isLongClick = false;
                removeFloatingWidgetView.setVisibility(View.GONE);
                removeImageView.getLayoutParams().height = remove_img_height;
              removeImageView.getLayoutParams().width = remove_img_width;
                handler_longClick.removeCallbacks(runnable_longClick);

                //If user drag and drop the floating widget view into remove view then stop the service
                if (collapsedView.getVisibility()== View.VISIBLE && inBounded) {
                    stopService();
                    inBounded = false;
                    break;
                }
                int x_diff =(int) (downLayoutX - event.getRawX());
                int y_diff =(int)( downLayoutY - event.getRawY());
                if (Math.abs(x_diff) <mTouchSlop && Math.abs(y_diff) < mTouchSlop) {
                    //Also check the difference between start time and end time should be less than 300ms
                    if (( System.currentTimeMillis()- startClickTime) < 300)
                        onFloatingWidgetClick();
                    return true;
                }
                mIsScrolling = false;

               break;
            case MotionEvent.ACTION_MOVE:
                //Calculate the X and Y coordinates of the view.
                floatingWidgetParams.x = downEventX + (int) (event.getRawX() - downLayoutX);

                floatingWidgetParams.y = downEventY + (int) (downLayoutY -event.getRawY());
                //Update the layout with new X & Y coordinate
                mWindowManager.updateViewLayout(this, floatingWidgetParams);
                if (collapsedView.getVisibility()==VISIBLE && isLongClick) {
                    int x_bound_left = szWindow.x / 2 - (int) (remove_img_width * 1.5);
                    int x_bound_right = szWindow.x / 2 + (int) (remove_img_width * 1.5);
                    int y_bound_top = szWindow.y - (int) (remove_img_height * 1.5);

                    //If Floating view comes under Remove View update Window Manager
                    if ((x_cord >= x_bound_left && x_cord <= x_bound_right) && y_cord >= y_bound_top) {
                        inBounded = true;


                        WindowManager.LayoutParams param_remove = (WindowManager.LayoutParams) removeFloatingWidgetView.getLayoutParams();
                        int x_cord_remove = (int) ((szWindow.x - (remove_img_height * 1.5)) / 2);
                        int y_cord_remove = (int) (szWindow.y - ((remove_img_width * 1.5) + getStatusBarHeight()));
                        if (removeImageView.getLayoutParams().height == remove_img_height) {
                            removeImageView.getLayoutParams().height = (int) (remove_img_height * 1.5);
                            removeImageView.getLayoutParams().width = (int) (remove_img_width * 1.5);

                            param_remove.x = x_cord_remove;
                            param_remove.y = y_cord_remove;

                            mWindowManager.updateViewLayout(removeFloatingWidgetView, param_remove);

                        }


                        break;
                    } else {
                        //If Floating window gets out of the Remove view update Remove view again
                        inBounded = false;
                        removeImageView.getLayoutParams().height = remove_img_height;
                        removeImageView.getLayoutParams().width = remove_img_width;

                    }

                }



                return true;
        }

        return false;
    }




    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
       if(collapsedView.getVisibility()==View.VISIBLE) super.onInterceptTouchEvent(ev);

        final int action = ev.getAction();
        // Always handle the case of the touch gesture being complete.
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            // Release the scroll.
            mIsScrolling = false;
            return false; // Do not intercept touch event, let the child handle it
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:

                WindowManager.LayoutParams params = (WindowManager.LayoutParams)getLayoutParams();
                downEventX = params.x;
                downEventY =params.y;
                downLayoutX = ev.getRawX();
                downLayoutY = ev.getRawY();
                startClickTime=System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE: {

                if (mIsScrolling) {
                    // We're currently scrolling, so yes, intercept the
                    // touch event!
                    return true;
                }

                // If the user has dragged her finger horizontally more than
                // the touch slop, start the scroll

                // left as an exercise for the reader

                final int xDiff =(int)(ev.getRawX()- downLayoutX);
                final int yDiff=(int) (ev.getRawY()- downLayoutY);

                // Touch slop should be calculated using ViewConfiguration
                // constants.
                if(System.currentTimeMillis()-startClickTime>500){

                    if (Math.abs(xDiff) > mTouchSlop||Math.abs(yDiff) >mTouchSlop) {

                        // Start scrolling!
                        mIsScrolling = true;
                        return true;
                    }
                }

                break;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    private void onFloatingWidgetClick() {

        if (isViewCollapsed()) {
            //When user clicks on the image view of the collapsed layout,
            //visibility of the collapsed layout will be changed to "View.GONE"
            //and expanded view will become visible.


            if(mPurposeType!=null){
                collapsedView.setVisibility(View.GONE);
                addExpandedWindow(mPurposeType,true);




            }


        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w,h,oldw,oldh);

    }

    private boolean isViewCollapsed() {
        return  findViewById(R.id.collapse_view).getVisibility() == View.VISIBLE;
    }

    public void addExpandedWindow(int whichWindow,boolean showNow){
            boolean isActivityStarted=false;

        switch(whichWindow){
            case PURPOSE_TEXT_MESSAGE:  expandedView=new TextViewWindow(getContext(),mDatabaseReference,textWindowMessage);
                break;
            case PURPOSE_MUTUAL_WATCH:isActivityStarted=true;
                getContext().startActivity(new Intent(getContext(), WatchVideoActivity.class)
                    .putExtra("contact",contact).putExtra("room",room));
            case PURPOSE_CHOOSER:expandedView=new ChoosePurposeWindow(getContext(),new ChoosePurposeWindow.ChangeViewListener(){

                @Override
                public void changeViewTo(int view) {
                    removeView(expandedView);

                     String key=mDatabaseReference.push().getKey();
                    mDatabaseReference=mDatabaseReference.child(key);
                    room =new Room(key,view,INTERACT_VIA_SIM_CALL);
                    mDatabaseReference.child("players").child(phoneNumber).child("currentConnectivityStatus").onDisconnect().setValue(Player.LEFT);
                    room.players.put(phoneNumber,new Player(Player.JOINED,new Contact(null,phoneNumber,uid)));
                    room.players.put(contact.phoneNumber,new Player(NOT_JOINED,contact));
                    WaitingActivity.setGameData(room,null);
                    mDatabaseReference.setValue(room);

                    addExpandedWindow(view,true);
                    writePurposeToPreferences(view);

                }
            });

        }

        DisplayMetrics displayMetrics=new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);

        int[] dimensions=getScreenDimensions(getResources().getDisplayMetrics());


        FrameLayout.LayoutParams params=new FrameLayout.LayoutParams(dimensions[0], LayoutParams.WRAP_CONTENT);
        if(!isActivityStarted){
            addView(expandedView,params);

            removeExpandedView=expandedView.findViewById(R.id.close_button);
            removeExpandedView.setOnClickListener(this);



            if(showNow){
                collapsedView.setVisibility(View.GONE);
                expandedView.setVisibility(View.VISIBLE);
            }
            else{
                collapsedView.setVisibility(View.VISIBLE);
                expandedView.setVisibility(View.GONE);
            }
        }
        else{
            collapsedView.setVisibility(View.GONE);
            expandedView.setVisibility(View.GONE);
        }


    }


    public  void writePurposeToPreferences(int purpose){
        getContext().getSharedPreferences("my_pref",Context.MODE_PRIVATE)
                .edit()
                .putInt("purposeCode",purpose)
                .apply();
    }

    public void setTextWindowMessage(String text){
        textWindowMessage=text;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.close_button:removeView(expandedView);
                collapsedView.setVisibility(VISIBLE);
                params.gravity=Gravity.BOTTOM|Gravity.LEFT;
                params.x=0;
                params.y=200;
                mWindowManager.updateViewLayout(this,params);
                break;

        }

    }

    public void stopService(){
        getContext().stopService(new Intent(getContext(),PhoneCallService.class));
    }


    /*  Reset position of Floating Widget view on dragging  */
    private void resetPosition(int x_cord_now) {
        if (x_cord_now <= szWindow.x / 2) {
            isLeft = true;
            moveToLeft(x_cord_now);
        } else {
            isLeft = false;
            moveToRight(x_cord_now);
        }

    }


    /*  Method to move the Floating widget view to Left  */
    private void moveToLeft(final int current_x_cord) {
        final int x = szWindow.x - current_x_cord;

        new CountDownTimer(500, 5) {
            //get params of Floating Widget view
            WindowManager.LayoutParams mParams = (WindowManager.LayoutParams)getLayoutParams();

            public void onTick(long t) {
                long step = (500 - t) / 5;

                mParams.x = 0 - (int) (current_x_cord * current_x_cord * step);

                //If you want bounce effect uncomment below line and comment above line
                // mParams.x = 0 - (int) (double) bounceValue(step, x);


                //Update window manager for Floating Widget
                mWindowManager.updateViewLayout(MainPhoneCallWindow.this, mParams);
            }

            public void onFinish() {
                mParams.x = 0;

                //Update window manager for Floating Widget
                mWindowManager.updateViewLayout(MainPhoneCallWindow.this, mParams);
            }
        }.start();
    }

    /*  Method to move the Floating widget view to Right  */
    private void moveToRight(final int current_x_cord) {

        new CountDownTimer(500, 5) {
            //get params of Floating Widget view
            WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) getLayoutParams();

            public void onTick(long t) {
                long step = (500 - t) / 5;

                mParams.x = (int) (szWindow.x + (current_x_cord * current_x_cord * step) - getWidth());

                //If you want bounce effect uncomment below line and comment above line
                //  mParams.x = szWindow.x + (int) (double) bounceValue(step, x_cord_now) - mFloatingWidgetView.getWidth();

                //Update window manager for Floating Widget
                mWindowManager.updateViewLayout(MainPhoneCallWindow.this, mParams);
            }

            public void onFinish() {
                mParams.x = szWindow.x -getWidth();

                //Update window manager for Floating Widget
                mWindowManager.updateViewLayout(MainPhoneCallWindow.this, mParams);
            }
        }.start();
    }

    /*  Get Bounce value if you want to make bounce effect to your Floating Widget */
    private double bounceValue(long step, long scale) {
        double value = scale * java.lang.Math.exp(-0.055 * step) * java.lang.Math.cos(0.08 * step);
        return value;
    }


    /*  return status bar height on basis of device display metrics  */
    private int getStatusBarHeight() {
        return (int) Math.ceil(25 * getApplicationContext().getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        getWindowManagerDefaultDisplay();

        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams)getLayoutParams();

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {


            if (layoutParams.y + (getHeight() + getStatusBarHeight()) > szWindow.y) {
                layoutParams.y = szWindow.y - (getHeight() + getStatusBarHeight());
                mWindowManager.updateViewLayout(this, layoutParams);
            }

            if (layoutParams.x != 0 && layoutParams.x < szWindow.x) {
                resetPosition(szWindow.x);
            }

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

            if (layoutParams.x > szWindow.x) {
                resetPosition(szWindow.x);
            }

        }

    }

    private void getWindowManagerDefaultDisplay() {

        mWindowManager.getDefaultDisplay().getSize(szWindow);


    }

    public int getNavBarHeight(Context c) {
        Resources resources = c.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }


    private boolean isTablet(Context c) {
        return (c.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
}
