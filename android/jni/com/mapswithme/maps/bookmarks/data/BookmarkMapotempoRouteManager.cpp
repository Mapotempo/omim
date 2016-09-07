#include "BookmarkManager.hpp"

#include "com/mapswithme/core/jni_helper.hpp"
#include "com/mapswithme/maps/Framework.hpp"
#include "com/mapswithme/maps/UserMarkHelper.hpp"

#include "coding/zip_creator.hpp"
#include "map/place_page_info.hpp"

#include "base/logging.hpp"

namespace
{
::Framework * frm() { return g_framework->NativeFramework(); }
}  // namespace

extern "C"
{
using namespace jni;

  JNIEXPORT jboolean JNICALL
  Java_com_mapswithme_maps_bookmarks_data_BookmarkRoutingManager_nativeGetStatus(
      JNIEnv * env, jobject thiz)
  {
    return frm()->MT_GetStatus();
  }

  JNIEXPORT jboolean JNICALL
  Java_com_mapswithme_maps_bookmarks_data_BookmarkRoutingManager_nativeInitRoutingManager(
      JNIEnv * env, jobject thiz, jint bmCatIndex, jint bmIndex)
  {
    return frm()->MT_InitRouteManager(bmCatIndex, bmIndex);
  }

  JNIEXPORT jobject JNICALL
  Java_com_mapswithme_maps_bookmarks_data_BookmarkRoutingManager_nativeGetCurrentBookmark(
      JNIEnv * env, jobject thiz)
  {
    place_page::Info info;
    jint catId = frm()->MT_GetCurrentBookmarkCategory();
    jint bmkId = frm()->MT_GetCurrentBookmark();

    BookmarkCategory * category = frm()->GetBmCategory(catId);

    frm()->FillBookmarkInfo(*static_cast<Bookmark const *>(category->GetUserMark(bmkId)), {catId, bmkId}, info);
    return usermark_helper::CreateMapObject(env, info);
  }

  JNIEXPORT jobject JNICALL
  Java_com_mapswithme_maps_bookmarks_data_BookmarkRoutingManager_nativeStepNextBookmark(
      JNIEnv * env, jobject thiz)
  {
    place_page::Info info;
    jint catId = frm()->MT_GetCurrentBookmarkCategory();
    jint bmkId = frm()->MT_StepNextBookmark();
    BookmarkCategory * category = frm()->GetBmCategory(catId);

    if(!category)
    {
        return NULL;
    }

    frm()->FillBookmarkInfo(*static_cast<Bookmark const *>(category->GetUserMark(bmkId)), {catId, bmkId}, info);

    return usermark_helper::CreateMapObject(env, info);
  }

  JNIEXPORT jobject JNICALL
  Java_com_mapswithme_maps_bookmarks_data_BookmarkRoutingManager_nativeStepPreviousBookmark(
      JNIEnv * env, jobject thiz)
  {
    place_page::Info info;
    jint catId = frm()->MT_GetCurrentBookmarkCategory();
    jint bmkId = frm()->MT_StepPreviousBookmark();

    BookmarkCategory * category = frm()->GetBmCategory(catId);
    if(!category)
    {
        return NULL;
    }

    frm()->FillBookmarkInfo(*static_cast<Bookmark const *>(category->GetUserMark(bmkId)), {catId, bmkId}, info);
    return usermark_helper::CreateMapObject(env, info);
  }

  JNIEXPORT jboolean JNICALL
  Java_com_mapswithme_maps_bookmarks_data_BookmarkRoutingManager_nativeSetCurrentBookmark(
        JNIEnv * env, jobject thiz, int bmIndex)
    {
      return frm()->MT_SetCurrentBookmark(bmIndex);
    }
}  // extern "C"
