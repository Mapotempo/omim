package com.mapswithme.maps.widget.mapotempo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapswithme.maps.R;
import com.mapswithme.maps.bookmarks.ChooseBookmarkCategoryFragment;
import com.mapswithme.maps.bookmarks.data.BookmarkCategory;
import com.mapswithme.maps.bookmarks.data.BookmarkManager;

public class MapotempoListManagerFragment extends Fragment
{
  BookmarkCategory mCurrentCategory;

  public static MapotempoListManagerFragment newInstance() {
    return new MapotempoListManagerFragment();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    int categoryIndex = getArguments().getInt(ChooseBookmarkCategoryFragment.CATEGORY_ID, -1);

    if(categoryIndex >= 0)
    {
      mCurrentCategory = BookmarkManager.INSTANCE.getCategory(categoryIndex);
    }
  }

  @Override
  public void onStart()
  {
    super.onStart();

    // Sub Fragment list
    if (getView().findViewById(R.id.mt_layout_fragment_container) != null
        && mCurrentCategory != null)
    {
      MapotempoListFragment firstFragment = new MapotempoListFragment();
      firstFragment.setArguments(getArguments());
      getFragmentManager().beginTransaction().add(R.id.mt_layout_fragment_container, firstFragment).commit();
    }
  }

  @Override
  public void onResume()
  {
    super.onResume();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.mapotempo_list_manager, container, false);
    return view;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
  }
}
