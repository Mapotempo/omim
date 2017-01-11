package com.mapswithme.maps.widget.mapotempo;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapswithme.maps.R;
import com.mapswithme.maps.bookmarks.data.Bookmark;
import com.mapswithme.maps.bookmarks.data.BookmarkCategory;
import com.mapswithme.maps.bookmarks.data.BookmarkManager;
import com.mapswithme.maps.bookmarks.data.RouteListManager;
import com.woxthebox.draglistview.DragItemAdapter;

import java.util.ArrayList;
import java.util.List;

public class MapotempoListAdapter extends DragItemAdapter<Integer, MapotempoListAdapter.ViewHolder>
    implements Bookmark.BookmarkParamsChangeListener
    ,RouteListManager.CurrentBookmarkChangeListener
{
  private BookmarkCategory mCategory;
  private int mLayoutId;
  private int mGrabHandleId;
  private boolean mDragOnLongPress;

  // Information current open category and bookmark status.
  private int mCurrentOpenBookmarkIdx = -1;
  private int mCurrentOpenCategoryIdx = -1;

  private void init(int layoutId, int grabHandleId, boolean dragOnLongPress)
  {
    mLayoutId = layoutId;
    mGrabHandleId = grabHandleId;
    mDragOnLongPress = dragOnLongPress;
    setHasStableIds(true);
    List<Integer>mItemArray = new ArrayList<>();
    setItemList(mItemArray);
  }

  public MapotempoListAdapter(int layoutId, int grabHandleId, boolean dragOnLongPress)
  {
    init(layoutId, grabHandleId, dragOnLongPress);
    mCategory = null;
  }

  public MapotempoListAdapter(@NonNull BookmarkCategory category, int layoutId, int grabHandleId, boolean dragOnLongPress)
  {
    init (layoutId, grabHandleId, dragOnLongPress);

    mCategory = category;

    if(RouteListManager.INSTANCE.getStatus() && (mCategory.getId() == RouteListManager.INSTANCE.getCurrentBookmark().getCategoryId()))
    {
      mCurrentOpenBookmarkIdx = RouteListManager.INSTANCE.getCurrentBookmark().getBookmarkId();
      mCurrentOpenCategoryIdx = RouteListManager.INSTANCE.getCurrentBookmark().getCategoryId();
    }

    List<Integer>mItemArray = new ArrayList<>(mCategory.getBookmarksCount());
    for (int i = 0; i < mCategory.getBookmarksCount(); i++)
    {
      mItemArray.add(i);
    }

    setItemList(mItemArray);
  }

  //###############################################################################################
  //  Interface implementation : DragItemAdapter / RecyclerView
  //###############################################################################################

  @Override
  public void onAttachedToRecyclerView(RecyclerView recyclerView)
  {
    super.onAttachedToRecyclerView(recyclerView);
    Bookmark.addBookmarkParamsChangeListener(this);
    RouteListManager.INSTANCE.addCurrentBookmarkChangeListener(this);
  }

  @Override
  public void onDetachedFromRecyclerView(RecyclerView recyclerView)
  {
    super.onDetachedFromRecyclerView(recyclerView);
    Bookmark.removeBookmarkParamsChangeListener(this);
    RouteListManager.INSTANCE.removeCurrentBookmarkChangeListener(this);
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
    if(mCurrentOpenCategoryIdx == mCategory.getId())
    {
      mCurrentOpenBookmarkIdx = RouteListManager.INSTANCE.getCurrentBookmark().getBookmarkId();
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

  @Override
  public void onCurrentBookmarkChangeListerner(Bookmark currentBookmark)
  {
    if(mCategory == null)
      return;

    mCurrentOpenCategoryIdx = RouteListManager.INSTANCE.getCurrentBookmark().getCategoryId();

    if(mCurrentOpenCategoryIdx == mCategory.getId())
    {
      mCurrentOpenBookmarkIdx = currentBookmark.getBookmarkId();
    }
    else
    {
      mCurrentOpenBookmarkIdx = -1;
    }

    notifyDataSetChanged();
  }

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
  public class ViewHolder extends DragItemAdapter.ViewHolder
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

      if(RouteListManager.INSTANCE.getStatus() && (mCategory.getId() == RouteListManager.INSTANCE.getCurrentBookmark().getCategoryId()))
        BookmarkManager.INSTANCE.nativeShowBookmarkOnMap(mCategory.getId(),
                                                       mBookmarkIndex);
    }

    @Override
    public boolean onItemLongClicked(View view)
    {
      super.onItemLongClicked(view);
      return true;
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
