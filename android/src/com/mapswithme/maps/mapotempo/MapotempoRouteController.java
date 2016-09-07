package com.mapswithme.maps.mapotempo;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapswithme.maps.Framework;
import com.mapswithme.maps.MwmActivity;
import com.mapswithme.maps.R;
import com.mapswithme.maps.bookmarks.BookmarkListActivity;
import com.mapswithme.maps.bookmarks.ChooseBookmarkCategoryFragment;
import com.mapswithme.maps.bookmarks.data.Bookmark;
import com.mapswithme.maps.bookmarks.data.BookmarkCategory;
import com.mapswithme.maps.bookmarks.data.BookmarkManager;
import com.mapswithme.maps.bookmarks.data.BookmarkRoutingManager;
import com.mapswithme.maps.bookmarks.data.Icon;
import com.mapswithme.maps.routing.RoutingController;
import com.mapswithme.util.statistics.AlohaHelper;
import com.mapswithme.util.statistics.Statistics;

import java.util.ArrayList;
import java.util.List;

public class MapotempoRouteController
{
  public static final List<Icon> STATUS = new ArrayList<>();

  static {
    STATUS.add(new Icon("placemark-red", "placemark-red", R.drawable.ic_bookmark_marker_red_off, R.drawable.ic_point_fail));
    STATUS.add(new Icon("placemark-blue", "placemark-blue", R.drawable.ic_bookmark_marker_blue_off, R.drawable.ic_point_todo));
    STATUS.add(new Icon("placemark-green", "placemark-green", R.drawable.ic_bookmark_marker_green_off, R.drawable.ic_point_done));
  }

  private final View mBottomMapotempoFrame;

  // MAPOTEMPO UI ROUTING
  private ImageButton mMTNextBM;
  private ImageButton mMTPrevBM;
  private ImageButton mMTActionLeft;
  private ImageButton mMTActionLeftSecond;
  private ImageButton mMTActionRight;
  private TextView mMTCurrentBM;

  public MapotempoRouteController(final Activity activity) {
    mBottomMapotempoFrame = activity.findViewById(R.id.nav_mapotempo_bottom_frame);

    mMTNextBM = (ImageButton) mBottomMapotempoFrame.findViewById(R.id.mt_nxt_bm);
    mMTPrevBM = (ImageButton) mBottomMapotempoFrame.findViewById(R.id.mt_prv_bm);
    mMTActionLeft = (ImageButton) mBottomMapotempoFrame.findViewById(R.id.mt_action_left);
    mMTActionLeftSecond = (ImageButton) mBottomMapotempoFrame.findViewById(R.id.mt_action_left_second);
    mMTActionRight = (ImageButton) mBottomMapotempoFrame.findViewById(R.id.mt_action_right);
    mMTCurrentBM = (TextView) mBottomMapotempoFrame.findViewById(R.id.mt_current_bm);

    mMTNextBM.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Bookmark bookmark = BookmarkRoutingManager.INSTANCE.stepNextBookmark();
        BookmarkManager.INSTANCE.nativeMoveToBookmarkOnMap(bookmark.getCategoryId(), bookmark.getBookmarkId());
        refreshUI(bookmark);
      }
    });

    mMTPrevBM.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Bookmark bookmark = BookmarkRoutingManager.INSTANCE.stepPreviousBookmark();
        BookmarkManager.INSTANCE.nativeMoveToBookmarkOnMap(bookmark.getCategoryId(), bookmark.getBookmarkId());
        refreshUI(bookmark);
      }
    });

    mMTCurrentBM.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Bookmark bookmark = BookmarkRoutingManager.INSTANCE.getCurrentBookmark();
        BookmarkManager.INSTANCE.nativeShowBookmarkOnMap(bookmark.getCategoryId(), bookmark.getBookmarkId());
        refreshUI(bookmark);
      }
    });

    mMTActionRight.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        ImageButton button = (ImageButton) v;
        Bookmark bookmark = BookmarkRoutingManager.INSTANCE.getCurrentBookmark();
        switch ((Integer) button.getTag()) {
          case R.drawable.ic_point_todo:
            bookmark.setParams(bookmark.getTitle(), BookmarkManager.ICONS.get(1), bookmark.getBookmarkDescription());
            break;
          case R.drawable.ic_point_done:
            bookmark.setParams(bookmark.getTitle(), BookmarkManager.ICONS.get(6), bookmark.getBookmarkDescription());
            break;
          case R.drawable.ic_point_fail:
          default:
            bookmark.setParams(bookmark.getTitle(), BookmarkManager.ICONS.get(0), bookmark.getBookmarkDescription());
            break;
        }
        // Get the bookmark refresh
        bookmark = BookmarkRoutingManager.INSTANCE.getCurrentBookmark();
        refreshUI(bookmark);
      }
    });

    mMTActionLeft.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Bookmark bookmark = BookmarkRoutingManager.INSTANCE.getCurrentBookmark();
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

    mMTActionLeftSecond.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
      Bookmark bookmark = BookmarkRoutingManager.INSTANCE.getCurrentBookmark();

        activity.startActivity(new Intent(activity, BookmarkListActivity.class)
            .putExtra(ChooseBookmarkCategoryFragment.CATEGORY_ID, bookmark.getCategoryId()));
      }
    });

    if(BookmarkRoutingManager.INSTANCE.getStatus())
    {
      showMapotempoRoutePanel(true);
    }
  }

  public void showMapotempoRoutePanel(boolean visibility)
  {
    if(visibility) {
      mBottomMapotempoFrame.setVisibility(View.VISIBLE);
      Bookmark bookmark = BookmarkRoutingManager.INSTANCE.getCurrentBookmark();
      refreshUI(bookmark);
    }
    else
    {
      mBottomMapotempoFrame.setVisibility(View.GONE);
    }
  }


  public void refreshUI(Bookmark currentBm)
  {
    if(BookmarkRoutingManager.INSTANCE.getStatus()) {
      mBottomMapotempoFrame.setVisibility(View.VISIBLE);
      mMTCurrentBM.setText(currentBm.getTitle());
      Icon icon = currentBm.getIcon();
      if(icon.getType().equals("placemark-red"))
      {
        mMTActionRight.setImageResource(R.drawable.ic_point_fail);
        mMTActionRight.setTag(R.drawable.ic_point_todo);
      }
      else if(icon.getType().equals("placemark-green"))
      {
        mMTActionRight.setImageResource(R.drawable.ic_point_done);
        mMTActionRight.setTag(R.drawable.ic_point_fail);
      }
      else
      {
        mMTActionRight.setImageResource(R.drawable.ic_point_todo);
        mMTActionRight.setTag(R.drawable.ic_point_done);
      }
    }
  }
}

