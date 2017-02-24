package com.mapswithme.maps.bookmarks.data;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public enum MTRoutePlanning
{
  INSTANCE;

 // private static List<CurrentBookmarkChangeListener> mCurrentBookmarkChangeListenerList = new ArrayList();

  @Nullable
  public Bookmark getCurrentBookmark(int catId)
  {
    return nativeGetCurrentBookmark(catId);
  }

  public boolean setCurrentBookmark(int catId, int bmId)
  {
    boolean res = nativeSetCurrentBookmark(catId, bmId);
    return res;
  }

  @Nullable
  public Bookmark stepNextBookmark(int catId)
  {
    Bookmark bm = nativeStepNextBookmark(catId);
    return bm;
  }

  @Nullable
  public Bookmark stepPreviousBookmark(int catId)
  {
    Bookmark bm = nativeStepPreviousBookmark(catId);
    return bm;
  }
/*
  public interface CurrentBookmarkChangeListener
  {
    void onCurrentBookmarkChangeListerner(@Nullable Bookmark currentBookmark);
  }

  public static void addCurrentBookmarkChangeListener(CurrentBookmarkChangeListener currentBookmarkChangeListener)
  {
    mCurrentBookmarkChangeListenerList.add(currentBookmarkChangeListener);
  }

  public static void removeCurrentBookmarkChangeListener(CurrentBookmarkChangeListener currentBookmarkChangeListener)
  {
    mCurrentBookmarkChangeListenerList.remove(currentBookmarkChangeListener);
  }

  public static void notifyCurrentBookmarkChange()
  {
    Bookmark currentBookmark = null;
    if(INSTANCE.getStatus())
    {
      currentBookmark = INSTANCE.getCurrentBookmark(0);
    }

    for (CurrentBookmarkChangeListener currentBookmarkChangeListener : mCurrentBookmarkChangeListenerList)
    {
      currentBookmarkChangeListener.onCurrentBookmarkChangeListerner(currentBookmark);
    }
  }*/

  public static native Bookmark nativeGetCurrentBookmark(int catId);

  private static native boolean nativeSetCurrentBookmark(int catId, int bmId);

  private static native Bookmark nativeStepNextBookmark(int catId);

  private static native Bookmark nativeStepPreviousBookmark(int catId);

}
