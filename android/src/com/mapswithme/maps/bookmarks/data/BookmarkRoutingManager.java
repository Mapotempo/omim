package com.mapswithme.maps.bookmarks.data;

public enum BookmarkRoutingManager
{
  INSTANCE;

  public boolean initRoutingManager(int catIndex, int bmIndex)
  {
    return nativeInitRoutingManager(catIndex, bmIndex);
  }

  public Bookmark getCurrentBookmark()
  {
    return nativeGetCurrentBookmark();
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

  public static native boolean nativeGetStatus();

  public static native boolean nativeInitRoutingManager(int catIndex, int bmIndex);

  public static native Bookmark nativeGetCurrentBookmark();

  public static native Bookmark nativeStepNextBookmark();

  public static native Bookmark nativeStepPreviousBookmark();
}
