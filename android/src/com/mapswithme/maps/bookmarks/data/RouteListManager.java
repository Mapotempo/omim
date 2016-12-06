package com.mapswithme.maps.bookmarks.data;

import java.util.ArrayList;
import java.util.List;

public enum RouteListManager
{
  INSTANCE;

  private static List<CurrentBookmarkChangeListener> mCurrentBookmarkChangeListenerList = new ArrayList();

  public boolean initRoutingManager(int catIndex, int bmIndex)
  {
    return nativeInitRoutingManager(catIndex, bmIndex);
  }

  public Bookmark getCurrentBookmark()
  {
    return nativeGetCurrentBookmark();
  }

  public boolean setCurrentBookmark(int bmIndex)
  {
    return nativeSetCurrentBookmark(bmIndex);
  }

  public Bookmark stepNextBookmark()
  {
    return nativeStepNextBookmark();
  }

  public Bookmark stepPreviousBookmark()
  {
    return nativeStepPreviousBookmark();
  }

  public boolean getStatus()
  {
    return nativeGetStatus();
  }

  public interface CurrentBookmarkChangeListener
  {
    void onCurrentBookmarkChangeListerner(Bookmark currentBookmark);
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
    if(INSTANCE.getStatus())
    {
      Bookmark currentBookmark = INSTANCE.getCurrentBookmark();
      for (CurrentBookmarkChangeListener currentBookmarkChangeListener : mCurrentBookmarkChangeListenerList)
      {
        currentBookmarkChangeListener.onCurrentBookmarkChangeListerner(currentBookmark);
      }
    }
  }

  public static native boolean nativeGetStatus();

  public static native void nativeStopRoutingManager();

  public static native boolean nativeInitRoutingManager(int catIndex, int bmIndex);

  public static native Bookmark nativeGetCurrentBookmark();

  public static native boolean nativeSetCurrentBookmark(int bmIndex);

  public static native Bookmark nativeStepNextBookmark();

  public static native Bookmark nativeStepPreviousBookmark();

  public static native boolean nativeRestoreRoutingManager();

  public static native Bookmark nativeAddBookmarkToCurrentCategory(String name, double lat, double lon);

  public static native boolean nativeOptimiseCurrentBookmarks();
}
