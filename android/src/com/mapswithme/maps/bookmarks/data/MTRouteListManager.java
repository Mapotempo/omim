package com.mapswithme.maps.bookmarks.data;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public enum MTRouteListManager
{
  INSTANCE;

  private static List<CurrentBookmarkChangeListener> mCurrentBookmarkChangeListenerList = new ArrayList();

  public void stopRoutingManager()
  {
    nativeStopRoutingManager();
    MTRouteListManager.INSTANCE.notifyCurrentBookmarkChange();
  }

  public boolean initRoutingManager(int catIndex, int bmIndex)
  {
    boolean res = nativeInitRoutingManager(catIndex, bmIndex);
    if(res)
      MTRouteListManager.INSTANCE.notifyCurrentBookmarkChange();
    return res;
  }

  public Bookmark getCurrentBookmark()
  {
    return nativeGetCurrentBookmark();
  }

  public boolean setCurrentBookmark(int bmIndex)
  {
    boolean res = nativeSetCurrentBookmark(bmIndex);
    if(res)
      MTRouteListManager.INSTANCE.notifyCurrentBookmarkChange();
    return res;
  }

  public Bookmark stepNextBookmark()
  {
    Bookmark bm = nativeStepNextBookmark();
    MTRouteListManager.INSTANCE.notifyCurrentBookmarkChange();
    return bm;
  }

  public Bookmark stepPreviousBookmark()
  {
    Bookmark bm = nativeStepPreviousBookmark();
    MTRouteListManager.INSTANCE.notifyCurrentBookmarkChange();
    return bm;
  }

  public boolean getStatus()
  {
    return nativeGetStatus();
  }

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
      currentBookmark = INSTANCE.getCurrentBookmark();
    }

    for (CurrentBookmarkChangeListener currentBookmarkChangeListener : mCurrentBookmarkChangeListenerList)
    {
      currentBookmarkChangeListener.onCurrentBookmarkChangeListerner(currentBookmark);
    }
  }

  public static native boolean nativeGetStatus();

  private static native void nativeStopRoutingManager();

  private static native boolean nativeInitRoutingManager(int catIndex, int bmIndex);

  public static native Bookmark nativeGetCurrentBookmark();

  private static native boolean nativeSetCurrentBookmark(int bmIndex);

  private static native Bookmark nativeStepNextBookmark();

  private static native Bookmark nativeStepPreviousBookmark();

  public static native boolean nativeRestoreRoutingManager();

  public static native Bookmark nativeAddBookmarkToCurrentCategory(String name, double lat, double lon);

  public static native boolean nativeOptimiseBookmarkCategory(int catIndex);

  public static native void nativeStopCurrentOptimisation();
}
