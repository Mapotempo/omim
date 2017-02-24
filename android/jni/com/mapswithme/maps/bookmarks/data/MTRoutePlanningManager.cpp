#include "BookmarkManager.hpp"

#include <jni.h>

#include "com/mapswithme/core/jni_helper.hpp"
#include "com/mapswithme/maps/Framework.hpp"
#include "com/mapswithme/maps/UserMarkHelper.hpp"

#include "map/place_page_info.hpp"
#include <map/mt_route_planning_manager.hpp>

#include "base/logging.hpp"

namespace
{
::Framework * frm() { return g_framework->NativeFramework(); }
}  // namespace

extern "C"
{
using namespace jni;

  JNIEXPORT jboolean JNICALL
  Java_com_mapswithme_maps_bookmarks_data_MTRoutePlanningManager_nativeGetStatus(
      JNIEnv * env, jobject thiz)
  {
    return frm()->MT_GetStatus();
  }

  JNIEXPORT void JNICALL
  Java_com_mapswithme_maps_bookmarks_data_MTRoutePlanningManager_nativeStopFollowCategory(
    JNIEnv * env, jobject thiz)
  {
    frm()->MT_StopFollowPlanning();
  }

  JNIEXPORT jboolean JNICALL
  Java_com_mapswithme_maps_bookmarks_data_MTRoutePlanningManager_nativeFollowCategory(
      JNIEnv * env, jobject thiz, jint bmCatIndex)
  {
    return frm()->MT_FollowPlanning(bmCatIndex);
  }

  JNIEXPORT jobject JNICALL
  Java_com_mapswithme_maps_bookmarks_data_MTRoutePlanningManager_nativeGetFollowedCategory(
      JNIEnv * env, jobject thiz)
  {
    int catId = frm()->MT_GetFollowedBookmarkCategoryID();

    if(catId == MTRoutePlanningManager::INVALIDE_VALUE)
      return NULL;

    // public MapObject(@MapObjectType int mapObjectType, String title, String subtitle, double lat,
    // double lon, String address, String apiId, @NonNull Banner banner, boolean reachableByTaxi)
    static jmethodID const ctorId = jni::GetConstructorID(env, g_bookmarkCategoryClazz, "(I)V");
    jobject categoryObject = env->NewObject(g_bookmarkCategoryClazz, ctorId, catId);

    return categoryObject;
  }

  JNIEXPORT jobject JNICALL
  Java_com_mapswithme_maps_bookmarks_data_MTRoutePlanningManager_nativeAddBookmarkToFollowedCategory(
      JNIEnv * env, jobject thiz, jstring name, double lat, double lon)
  {
    if(frm()->MT_GetStatus())
    {
      m2::PointD const glbPoint(MercatorBounds::FromLatLon(lat, lon));
      ::Framework * f = frm();
      BookmarkData bmkData(ToNativeString(env, name), f->LastEditedBMType());
      size_t const lastEditedCategory = frm()->MT_GetFollowedBookmarkCategoryID();
      size_t const createdBookmarkIndex = f->AddBookmark(lastEditedCategory, glbPoint, bmkData);
      place_page::Info & info = g_framework->GetPlacePageInfo();
      info.m_bac = {createdBookmarkIndex, lastEditedCategory};
      return usermark_helper::CreateMapObject(env, info);
    }
    else
      return NULL;
  }

  JNIEXPORT jboolean JNICALL
  Java_com_mapswithme_maps_bookmarks_data_MTRoutePlanningManager_nativeRestoreRoutingManager(
        JNIEnv * env, jobject thiz)
  {
    return frm()->MT_RestoreRoutingManager();
  }

  JNIEXPORT jboolean JNICALL
  Java_com_mapswithme_maps_bookmarks_data_MTRoutePlanningManager_nativeOptimiseBookmarkCategory(
       JNIEnv * env, jobject thiz, int catIndex)
  {
    return frm()->MT_OptimiseBookmarkCategory(catIndex);
  }

  JNIEXPORT void JNICALL
  Java_com_mapswithme_maps_bookmarks_data_MTRoutePlanningManager_nativeStopCurrentOptimisation(
       JNIEnv * env)
  {
    frm()->MT_StopCurrentOptimisation();
  }
}  // extern "C"
