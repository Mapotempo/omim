package com.mapswithme.maps.widget.mapotempo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.mapswithme.maps.Framework;
import com.mapswithme.maps.MwmActivity;
import com.mapswithme.maps.R;
import com.mapswithme.maps.base.BaseMwmListFragment;
import com.mapswithme.maps.widget.mapotempo.MapotempoListAdapter;
import com.mapswithme.maps.bookmarks.ChooseBookmarkCategoryFragment;
import com.mapswithme.maps.bookmarks.data.Bookmark;
import com.mapswithme.maps.bookmarks.data.BookmarkCategory;
import com.mapswithme.maps.bookmarks.data.BookmarkManager;
import com.mapswithme.maps.bookmarks.data.BookmarkRoutingManager;
import com.mapswithme.maps.bookmarks.data.Track;
import com.mapswithme.maps.widget.placepage.EditBookmarkFragment;
import com.mapswithme.maps.widget.placepage.SponsoredHotel;
import com.mapswithme.util.BottomSheetHelper;
import com.mapswithme.util.sharing.ShareOption;
import com.mapswithme.util.sharing.SharingHelper;

public class MapotempoListFragment extends BaseMwmListFragment
                                implements AdapterView.OnItemLongClickListener,
                                           MenuItem.OnMenuItemClickListener
{
  public static final String TAG = MapotempoListFragment.class.getSimpleName();

  private BookmarkCategory mCategory;
  private int mSelectedPosition;
  private MapotempoListAdapter mAdapter;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    if(BookmarkRoutingManager.INSTANCE.nativeGetStatus())
    {
      mCategory = BookmarkManager.INSTANCE.getCategory(BookmarkRoutingManager.INSTANCE.getCurrentBookmark().getCategoryId());
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
  {
    return inflater.inflate(R.layout.simple_list, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState)
  {
    super.onViewCreated(view, savedInstanceState);
    initList();
    setHasOptionsMenu(true);
  }

  @Override
  public void onResume()
  {
    super.onResume();

    if(BookmarkRoutingManager.INSTANCE.nativeGetStatus())
    {
      if(mCategory == null || mCategory.getId() != BookmarkRoutingManager.INSTANCE.getCurrentBookmark().getCategoryId())
      {
        mCategory = BookmarkManager.INSTANCE.getCategory(BookmarkRoutingManager.INSTANCE.getCurrentBookmark().getCategoryId());
        initList();
      }
      else
      {
        mAdapter.startTimerUpdate();
      }
    }
  }

  @Override
  public void onPause()
  {
    super.onPause();

    if(mCategory != null)
    {
      mAdapter.stopTimerUpdate();
    }
  }

  private void initList()
  {
    if(mCategory != null)
    {
      mAdapter = new MapotempoListAdapter(getActivity(), mCategory);
      mAdapter.startTimerUpdate();
      setListAdapter(mAdapter);
      getListView().setOnItemLongClickListener(this);
    }
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id)
  {
    final Bookmark bookmark = (Bookmark) mAdapter.getItem(position);
    BookmarkRoutingManager.INSTANCE.initRoutingManager(bookmark.getCategoryId(), bookmark.getBookmarkId());
  }

  @Override
  public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
  {
    mSelectedPosition = position;
    final Object item = mAdapter.getItem(mSelectedPosition);

    BottomSheetHelper.Builder bs = BottomSheetHelper.create(getActivity(), ((Bookmark) item).getTitle())
                                                    .sheet(R.menu.menu_bookmarks)
                                                    .listener(this);
    if (!ShareOption.SMS.isSupported(getActivity()))
      bs.getMenu().removeItem(R.id.share_message);

    if (!ShareOption.EMAIL.isSupported(getActivity()))
      bs.getMenu().removeItem(R.id.share_email);

    bs.tint().show();

    return true;
  }

  @Override
  public boolean onMenuItemClick(MenuItem menuItem)
  {
    Bookmark item = (Bookmark) mAdapter.getItem(mSelectedPosition);

    switch (menuItem.getItemId())
    {
    case R.id.share_message:
      ShareOption.SMS.shareMapObject(getActivity(), item, SponsoredHotel.nativeGetCurrent());
      break;

    case R.id.share_email:
      ShareOption.EMAIL.shareMapObject(getActivity(), item, SponsoredHotel.nativeGetCurrent());
      break;

    case R.id.share:
      ShareOption.ANY.shareMapObject(getActivity(), item, SponsoredHotel.nativeGetCurrent());
      break;

    case R.id.edit:
      EditBookmarkFragment.editBookmark(mCategory.getId(), item.getBookmarkId(), getActivity(), getChildFragmentManager());
      break;

    case R.id.delete:
      BookmarkManager.INSTANCE.deleteBookmark(item);
      mAdapter.notifyDataSetChanged();
      break;
    }
    return false;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
  {
    inflater.inflate(R.menu.option_menu_bookmarks, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    if (item.getItemId() == R.id.set_share)
    {
      SharingHelper.shareBookmarksCategory(getActivity(), mCategory.getId());
      return true;
    }

    return super.onOptionsItemSelected(item);
  }
}
