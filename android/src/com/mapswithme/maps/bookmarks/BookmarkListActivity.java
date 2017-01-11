package com.mapswithme.maps.bookmarks;

import android.support.v4.app.Fragment;
import com.mapswithme.maps.base.BaseToolbarActivity;
import com.mapswithme.maps.widget.mapotempo.MapotempoListManagerFragment;

public class BookmarkListActivity extends BaseToolbarActivity
{
  @Override
  protected Class<? extends Fragment> getFragmentClass()
  {
    return MapotempoListManagerFragment.class;
    //return BookmarksListFragment.class;
  }
}
