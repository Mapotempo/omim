package com.mapswithme.maps.bookmarks.mapotempo;

import android.support.design.widget.FloatingActionButton;
import android.view.View;

import com.mapswithme.maps.R;
import com.mapswithme.maps.bookmarks.data.BookmarkManager;

public class AddButtonController
{
  interface AddItemClickListener
  {
    void onAddItemClick(int position);
  }

  private final MapotempoCategoriesFragment mFragment;
  private final FloatingActionButton mFab;
  private AddItemClickListener mAddItemClickListener;

  AddButtonController(MapotempoCategoriesFragment fragment, View frame)
  {
    mFragment = fragment;
    mFab = (FloatingActionButton) frame.findViewById(R.id.fab);
    if(mFab == null || !mFab.getClass().equals(FloatingActionButton.class))
      throw new NullPointerException("An object type FloatingActionButton with a id \"R.id.fab\" could't be found in the view.");

    mFab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v)
      {
        int newIndex = BookmarkManager.INSTANCE.nativeCreateCategory(mFragment.getString(R.string.new_group));
        mFragment.update();
        mAddItemClickListener.onAddItemClick(newIndex);
      }
    });

    mAddItemClickListener = new AddItemClickListener() {
      @Override
      public void onAddItemClick(int position)
      {
      }
    };
  }

  void setOnAddItemClickView(AddItemClickListener listener)
  {
    mAddItemClickListener = listener;
  }

}
