package com.mapswithme.maps.widget.mapotempo;

import android.support.v4.util.Pair;
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
import com.mapswithme.maps.bookmarks.data.BookmarkRoutingManager;
import com.mapswithme.maps.bookmarks.data.Icon;
import com.woxthebox.draglistview.DragItemAdapter;

import java.util.ArrayList;
import java.util.List;

public class MapotempoListAdapter extends DragItemAdapter<Pair<Integer, MapotempoListAdapter.BookmarkInformation>, MapotempoListAdapter.ViewHolder>
    implements Bookmark.BookmarkParamsChangeListener
    ,BookmarkRoutingManager.CurrentBookmarkChangeListener
{

  private BookmarkCategory mCategory;
  private int mCurrentBookmarkIndex;
  private int mLayoutId;
  private int mGrabHandleId;
  private boolean mDragOnLongPress;

  public MapotempoListAdapter(BookmarkCategory category, int layoutId, int grabHandleId, boolean dragOnLongPress) {
    mLayoutId = layoutId;
    mGrabHandleId = grabHandleId;
    mDragOnLongPress = dragOnLongPress;
    setHasStableIds(true);
    mCategory = category;

    if(BookmarkRoutingManager.INSTANCE.getStatus())
      mCurrentBookmarkIndex = BookmarkRoutingManager.INSTANCE.getCurrentBookmark().getBookmarkId();

    List<Pair<Integer,BookmarkInformation>>mItemArray = new ArrayList<>();
    if(mCategory != null)
    {
      for (int i = 0; i < mCategory.getBookmarksCount(); i++)
      {
        mItemArray.add(new Pair(i, new BookmarkInformation(mCategory.getBookmark(i))));
      }
    }
    setItemList(mItemArray);
  }

  @Override
  public void onAttachedToRecyclerView(RecyclerView recyclerView)
  {
    super.onAttachedToRecyclerView(recyclerView);
    Bookmark.addBookmarkParamsChangeListener(this);
    BookmarkRoutingManager.INSTANCE.addCurrentBookmarkChangeListener(this);
  }

  @Override
  public void onDetachedFromRecyclerView(RecyclerView recyclerView)
  {
    super.onDetachedFromRecyclerView(recyclerView);
    Bookmark.removeBookmarkParamsChangeListener(this);
    BookmarkRoutingManager.INSTANCE.removeCurrentBookmarkChangeListener(this);
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, final int position) {
    super.onBindViewHolder(holder, position);
    final BookmarkInformation bmInfo = mItemList.get(position).second;

    // Here position corresponding to bookmark index, we need to update
    bmInfo.updateIndex(position);

    // Refresh holder informations.
    boolean isCurrent = mCurrentBookmarkIndex == bmInfo.mIndex;
    holder.refreshInfo(bmInfo, isCurrent);
  }

  @Override
  public long getItemId(int position) {
    return mItemList.get(position).first;
  }

  @Override
  public void onBookmarkParamsChangeListerner(Bookmark bookmark)
  {
    mItemList.get(bookmark.getBookmarkId()).second.updateInfo(bookmark);
    notifyDataSetChanged();
  }

  @Override
  public void onCurrentBookmarkChangeListerner(Bookmark currentBookmark)
  {
    mCurrentBookmarkIndex = currentBookmark.getBookmarkId();
    notifyDataSetChanged();
  }

  public void updateNativeBookmarkOrder(int fromPosition, int toPosition)
  {
    BookmarkManager.INSTANCE.changeBookmarkOrder(mCategory.getId(), fromPosition, toPosition);
    mCurrentBookmarkIndex = BookmarkRoutingManager.INSTANCE.getCurrentBookmark().getBookmarkId();
//    notifyDataSetChanged();
//    if(fromPosition == mCurrentBookmarkIndex)
//      mCurrentBookmarkIndex = toPosition;
  }

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
      super.onItemClicked(view);

      BookmarkManager.INSTANCE.nativeShowBookmarkOnMap(mCategory.getId(),
                                                       mBookmarkIndex);
    }

    @Override
    public boolean onItemLongClicked(View view)
    {
      super.onItemLongClicked(view);
      return true;
    }

    public void refreshInfo(BookmarkInformation bookmarkInformation, boolean isCurrent)
    {
      if(isCurrent)
        mBookmarkMarker.setVisibility(View.VISIBLE);
      else
        mBookmarkMarker.setVisibility(View.INVISIBLE);

      mBookmarkIndex = bookmarkInformation.mIndex;
      mText.setText(bookmarkInformation.mTitle);
      mIcon.setImageResource(bookmarkInformation.mIcon.getSelectedResId());
      mIcon.setOnClickListener(new View.OnClickListener()
      {
        @Override
        public void onClick(View v)
        {
          final Bookmark bm = mCategory.getBookmark(mBookmarkIndex);
          switch (bm.getIcon().getSelectedResId())
          {
            case R.drawable.ic_bookmark_marker_blue_on :
              bm.setParamsAndNotify(bm.getTitle(), BookmarkManager.ICONS.get(6), bm.getBookmarkDescription());
              break;
            case R.drawable.ic_bookmark_marker_green_on :
              bm.setParamsAndNotify(bm.getTitle(), BookmarkManager.ICONS.get(0), bm.getBookmarkDescription());
              break;
            case R.drawable.ic_bookmark_marker_red_on :
            default:
              bm.setParamsAndNotify(bm.getTitle(), BookmarkManager.ICONS.get(1), bm.getBookmarkDescription());
              break;
          }
        }
      });
    }
  }

  // Class interne enregistrant les informations des bookmarks
  public static class BookmarkInformation
  {
    public int mIndex;
    public Icon mIcon;
    public String mTitle;

    public BookmarkInformation(Bookmark bookmark)
    {
      updateInfo(bookmark);
    }

    public void updateInfo(Bookmark bookmark)
    {
      this.mIndex = bookmark.getBookmarkId();
      this.mIcon = bookmark.getIcon();
      this.mTitle = bookmark.getTitle();
    }

    public void updateIndex(int index)
    {
      mIndex = index;
    }
  }
}
