package com.mapswithme.maps.bookmarks.data;

import android.support.annotation.Nullable;

public enum MTRoutePlanningManager
{
  INSTANCE;

  public void stopRoutingManager()
  {
    nativeStopFollowCategory();
  }

  public boolean initRoutingManager(int catIndex, int bmIndex)
  {
    boolean res = nativeFollowCategory(catIndex);
    return res;
  }

  public boolean getStatus()
  {
    return nativeGetStatus();
  }

  public BookmarkCategory getCurrentBookmarkCategory()
  {
    return  nativeGetFollowedCategory();
  }

  private static native boolean nativeGetStatus();

  private static native void nativeStopFollowCategory();

  private static native boolean nativeFollowCategory(int catIndex);

  @Nullable
  private static native BookmarkCategory nativeGetFollowedCategory();

  public static native Bookmark nativeAddBookmarkToFollowedCategory(String name, double lat, double lon);

  public static native boolean nativeOptimiseBookmarkCategory(int catIndex);

  public static native void nativeStopCurrentOptimisation();
}
