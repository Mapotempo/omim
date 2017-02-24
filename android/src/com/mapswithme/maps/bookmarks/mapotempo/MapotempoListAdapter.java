package com.mapswithme.maps.bookmarks.mapotempo;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapswithme.maps.R;
import com.mapswithme.maps.bookmarks.data.Bookmark;
import com.mapswithme.maps.bookmarks.data.BookmarkCategory;
import com.mapswithme.maps.bookmarks.data.BookmarkManager;
import com.mapswithme.maps.bookmarks.data.MTRoutePlanning;
import com.mapswithme.maps.bookmarks.data.MTRoutePlanningManager;
import com.mapswithme.maps.widget.placepage.Sponsored;
import com.mapswithme.util.BottomSheetHelper;
import com.mapswithme.util.sharing.ShareOption;
import com.woxthebox.draglistview.DragItemAdapter;

import java.util.ArrayList;
import java.util.List;

public class MapotempoListAdapter extends DragItemAdapter<Integer, MapotempoListAdapter.ViewHolder>
    implements Bookmark.BookmarkParamsChangeListener
{
  private Activity mActivity;

  private BookmarkCategory mCategory;
  private int mLayoutId;
  private int mGrabHandleId;
  private boolean mDragOnLongPress;

  // Information current open category and bookmark status.
  private boolean isCurrentOpenCategory = false;
  private int mCurrentOpenBookmarkIdx = -1;

  private void init(int layoutId, int grabHandleId, boolean dragOnLongPress)
  {
    mLayoutId = layoutId;
    mGrabHandleId = grabHandleId;
    mDragOnLongPress = dragOnLongPress;
    setHasStableIds(true);
    List<Integer>mItemArray = new ArrayList<>();
    setItemList(mItemArray);
  }

  public MapotempoListAdapter(Activity activity, int layoutId, int grabHandleId, boolean dragOnLongPress)
  {
    mActivity = activity;
    init(layoutId, grabHandleId, dragOnLongPress);
    mCategory = null;
  }

  public MapotempoListAdapter(@NonNull Activity activity, @NonNull BookmarkCategory category, int layoutId, int grabHandleId, boolean dragOnLongPress)
  {
    mActivity = activity;
    init (layoutId, grabHandleId, dragOnLongPress);
    mCategory = category;
    reInitList();
  }

  private void reInitList()
  {
    if(mCategory != null)
    {
      if (MTRoutePlanningManager.INSTANCE.getStatus() &&
          (mCategory.getId() == MTRoutePlanningManager.INSTANCE.getCurrentBookmarkCategory().getId()))
      {
        isCurrentOpenCategory = true;
        mCurrentOpenBookmarkIdx = MTRoutePlanning.INSTANCE.getCurrentBookmark(mCategory.getId()).getBookmarkId();
      }

      List<Integer> mItemArray = new ArrayList<>(mCategory.getBookmarksCount());
      for (int i = 0; i < mCategory.getBookmarksCount(); i++)
      {
        mItemArray.add(i);
      }

      setItemList(mItemArray);
    }
  }

  //###############################################################################################
  //  Interface implementation : DragItemAdapter / RecyclerView
  //###############################################################################################

  @Override
  public void onAttachedToRecyclerView(RecyclerView recyclerView)
  {
    super.onAttachedToRecyclerView(recyclerView);
    Bookmark.addBookmarkParamsChangeListener(this);
    //MTRouteListManager.INSTANCE.addCurrentBookmarkChangeListener(this);
  }

  @Override
  public void onDetachedFromRecyclerView(RecyclerView recyclerView)
  {
    super.onDetachedFromRecyclerView(recyclerView);
    Bookmark.removeBookmarkParamsChangeListener(this);
    //MTRouteListManager.INSTANCE.removeCurrentBookmarkChangeListener(this);
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, final int position) {
    if(mCategory == null)
      return;

    super.onBindViewHolder(holder, position);

    final Bookmark bookmark = mCategory.getBookmark(position);
    if(MTRoutePlanningManager.INSTANCE.getStatus() && isCurrentOpenCategory)
    {
      mCurrentOpenBookmarkIdx = MTRoutePlanning.INSTANCE.getCurrentBookmark(mCategory.getId()).getBookmarkId();
    }

    holder.refreshInfo(bookmark, (mCurrentOpenBookmarkIdx == bookmark.getBookmarkId()? true : false));
  }

  @Override
  public long getItemId(int position) {
    return mItemList.get(position);
  }

  //###############################################################################################
  //  Interface implementation : Bookmark.BookmarkParamsChangeListener
  //###############################################################################################

  @Override
  public void onBookmarkParamsChangeListerner(Bookmark bookmark)
  {
    if(mCategory == null)
      return;

    notifyDataSetChanged();
  }

  //###############################################################################################
  //  Interface implementation : RouteListManager.CurrentBookmarkChangeListener
  //###############################################################################################

  /*
  @Override
  public void onCurrentBookmarkChangeListerner(Bookmark currentBookmark)
  {
    if(mCategory == null)
      return;

    if(currentBookmark != null
       && MTRoutePlanningManager.INSTANCE.getStatus()
       && currentBookmark.getCategoryId() == mCategory.getId())
    {
      isCurrentOpenCategory = true;
      mCurrentOpenBookmarkIdx = currentBookmark.getBookmarkId();
    }
    else
    {
      isCurrentOpenCategory = false;
      mCurrentOpenBookmarkIdx = -1;
    }

    notifyDataSetChanged();
  }
  */

  //###############################################################################################
  //  Public function.
  //###############################################################################################

/*  public void updateNativeBookmarkOrder(int fromPosition, int toPosition)
  {
    if(RouteListManager.INSTANCE.getStatus() && (mCategory.getId() == RouteListManager.INSTANCE.getCurrentBookmark().getCategoryId()))
      mCurrentBookmarkIndex = RouteListManager.INSTANCE.getCurrentBookmark().getBookmarkId();
//    notifyDataSetChanged();
//    if(fromPosition == mCurrentBookmarkIndex)
//      mCurrentBookmarkIndex = toPosition;
  }*/

  //###############################################################################################
  //  Public class.
  //###############################################################################################
  public class ViewHolder extends DragItemAdapter.ViewHolder implements MenuItem.OnMenuItemClickListener
  {
    public View mView;
    public TextView mText;
    public ImageView mIcon;
    public FrameLayout mBookmarkMarker;
    public Integer mBookmarkIndex;

    public ViewHolder(final View itemView)
    {
      super(itemView, mGrabHandleId, mDragOnLongPress);
      mView = itemView;
      mText = (TextView) itemView.findViewById(R.id.tv__bookmark_name);
      mIcon = (ImageView) itemView.findViewById(R.id.iv__bookmark_color);
      mBookmarkMarker = (FrameLayout) itemView.findViewById(R.id.bookmark_active_marker);
    }

    @Override
    public void onItemClicked(View view)
    {
      if(mCategory == null)
        return;

      super.onItemClicked(view);

      if(MTRoutePlanningManager.INSTANCE.getStatus() && (mCategory.getId() == MTRoutePlanningManager.INSTANCE.getCurrentBookmarkCategory().getId()))
        BookmarkManager.INSTANCE.nativeShowBookmarkOnMap(mCategory.getId(), mBookmarkIndex);
      notifyDataSetChanged();
    }

    @Override
    public boolean onItemLongClicked(View view)
    {
      super.onItemLongClicked(view);
      BottomSheetHelper.Builder bs = BottomSheetHelper.create(mActivity, (mCategory.getBookmark(mBookmarkIndex)).getTitle())
                                                      .sheet(R.menu.menu_bookmarks)
                                                      .listener(this);
      if (!ShareOption.SMS.isSupported(mActivity))
        bs.getMenu().removeItem(R.id.share_message);

      if (!ShareOption.EMAIL.isSupported(mActivity))
        bs.getMenu().removeItem(R.id.share_email);

      bs.tint().show();
      return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem)
    {
      Bookmark bookmark = mCategory.getBookmark(mBookmarkIndex);

      switch (menuItem.getItemId())
      {
        case R.id.share_message:
          ShareOption.SMS.shareMapObject(mActivity, bookmark, Sponsored.nativeGetCurrent());
          break;

        case R.id.share_email:
          ShareOption.EMAIL.shareMapObject(mActivity, bookmark, Sponsored.nativeGetCurrent());
          break;

        case R.id.share:
          ShareOption.ANY.shareMapObject(mActivity, bookmark, Sponsored.nativeGetCurrent());
          break;

        case R.id.edit:
          //EditBookmarkFragment.editBookmark(mCategory.getId(), mBookmarkIndex, mActivity, getChildFragmentManager());
          break;

        case R.id.delete:
          BookmarkManager.INSTANCE.deleteBookmark(bookmark);
          // update category
          mCategory = BookmarkManager.INSTANCE.getCategory(mCategory.getId());
          reInitList();
          notifyDataSetChanged();
          break;
      }
      return false;
    }

    public void refreshInfo(Bookmark bookmark, boolean isCurrent)
    {
      if(isCurrent)
        mBookmarkMarker.setVisibility(View.VISIBLE);
      else
        mBookmarkMarker.setVisibility(View.INVISIBLE);

      mBookmarkIndex = bookmark.getBookmarkId();
      mText.setText(bookmark.getTitle());
      mIcon.setImageResource(bookmark.getIcon().getSelectedResId());
      mIcon.setOnClickListener(new View.OnClickListener()
      {
        @Override
        public void onClick(View v)
        {
          if(mCategory == null)
            return;

          final Bookmark bm = mCategory.getBookmark(mBookmarkIndex);
          switch (bm.getIcon().getSelectedResId())
          {
            case R.drawable.ic_bookmark_marker_blue_on :
              bm.setParamsAndNotify(bm.getTitle(), BookmarkManager.ICONS.get(6), bm.getBookmarkDescription(), bm.getPhoneNumber());
              break;
            case R.drawable.ic_bookmark_marker_green_on :
              bm.setParamsAndNotify(bm.getTitle(), BookmarkManager.ICONS.get(0), bm.getBookmarkDescription(), bm.getPhoneNumber());
              break;
            case R.drawable.ic_bookmark_marker_red_on :
            default:
              bm.setParamsAndNotify(bm.getTitle(), BookmarkManager.ICONS.get(1), bm.getBookmarkDescription(), bm.getPhoneNumber());
              break;
          }
        }
      });
    }
  }
}
