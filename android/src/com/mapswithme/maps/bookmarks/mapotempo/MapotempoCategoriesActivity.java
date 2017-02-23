package com.mapswithme.maps.bookmarks.mapotempo;

import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.v4.app.Fragment;

import com.mapswithme.maps.base.BaseToolbarActivity;
import com.mapswithme.util.ThemeUtils;

public class MapotempoCategoriesActivity extends BaseToolbarActivity
{
  @Override
  @StyleRes
  public int getThemeResourceId(@NonNull String theme)
  {
    return ThemeUtils.getCardBgThemeResourceId(theme);
  }

  @Override
  protected Class<? extends Fragment> getFragmentClass()
  {
    return MapotempoCategoriesFragment.class;
  }
}
