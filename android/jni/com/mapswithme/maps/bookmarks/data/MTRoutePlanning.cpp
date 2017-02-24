#include "BookmarkManager.hpp"

#include "com/mapswithme/core/jni_helper.hpp"
#include "com/mapswithme/maps/Framework.hpp"
#include "com/mapswithme/maps/UserMarkHelper.hpp"

#include "coding/zip_creator.hpp"
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

  JNIEXPORT jobject JNICALL
  Java_com_mapswithme_maps_bookmarks_data_MTRoutePlanning_nativeGetCurrentBookmark(
      JNIEnv * env, jobject thiz, jint catId)
  {
    place_page::Info info;
    BookmarkCategory * category = frm()->GetBmCategory(catId);
    if(category)
    {
      jint bmkId = category->m_mt_bookmark_planning.GetCurrentBookmarkId();

      if(bmkId == MTRoutePlanning::INVALIDE_VALUE)
        return NULL;

      frm()->FillBookmarkInfo(*static_cast<Bookmark const *>(category->GetUserMark(bmkId)), {static_cast<size_t>(bmkId), static_cast<size_t>(catId)}, info);
      return usermark_helper::CreateMapObject(env, info);
    }
    return NULL;
  }

  JNIEXPORT jobject JNICALL
  Java_com_mapswithme_maps_bookmarks_data_MTRoutePlanning_nativeStepNextBookmark(
      JNIEnv * env, jobject thiz, jint catId)
  {
    place_page::Info info;
    BookmarkCategory * category = frm()->GetBmCategory(catId);
    if(category)
    {
      jint bmkId = category->m_mt_bookmark_planning.StepNextBookmarkId();

      if(bmkId == MTRoutePlanning::INVALIDE_VALUE)
        return NULL;

      frm()->FillBookmarkInfo(*static_cast<Bookmark const *>(category->GetUserMark(bmkId)), {static_cast<size_t>(bmkId), static_cast<size_t>(catId)}, info);
      return usermark_helper::CreateMapObject(env, info);
    }
    return NULL;
  }

  JNIEXPORT jobject JNICALL
  Java_com_mapswithme_maps_bookmarks_data_MTRoutePlanning_nativeStepPreviousBookmark(
      JNIEnv * env, jobject thiz, jint catId)
  {
    place_page::Info info;
    BookmarkCategory * category = frm()->GetBmCategory(catId);
    if(category)
    {
      jint bmkId = category->m_mt_bookmark_planning.StepPreviousBookmarkId();

      if(bmkId == MTRoutePlanning::INVALIDE_VALUE)
        return NULL;

      frm()->FillBookmarkInfo(*static_cast<Bookmark const *>(category->GetUserMark(bmkId)), {static_cast<size_t>(bmkId), static_cast<size_t>(catId)}, info);
      return usermark_helper::CreateMapObject(env, info);
    }
    return NULL;
  }

  JNIEXPORT jboolean JNICALL
  Java_com_mapswithme_maps_bookmarks_data_MTRoutePlanning_nativeSetCurrentBookmark(
        JNIEnv * env, jobject thiz, jint catId, int bmIndex)
  {
    BookmarkCategory * category = frm()->GetBmCategory(catId);
    if(category)
    {
      bool res = category->m_mt_bookmark_planning.SetCurrentBookmarkId(bmIndex);
      return res;
    }
    return false;
  }
}  // extern "C"
