package com.mapswithme.maps.bookmarks.mapotempo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mapswithme.maps.R;
import com.mapswithme.maps.base.BaseMwmRecyclerFragment;
import com.mapswithme.maps.bookmarks.ChooseBookmarkCategoryFragment;
import com.mapswithme.maps.bookmarks.data.BookmarkCategory;
import com.mapswithme.maps.bookmarks.data.BookmarkManager;
import com.mapswithme.maps.bookmarks.data.MTRoutePlanningManager;
import com.mapswithme.maps.dialog.EditTextDialogFragment;
import com.mapswithme.maps.widget.recycler.RecyclerClickListener;
import com.mapswithme.maps.widget.recycler.RecyclerLongClickListener;
import com.mapswithme.util.BottomSheetHelper;
import com.mapswithme.util.sharing.SharingHelper;

public class MapotempoCategoriesFragment extends BaseMwmRecyclerFragment
                                     implements EditTextDialogFragment.OnTextSaveListener,
                                                MenuItem.OnMenuItemClickListener,
                                                RecyclerClickListener,
                                                RecyclerLongClickListener
{
  private int mSelectedPosition;
  private AddButtonController mAddButtonController;

  @Override
  protected @LayoutRes int getLayoutRes()
  {
    return R.layout.fragment_recycler_with_add;
  }

  @Override
  protected RecyclerView.Adapter createAdapter()
  {
    return new MapotempoCategoriesAdapter(getActivity());
  }

  @Override
  protected MapotempoCategoriesAdapter getAdapter()
  {
    return (MapotempoCategoriesAdapter)super.getAdapter();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    return super.onCreateView(inflater, container, savedInstanceState);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState)
  {
    super.onViewCreated(view, savedInstanceState);

    getAdapter().setOnClickListener(this);
    getAdapter().setOnLongClickListener(this);
    mAddButtonController = new AddButtonController(this, view);
  }

  @Override
  public void onResume()
  {
    super.onResume();
    update();
  }

  @Override
  public void onPause()
  {
    super.onPause();
    BottomSheetHelper.free();
  }

  @Override
  public void onSaveText(String text)
  {
    final BookmarkCategory category = BookmarkManager.INSTANCE.getCategory(mSelectedPosition);
    category.setName(text);
    update();
  }

  @Override
  public boolean onMenuItemClick(MenuItem item)
  {
    switch (item.getItemId())
    {
    case R.id.set_show:
      BookmarkManager.INSTANCE.toggleCategoryVisibility(mSelectedPosition);
      update();
      break;

    case R.id.set_share:
      SharingHelper.shareBookmarksCategory(getActivity(), mSelectedPosition);
      break;

    case R.id.set_delete:
      if(MTRoutePlanningManager.INSTANCE.getStatus() == MTRoutePlanningManagerStatus.FOLLOW_PLANNING &&
          MTRoutePlanningManager.INSTANCE.getCurrentBookmarkCategory().getId() == mSelectedPosition)
      {
        MTRoutePlanningManager.INSTANCE.stopRoutingManager();
      }
      BookmarkManager.INSTANCE.nativeDeleteCategory(mSelectedPosition);
      update();
      break;

    case R.id.set_edit:
      EditTextDialogFragment.show(getString(R.string.bookmark_set_name),
                                  BookmarkManager.INSTANCE.getCategory(mSelectedPosition).getName(),
                                  getString(R.string.rename), getString(R.string.cancel), this);
      break;
    }

    return true;
  }

  @Override
  public void onLongItemClick(View v, int position)
  {
    mSelectedPosition = position;

    BookmarkCategory category = BookmarkManager.INSTANCE.getCategory(mSelectedPosition);
    BottomSheetHelper.Builder bs = BottomSheetHelper.create(getActivity(), category.getName())
                                                    .sheet(R.menu.menu_bookmark_categories)
                                                    .listener(this);
    MenuItem show = bs.getMenu().getItem(0);
    show.setIcon(category.isVisible() ? R.drawable.ic_hide
                                      : R.drawable.ic_show);
    show.setTitle(category.isVisible() ? R.string.hide
                                       : R.string.show);
    bs.tint().show();
  }

  @Override
  public void onItemClick(View v, int position)
  {
    startActivity(new Intent(getActivity(), MapotempoListActivity.class)
                      .putExtra(ChooseBookmarkCategoryFragment.CATEGORY_ID, position));
  }

  public void update()
  {
    getAdapter().notifyDataSetChanged();
  }
}
