 package com.mapswithme.maps.widget.mapotempo;

import android.app.Activity;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapswithme.maps.R;
import com.mapswithme.maps.bookmarks.data.Bookmark;
import com.mapswithme.maps.bookmarks.data.BookmarkCategory;
import com.mapswithme.maps.bookmarks.data.BookmarkManager;
import com.mapswithme.maps.bookmarks.data.BookmarkRoutingManager;

import java.util.Timer;
import java.util.TimerTask;

 public class MapotempoListAdapter extends BaseAdapter
{
  private static final long UPDATE_PERIODE = 1000;
  private final Activity mActivity;
  private final BookmarkCategory mCategory;

  private Timer timer = null;

  public MapotempoListAdapter(final Activity activity, BookmarkCategory cat)
  {
    mActivity = activity;
    mCategory = cat;
  }

  public void startTimerUpdate()
  {
    if(timer  == null)
    {
      timer = new Timer();
      timer.schedule(new TimerTask()
      {
        @Override
        public void run()
        {
          mActivity.runOnUiThread(new Runnable()
          {
            @Override
            public void run()
            {
              notifyDataSetChanged();
            }
          });
        }
      }, 0, UPDATE_PERIODE);
    }
  }

  public void stopTimerUpdate()
  {
    if(timer  != null)
    {
      timer.cancel();
      timer.purge();
      timer = null;
    }
  }

  @Override
  public int getViewTypeCount()
  {
    return 1;
  }

  @Override
  public int getItemViewType(int position)
  {
    return 0;
  }

  @Override
  public boolean isEnabled(int position)
  {
    return true;
  }

  @Override
  public View getView(final int position, View convertView, ViewGroup parent)
  {
    if (convertView == null)
    {
      final int id = R.layout.item_mapotempo_bookmark;
      convertView = LayoutInflater.from(mActivity).inflate(id, parent, false);
      convertView.setTag(new PinHolder(convertView));
    }

    final PinHolder holder = (PinHolder) convertView.getTag();
    holder.set((Bookmark) getItem(position));
    holder.icon.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        Bookmark bookmark = (Bookmark) getItem(position);
        switch (bookmark.getIcon().getSelectedResId())
        {
          case R.drawable.ic_bookmark_marker_red_on:
            bookmark.setParams(bookmark.getTitle(), BookmarkManager.ICONS.get(1), bookmark.getBookmarkDescription());
            break;
          case R.drawable.ic_bookmark_marker_blue_on:
            bookmark.setParams(bookmark.getTitle(), BookmarkManager.ICONS.get(6), bookmark.getBookmarkDescription());
            break;
          case R.drawable.ic_bookmark_marker_green_on:
          default:
            bookmark.setParams(bookmark.getTitle(), BookmarkManager.ICONS.get(0), bookmark.getBookmarkDescription());
            break;
        }
        holder.set(bookmark);
      }
    });
    return convertView;
  }

  @Override
  public int getCount()
  {
    return mCategory.getBookmarksCount();
  }

  @Override
  public Object getItem(int position)
  {
    return mCategory.getBookmark(position);
  }

  @Override
  public long getItemId(int position)
  {
    return position;
  }

  private class PinHolder
  {
    ImageView icon;
    TextView name;

    public PinHolder(View convertView)
    {
      icon = (ImageView) convertView.findViewById(R.id.iv__bookmark_color);
      name = (TextView) convertView.findViewById(R.id.tv__bookmark_name);
    }

    void setName(Bookmark bmk)
    {
      name.setText(bmk.getTitle());
    }

    void setIcon(Bookmark bookmark)
    {
      icon.setImageResource(bookmark.getIcon().getSelectedResId());
    }

    void set(Bookmark bmk)
    {
      setName(bmk);
      setIcon(bmk);
    }
  }
}
