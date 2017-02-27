package com.mapswithme.maps.bookmarks.data;

import android.support.annotation.Nullable;

import com.mapswithme.maps.bookmarks.mapotempo.MTRoutePlanningManagerStatus;

public enum MTRoutePlanningManager
{
  INSTANCE;

  public void stopRoutingManager()
  {
    nativeStopFollowCategory();
  }

  private MTRoutePlanningManagerStatus JNIConverterEnum(int status)
  {
    if(status == 0)
      return MTRoutePlanningManagerStatus.FOLLOW_PLANNING;
    else if(status == 1)
      return MTRoutePlanningManagerStatus.FOLLOW_EMPTY_PLANNING;
    else
      return MTRoutePlanningManagerStatus.CLOSE;
  }

  public MTRoutePlanningManagerStatus initRoutingManager(int catIndex)
  {
    int native_status = nativeFollowCategory(catIndex);
    return JNIConverterEnum(native_status);
  }

  public MTRoutePlanningManagerStatus getStatus()
  {
    int native_status = nativeGetStatus();
    return JNIConverterEnum(native_status);
  }

  public BookmarkCategory getCurrentBookmarkCategory()
  {
    return  nativeGetFollowedCategory();
  }

  private static native int nativeGetStatus();

  private static native void nativeStopFollowCategory();

  private static native int nativeFollowCategory(int catIndex);

  @Nullable
  private static native BookmarkCategory nativeGetFollowedCategory();

  public static native Bookmark nativeAddBookmarkToFollowedCategory(String name, double lat, double lon);

  public static native boolean nativeOptimiseBookmarkCategory(int catIndex);

  public static native void nativeStopCurrentOptimisation();
}
