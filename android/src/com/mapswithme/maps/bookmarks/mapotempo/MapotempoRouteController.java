package com.mapswithme.maps.bookmarks.mapotempo;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapswithme.maps.MwmActivity;
import com.mapswithme.maps.R;
import com.mapswithme.maps.bookmarks.data.Bookmark;
import com.mapswithme.maps.bookmarks.data.BookmarkManager;
import com.mapswithme.maps.bookmarks.data.Icon;
import com.mapswithme.maps.bookmarks.data.MTRoutePlanning;
import com.mapswithme.maps.bookmarks.data.MTRoutePlanningManager;
import com.mapswithme.maps.widget.menu.MapotempoMenu;

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
  private ImageView mMTNextBM;
  private ImageView mMTPrevBM;
  //private ImageView mMTActionLeft;
  private ImageButton mMTActionRight;
  private TextView mMTCurrentBM;
  private Button mapotempoStartRoute;
  private LinearLayout mLineFrame;
  private MapotempoMenu mapotempoMenu;

  public MapotempoRouteController(final Activity activity)
  {
    mActivity = activity;

    mBottomMapotempoFrame = activity.findViewById(R.id.nav_mapotempo_bottom_frame);

    mLineFrame = (LinearLayout) mBottomMapotempoFrame.findViewById(R.id.line_frame);
    mMTNextBM = (ImageView) mBottomMapotempoFrame.findViewById(R.id.mt_nxt_bm);
    mMTPrevBM = (ImageView) mBottomMapotempoFrame.findViewById(R.id.mt_prv_bm);
    //mMTActionLeft = (ImageView) mBottomMapotempoFrame.findViewById(R.id.mt_action_left);
    mMTActionRight = (ImageButton) mBottomMapotempoFrame.findViewById(R.id.mt_action_right);
    mMTCurrentBM = (TextView) mBottomMapotempoFrame.findViewById(R.id.mt_current_bm);
    //mapotempoStartRoute = (Button) mBottomMapotempoFrame.findViewById(R.id.mt_route_start);

//    mapotempoStartRoute.setOnClickListener(new View.OnClickListener() {
//      @Override
//      public void onClick(View v) {
//        activity.startActivity(new Intent(activity, BookmarkCategoriesActivity.class));
//      }
//    });

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
            parent.closePlacePage();
            break;
          default:
            break;
        }
      }
    });

    mMTNextBM.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        int catId = MTRoutePlanningManager.INSTANCE.getCurrentBookmarkCategory().getId();
        Bookmark bookmark = MTRoutePlanning.INSTANCE.stepNextBookmark(catId);
        BookmarkManager.INSTANCE.nativeShowBookmarkOnMap(bookmark.getCategoryId(), bookmark.getBookmarkId());
        refreshUI(bookmark);
      }
    });

    mMTPrevBM.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        int catId = MTRoutePlanningManager.INSTANCE.getCurrentBookmarkCategory().getId();
        Bookmark bookmark = MTRoutePlanning.INSTANCE.stepPreviousBookmark(catId);
        BookmarkManager.INSTANCE.nativeShowBookmarkOnMap(bookmark.getCategoryId(), bookmark.getBookmarkId());
        refreshUI(bookmark);
      }
    });

    mMTCurrentBM.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        int catId = MTRoutePlanningManager.INSTANCE.getCurrentBookmarkCategory().getId();
        Bookmark bookmark = MTRoutePlanning.INSTANCE.getCurrentBookmark(catId);
        BookmarkManager.INSTANCE.nativeShowBookmarkOnMap(bookmark.getCategoryId(), bookmark.getBookmarkId());
        refreshUI(bookmark);
      }
    });

    mMTActionRight.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        ImageButton button = (ImageButton) v;
        int catId = MTRoutePlanningManager.INSTANCE.getCurrentBookmarkCategory().getId();
        Bookmark bookmark = MTRoutePlanning.INSTANCE.getCurrentBookmark(catId);
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
        bookmark = MTRoutePlanning.INSTANCE.getCurrentBookmark(catId);
        refreshUI(bookmark);
      }
    });

//    mMTActionLeft.setOnClickListener(new View.OnClickListener() {
//      @Override
//      public void onClick(View v) {
//        Bookmark bookmark = RouteListManager.INSTANCE.getCurrentBookmark();
//        if (RoutingController.get().isPlanning() && bookmark != null)
//        {
//          RoutingController.get().setEndPoint(bookmark);
//        }
//        else
//        {
//          ((MwmActivity)activity).startLocationToPoint(Statistics.EventName.PP_ROUTE, AlohaHelper.PP_ROUTE, bookmark);
//        }
//      }
//    });

    Bookmark.addBookmarkParamsChangeListener(this);
  }

  public void showMapotempoRoutePanel(boolean visibility)
  {
    if(visibility && MTRoutePlanningManager.INSTANCE.getStatus()) {
      mLineFrame.setVisibility(View.VISIBLE);
      int catId = MTRoutePlanningManager.INSTANCE.getCurrentBookmarkCategory().getId();
      Bookmark bookmark = MTRoutePlanning.INSTANCE.getCurrentBookmark(catId);
      refreshUI(bookmark);
    }
    else
    {
      mLineFrame.setVisibility(View.GONE);
      mapotempoMenu.close(false);
      mBottomMapotempoFrame.setVisibility(View.GONE);
    }
  }


  public void refreshUI(Bookmark currentBm)
  {
    if(MTRoutePlanningManager.INSTANCE.getStatus()) {
      mBottomMapotempoFrame.setVisibility(View.VISIBLE);
      mMTCurrentBM.setText(currentBm.getTitle());
      mMTActionRight.setImageResource(currentBm.getIcon().getSelectedResId());
    }
  }

  @Override
  public void onBookmarkParamsChangeListerner(Bookmark bookmark)
  {
    if(MTRoutePlanningManager.INSTANCE.getStatus())
    {
      int catId = MTRoutePlanningManager.INSTANCE.getCurrentBookmarkCategory().getId();
      Bookmark cuBm = MTRoutePlanning.INSTANCE.getCurrentBookmark(catId);
      if(bookmark != null &&
         bookmark.getBookmarkId() == cuBm.getBookmarkId())
      {
        refreshUI(bookmark);
      }
    }
  }
}

