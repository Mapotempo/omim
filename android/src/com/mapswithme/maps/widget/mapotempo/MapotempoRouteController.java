package com.mapswithme.maps.widget.mapotempo;

import android.app.Activity;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mapswithme.maps.MwmActivity;
import com.mapswithme.maps.R;
import com.mapswithme.maps.bookmarks.BookmarkCategoriesActivity;
import com.mapswithme.maps.bookmarks.data.Bookmark;
import com.mapswithme.maps.bookmarks.data.BookmarkManager;
import com.mapswithme.maps.bookmarks.data.RouteListManager;
import com.mapswithme.maps.bookmarks.data.Icon;
import com.mapswithme.maps.routing.RoutingController;
import com.mapswithme.maps.widget.menu.MapotempoMenu;
import com.mapswithme.util.statistics.AlohaHelper;
import com.mapswithme.util.statistics.Statistics;
import com.woxthebox.draglistview.DragListView;

import java.util.ArrayList;
import java.util.List;

public class MapotempoRouteController implements Bookmark.BookmarkParamsChangeListener
{
  public static final List<Icon> STATUS = new ArrayList<>();

  static {
    STATUS.add(new Icon("placemark-red", "placemark-red", R.drawable.ic_bookmark_marker_red_off, R.drawable.ic_point_fail));
    STATUS.add(new Icon("placemark-blue", "placemark-blue", R.drawable.ic_bookmark_marker_blue_off, R.drawable.ic_point_todo));
    STATUS.add(new Icon("placemark-green", "placemark-green", R.drawable.ic_bookmark_marker_green_off, R.drawable.ic_point_done));
  }

  private final View mBottomMapotempoFrame;

  private Activity mActivity;

  // MAPOTEMPO UI ROUTING
  private ImageButton mMTNextBM;
  private ImageButton mMTPrevBM;
  private ImageButton mMTActionLeft;
  private ImageButton mMTActionLeftSecond;
  private ImageButton mMTActionRight;
  private TextView mMTCurrentBM;
  private Button mapotempoStartRoute;
  private LinearLayout mLineFrame;
  private MapotempoMenu mapotempoMenu;
  private ProgressBar mOptimProgressBar;

