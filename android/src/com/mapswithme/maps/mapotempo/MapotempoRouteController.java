package com.mapswithme.maps.mapotempo;

import android.app.Activity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mapswithme.maps.R;
import com.mapswithme.maps.bookmarks.data.Bookmark;
import com.mapswithme.maps.bookmarks.data.BookmarkManager;
import com.mapswithme.maps.bookmarks.data.BookmarkRoutingManager;

public class MapotempoRouteController
{
  private final View mBottomMapotempoFrame;

  // MAPOTEMPO UI ROUTING
  private ImageButton mMPNextBM;
  private ImageButton mMPPrevBM;
  private TextView    mMPCurrentBM;

  public MapotempoRouteController(Activity activity)
  {
    mBottomMapotempoFrame = activity.findViewById(R.id.nav_mapotempo_bottom_frame);

    mMPNextBM = (ImageButton)  mBottomMapotempoFrame.findViewById(R.id.mp_nxt_bm);
    mMPPrevBM = (ImageButton)  mBottomMapotempoFrame.findViewById(R.id.mp_prv_bm);
    mMPCurrentBM = (TextView)  mBottomMapotempoFrame.findViewById(R.id.mp_current_bm);

    if(BookmarkRoutingManager.INSTANCE.getStatus()) {
      mBottomMapotempoFrame.setVisibility(View.VISIBLE);
      Bookmark bookmark = BookmarkRoutingManager.INSTANCE.getCurrentBookmark();
      mMPCurrentBM.setText(bookmark.getTitle());
    }

    mMPNextBM.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Bookmark bookmark = BookmarkRoutingManager.INSTANCE.stepNextBookmark();
        mMPCurrentBM.setText(bookmark.getTitle());
        BookmarkManager.INSTANCE.nativeShowBookmarkOnMap(bookmark.getCategoryId(), bookmark.getBookmarkId());
      }
    });

    mMPPrevBM.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Bookmark bookmark = BookmarkRoutingManager.INSTANCE.stepPreviousBookmark();
        mMPCurrentBM.setText(bookmark.getTitle());
        BookmarkManager.INSTANCE.nativeShowBookmarkOnMap(bookmark.getCategoryId(), bookmark.getBookmarkId());
      }
    });

    mMPCurrentBM.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Bookmark bookmark = BookmarkRoutingManager.INSTANCE.getCurrentBookmark();
        mMPCurrentBM.setText(bookmark.getTitle());
        BookmarkManager.INSTANCE.nativeShowBookmarkOnMap(bookmark.getCategoryId(), bookmark.getBookmarkId());
      }
    });
  }

  public void showMapotempoRouteInfo(boolean visibility)
  {
    if(visibility) {
      mBottomMapotempoFrame.setVisibility(View.VISIBLE);
      Bookmark bookmark = BookmarkRoutingManager.INSTANCE.getCurrentBookmark();
      mMPCurrentBM.setText(bookmark.getTitle());
      BookmarkManager.INSTANCE.nativeShowBookmarkOnMap(bookmark.getCategoryId(), bookmark.getBookmarkId());
    }
    else
    {
      mBottomMapotempoFrame.setVisibility(View.GONE);
    }
  }
}
