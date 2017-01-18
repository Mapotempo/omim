package com.mapswithme.maps.widget.mapotempo;

import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.v4.app.Fragment;

import com.mapswithme.maps.base.BaseToolbarActivity;
import com.mapswithme.util.ThemeUtils;

public class MapotempoListActivity extends BaseToolbarActivity
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
    return MapotempoListManagerFragment.class;
  }

  @Override
  public void onBackPressed()
  {
    MapotempoListManagerFragment toto = (MapotempoListManagerFragment)getSupportFragmentManager().findFragmentById(getFragmentContentResId());
    if(toto.onBackPressed())
      return;

    super.onBackPressed();
  }
}