  public MapotempoRouteController(final Activity activity)
  {
    mActivity = activity;

    mBottomMapotempoFrame = activity.findViewById(R.id.nav_mapotempo_bottom_frame);

    mLineFrame = (LinearLayout) mBottomMapotempoFrame.findViewById(R.id.line_frame);
    mMTNextBM = (ImageButton) mBottomMapotempoFrame.findViewById(R.id.mt_nxt_bm);
    mMTPrevBM = (ImageButton) mBottomMapotempoFrame.findViewById(R.id.mt_prv_bm);
    mMTActionLeft = (ImageButton) mBottomMapotempoFrame.findViewById(R.id.mt_action_left);
    mMTActionLeftSecond = (ImageButton) mBottomMapotempoFrame.findViewById(R.id.mt_action_left_second);
    mMTActionRight = (ImageButton) mBottomMapotempoFrame.findViewById(R.id.mt_action_right);
    mMTCurrentBM = (TextView) mBottomMapotempoFrame.findViewById(R.id.mt_current_bm);
    mapotempoStartRoute = (Button) mBottomMapotempoFrame.findViewById(R.id.mt_route_start);
    mOptimProgressBar = (ProgressBar) mBottomMapotempoFrame.findViewById(R.id.mt_optim_progress_bar);
    mOptimProgressBar.setMax(100);

    mapotempoStartRoute.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        activity.startActivity(new Intent(activity, BookmarkCategoriesActivity.class));
      }
    });

    mapotempoMenu = new MapotempoMenu(mBottomMapotempoFrame, new MapotempoMenu.ItemClickListener<MapotempoMenu.Item>(){
      @Override
      public void onItemClick(MapotempoMenu.Item item)
      {
        final MwmActivity parent = ((MwmActivity) mBottomMapotempoFrame.getContext());

        switch (item)
        {
          case TOGGLE:
            mapotempoMenu.toggle(false);
            parent.refreshFade();
            break;
          default:
            break;
        }
      }
    });

    mMTNextBM.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Bookmark bookmark = RouteListManager.INSTANCE.stepNextBookmark();
        BookmarkManager.INSTANCE.nativeShowBookmarkOnMap(bookmark.getCategoryId(), bookmark.getBookmarkId());
        refreshUI(bookmark);
      }
    });

    mMTPrevBM.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Bookmark bookmark = RouteListManager.INSTANCE.stepPreviousBookmark();
        BookmarkManager.INSTANCE.nativeShowBookmarkOnMap(bookmark.getCategoryId(), bookmark.getBookmarkId());
        refreshUI(bookmark);
      }
    });

    mMTCurrentBM.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Bookmark bookmark = RouteListManager.INSTANCE.getCurrentBookmark();
        BookmarkManager.INSTANCE.nativeShowBookmarkOnMap(bookmark.getCategoryId(), bookmark.getBookmarkId());
        refreshUI(bookmark);
      }
    });

    mMTActionRight.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        ImageButton button = (ImageButton) v;
        Bookmark bookmark = RouteListManager.INSTANCE.getCurrentBookmark();
        switch (bookmark.getIcon().getSelectedResId())
        {
          case R.drawable.ic_bookmark_marker_blue_on :
            bookmark.setParamsAndNotify(bookmark.getTitle(), BookmarkManager.ICONS.get(6), bookmark.getBookmarkDescription(), bookmark.getPhoneNumber());
            break;
          case R.drawable.ic_bookmark_marker_green_on :
            bookmark.setParamsAndNotify(bookmark.getTitle(), BookmarkManager.ICONS.get(0), bookmark.getBookmarkDescription(), bookmark.getPhoneNumber());
            break;
          case R.drawable.ic_bookmark_marker_red_on :
          default:
            bookmark.setParamsAndNotify(bookmark.getTitle(), BookmarkManager.ICONS.get(1), bookmark.getBookmarkDescription(), bookmark.getPhoneNumber());
            break;
        }
        // Get the bookmark refresh
        bookmark = RouteListManager.INSTANCE.getCurrentBookmark();
        refreshUI(bookmark);
      }
    });

    mMTActionLeft.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Bookmark bookmark = RouteListManager.INSTANCE.getCurrentBookmark();
        if (RoutingController.get().isPlanning() && bookmark != null)
        {
          RoutingController.get().setEndPoint(bookmark);
        }
        else
        {
          ((MwmActivity)activity).startLocationToPoint(Statistics.EventName.PP_ROUTE, AlohaHelper.PP_ROUTE, bookmark);
        }
      }
    });

    mMTActionLeftSecond.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        mOptimProgressBar.setProgress(0);
        mOptimProgressBar.setVisibility(View.VISIBLE);
        RouteListManager.nativeOptimiseBookmarkCategory(RouteListManager.INSTANCE.getCurrentBookmark().getCategoryId());
      }
    });

    Bookmark.addBookmarkParamsChangeListener(this);
  }

  public void showMapotempoRoutePanel(boolean visibility)
  {
    if(visibility && RouteListManager.INSTANCE.getStatus()) {
      mLineFrame.setVisibility(View.VISIBLE);
      mapotempoStartRoute.setVisibility(View.GONE);
      Bookmark bookmark = RouteListManager.INSTANCE.getCurrentBookmark();
      refreshUI(bookmark);
    }
    else
    {
      mLineFrame.setVisibility(View.GONE);
      mapotempoStartRoute.setVisibility(View.VISIBLE);
      mapotempoMenu.close(false);
    }
  }


  public void refreshUI(Bookmark currentBm)
  {
    if(RouteListManager.INSTANCE.getStatus()) {
      mBottomMapotempoFrame.setVisibility(View.VISIBLE);
      mMTCurrentBM.setText(currentBm.getTitle());
      mMTActionRight.setImageResource(currentBm.getIcon().getSelectedResId());
      RouteListManager.INSTANCE.notifyCurrentBookmarkChange();
    }
  }

  @Override
  public void onBookmarkParamsChangeListerner(Bookmark bookmark)
  {
    if(RouteListManager.INSTANCE.getStatus())
    {
      Bookmark cuBookmark = RouteListManager.INSTANCE.getCurrentBookmark();
      if(cuBookmark != null &&
         bookmark.getBookmarkId() == cuBookmark.getBookmarkId())
      {
        refreshUI(bookmark);
      }
    }
  }

  // Attention ses méthodes appelles du code thread on le racroche ton au thread ui !!!!
  // FIXME A l'arrache !!!!! A refaire
  boolean v;
  public void optimProgressBarVisibility(boolean visibility)
  {
    v = visibility;
    mActivity.runOnUiThread(new Runnable()
    {
      @Override
      public void run()
      {
        mOptimProgressBar.setVisibility(v ? View.VISIBLE : View.GONE);
      }
    });
  }

  int p;
  public void setOptimProgress(int value)
  {
    p = value;
    mActivity.runOnUiThread(new Runnable()
    {
      @Override
      public void run()
      {
        mOptimProgressBar.setProgress(p);
      }
    });
  }
}

