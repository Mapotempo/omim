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
  Java_com_mapswithme_maps_bookmarks_data_MTRouteListManager_nativeGetStatus(
      JNIEnv * env, jobject thiz)
  {
    return frm()->MT_GetStatus();
  }

  JNIEXPORT void JNICALL
  Java_com_mapswithme_maps_bookmarks_data_MTRouteListManager_nativeStopRoutingManager()
  {
    frm()->MT_StopRouteManager();
  }

  JNIEXPORT jboolean JNICALL
  Java_com_mapswithme_maps_bookmarks_data_MTRouteListManager_nativeInitRoutingManager(
      JNIEnv * env, jobject thiz, jint bmCatIndex, jint bmIndex)
  {
    return frm()->MT_InitRouteManager(bmCatIndex, bmIndex);
  }

  JNIEXPORT jobject JNICALL
  Java_com_mapswithme_maps_bookmarks_data_MTRouteListManager_nativeGetCurrentBookmark(
      JNIEnv * env, jobject thiz)
  {
    place_page::Info info;
    jint catId = frm()->MT_GetCurrentBookmarkCategory();
    jint bmkId = frm()->MT_GetCurrentBookmark();

    BookmarkCategory * category = frm()->GetBmCategory(catId);

    frm()->FillBookmarkInfo(*static_cast<Bookmark const *>(category->GetUserMark(bmkId)), {static_cast<size_t>(bmkId), static_cast<size_t>(catId)}, info);
    return usermark_helper::CreateMapObject(env, info);
  }

  JNIEXPORT jobject JNICALL
  Java_com_mapswithme_maps_bookmarks_data_MTRouteListManager_nativeStepNextBookmark(
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

    frm()->FillBookmarkInfo(*static_cast<Bookmark const *>(category->GetUserMark(bmkId)), {static_cast<size_t>(bmkId), static_cast<size_t>(catId)}, info);
    return usermark_helper::CreateMapObject(env, info);
  }

  JNIEXPORT jobject JNICALL
  Java_com_mapswithme_maps_bookmarks_data_MTRouteListManager_nativeStepPreviousBookmark(
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

    frm()->FillBookmarkInfo(*static_cast<Bookmark const *>(category->GetUserMark(bmkId)), {static_cast<size_t>(bmkId), static_cast<size_t>(catId)}, info);
    return usermark_helper::CreateMapObject(env, info);
  }

  JNIEXPORT jboolean JNICALL
  Java_com_mapswithme_maps_bookmarks_data_MTRouteListManager_nativeSetCurrentBookmark(
        JNIEnv * env, jobject thiz, int bmIndex)
  {
    return frm()->MT_SetCurrentBookmark(bmIndex);
  }

  JNIEXPORT jboolean JNICALL
  Java_com_mapswithme_maps_bookmarks_data_MTRouteListManager_nativeRestoreRoutingManager(
        JNIEnv * env, jobject thiz)
  {
    return frm()->MT_RestoreRoutingManager();
  }

  JNIEXPORT jobject JNICALL
  Java_com_mapswithme_maps_bookmarks_data_MTRouteListManager_nativeAddBookmarkToCurrentCategory(
      JNIEnv * env, jobject thiz, jstring name, double lat, double lon)
  {
    if(frm()->MT_GetStatus())
    {
      m2::PointD const glbPoint(MercatorBounds::FromLatLon(lat, lon));
      ::Framework * f = frm();
      BookmarkData bmkData(ToNativeString(env, name), f->LastEditedBMType());
      size_t const lastEditedCategory = frm()->MT_GetCurrentBookmarkCategory();
      size_t const createdBookmarkIndex = f->AddBookmark(lastEditedCategory, glbPoint, bmkData);
      place_page::Info & info = g_framework->GetPlacePageInfo();
      info.m_bac = {createdBookmarkIndex, lastEditedCategory};
      return usermark_helper::CreateMapObject(env, info);
    }
    else
      return NULL;
  }

  JNIEXPORT jboolean JNICALL
  Java_com_mapswithme_maps_bookmarks_data_MTRouteListManager_nativeOptimiseBookmarkCategory(
       JNIEnv * env, jobject thiz, int catIndex)
  {
    return frm()->MT_OptimiseBookmarkCategory(catIndex);
  }

  JNIEXPORT void JNICALL
  Java_com_mapswithme_maps_bookmarks_data_MTRouteListManager_nativeStopCurrentOptimisation(
       JNIEnv * env)
  {
    frm()->MT_StopCurrentOptimisation();
  }
}  // extern "C"
