#include "mt_route_planning_manager.hpp"

#include "std/iostream.hpp"
#include <vector>

#include "map/framework.hpp"
#include "map/bookmark.hpp"
#include "map/user_mark.hpp"
#include "base/logging.hpp"

#include "geometry/distance_on_sphere.hpp"

#include "3party/vroom/src/structures/typedefs.h"
#include "3party/vroom/src/structures/matrix.h"
#include "3party/vroom/src/utils/version.h"
#include "3party/vroom/src/heuristics/tsp_strategy.h"
#include "3party/vroom/src/loaders/tsplib_loader.h"
#include "3party/vroom/src/utils/logger.h"

#include "routing/osrm_engine.hpp"
#include "geometry/point2d.hpp"
#include "drape/color.hpp"

const size_t MTRoutePlanningManager::INVALIDE_VALUE = numeric_limits<size_t>::max();
 
PlanningManagerStatus MTRoutePlanningManager::GetStatus()
{
  if(m_indexCurrentBmCat == INVALIDE_VALUE ||
     !GetBmCategory(m_indexCurrentBmCat))
     return PlanningManagerStatus::CLOSE;

  if(GetBmCategory(m_indexCurrentBmCat)->GetUserMarkCount() == 0)
    return PlanningManagerStatus::FOLLOW_EMPTY_PLANNING;

  return PlanningManagerStatus::FOLLOW_PLANNING;
}

void MTRoutePlanningManager::StopFollow()
{
  m_indexCurrentBmCat = INVALIDE_VALUE;

  // Hide all other category
  for(int i = 0; i < GetBmCategoriesCount(); i++)
  {
    BookmarkCategory * otherCat = GetBmCategory(i);
    {
      BookmarkCategory::Guard guard(*otherCat);
      guard.m_controller.SetIsVisible(false);
    }
    otherCat->SaveToKMLFile();
  }

  save_status();
}

PlanningManagerStatus MTRoutePlanningManager::FollowPlanning(size_t indexBmCat)
{
  static bool res = false;
  BookmarkCategory * bmCat = GetBmCategory(indexBmCat);

  if(!bmCat)
    return PlanningManagerStatus::CLOSE;

  m_indexCurrentBmCat = indexBmCat;
  save_status();
  // Hide all other category
  for(int i = 0; i < GetBmCategoriesCount(); i++)
  {
    bool visibily = false;
    if(i == indexBmCat)
      visibily = true;

    BookmarkCategory * otherCat = GetBmCategory(i);
    {
      BookmarkCategory::Guard guard(*otherCat);
      guard.m_controller.SetIsVisible(visibily);
    }
    otherCat->SaveToKMLFile();
  }

  return GetStatus();
}

BookmarkCategory * MTRoutePlanningManager::GetFollowedBookmarkCategory()
{
  return GetBmCategory(m_indexCurrentBmCat);
}

/*
bool MTRoutePlanningManager::SetCurrentBookmark(size_t indexBm)
{
  bool res = false;
  BookmarkCategory * bmCat = GetBmCategory(m_indexCurrentBmCat);
  bmCat->m_mt_bookmark_planning.SetCurrentPlanId(indexBm);
  save_status();
  return res;
}

size_t MTRoutePlanningManager::StepNextBookmark()
{
  BookmarkCategory * bmCat = GetBmCategory(m_indexCurrentBmCat);
  bmCat->m_mt_bookmark_planning.StepNextPlanId();
  save_status();
  return GetCurrentBookmark();
}

size_t MTRoutePlanningManager::StepPreviousBookmark()
{
  BookmarkCategory * bmCat = GetBmCategory(m_indexCurrentBmCat);
  bmCat->m_mt_bookmark_planning.StepPreviousPlanId();
  save_status();
  return GetCurrentBookmark();
}

size_t MTRoutePlanningManager::GetCurrentBookmark()
{
  BookmarkCategory * cat = GetBmCategory(m_indexCurrentBmCat);
  if(cat)
    return cat->m_mt_bookmark_planning.GetCurrentPlanId();
  else
    return INVALIDE_VALUE;
};
*/

bool MTRoutePlanningManager::CheckCurrentBookmarkStatus(const double & curLat, const double & curLon)
{
  if(GetStatus())
  {
    BookmarkCategory * cat = GetBmCategory(m_indexCurrentBmCat);
    const UserMark * bm = cat->GetUserMark(cat->m_mt_bookmark_planning.GetCurrentBookmarkId());
    ms::LatLon bmPosition = bm->GetLatLon();
    if(cat)
    {
      double const d = ms::DistanceOnEarth(curLat, curLon, bmPosition.lat, bmPosition.lon);
      if(d < MTRoutePlanningManager::MT_DISTANCE_BOOKMARK_DONE)
      {
        return true;
      }
    }
  }
  return false;
}

void MTRoutePlanningManager::save_status()
{
  BookmarkCategory * cat = GetBmCategory(m_indexCurrentBmCat);
  if(cat)
    settings::Set("category", m_indexCurrentBmCat);
  else
    settings::Set("category", INVALIDE_VALUE);
}
  
  
  
  
  
  
  
  
  
void MTRoutePlanningManager::SetRouter(unique_ptr<routing::IRouter> && router)
{
  threads::MutexGuard guard(m_routeListManagerMutex);
  m_optimizer.reset(new routing::AsyncOptimizer());
  m_optimizer->SetRouter(move(router));
}

void MTRoutePlanningManager::SetOptimisationListeners(TOptimisationFinishFn const & finishListener,
                                TOptimisationProgessFn const & progressListener)
{
  threads::MutexGuard guard(m_routeListManagerMutex);
  m_optimisationFinishFn = finishListener;
  m_optimisationProgressFn = progressListener;
}

bool MTRoutePlanningManager::optimiseBookmarkCategory(size_t indexBmCat)
{
  threads::MutexGuard guard(m_routeListManagerMutex);

  BookmarkCategory * bmCat = GetBmCategory(indexBmCat);
  if(bmCat == nullptr)
    return false;

  {
    double lat, lon;
    bool user_position = m_framework.GetCurrentPosition(lat, lon);
    size_t probleme_size = (user_position ? bmCat->GetUserMarkCount() + 1 : bmCat->GetUserMarkCount()) ;

    vector<m2::PointD> problemePoints(probleme_size);
    LOG(LINFO, (probleme_size , " probleme points founds"));

    if(user_position)
    {
      problemePoints[0] = MercatorBounds::FromLatLon(lat, lon);
    }

    for(int i = 0; i < bmCat->GetUserMarkCount(); i++)
    {
      const UserMark * user_mark = bmCat->GetUserMark(i);
      int ref = (user_position ? i + 1: i);
      problemePoints[ref] = MercatorBounds::FromLatLon(user_mark->GetLatLon().lat, user_mark->GetLatLon().lon);
    }

    auto readyCallback = [this, indexBmCat] (std::pair<std::list<size_t>, size_t> &result, routing::IRouter::ResultCode code, m2::PolylineD polyline)
    {
      bool res = false;

      threads::MutexGuard guard(m_routeListManagerMutex);

      BookmarkCategory * bmCat = GetBmCategory(indexBmCat);

      if (code == routing::IRouter::ResultCode::NoError)
      {
        res = true;        
        LOG(LINFO, ("Route optimize"));

        if(result.first.size() < 1)
        {
          if(m_optimisationFinishFn)
            m_optimisationFinishFn(false);
          return;
        }

        // pop the first current position point
        //result.first.pop_front();
        SortUserMarks(indexBmCat, result.first);
        bmCat->m_mt_bookmark_planning.SetCurrentBookmarkId(0);

        Track::Params params;
        params.m_name = "new_track";
        params.m_colors.push_back({ 15.0f, dp::Color::Black()});

        //Track const track(points, params);
        bmCat->ClearTracks();
        bmCat->AddTrack(make_unique<Track>(polyline, params));

        if(indexBmCat == m_indexCurrentBmCat)
        {
          BookmarkCategory::Guard guard(*bmCat);
          guard.m_controller.SetIsVisible(false);
          guard.m_controller.SetIsVisible(true);
        }
      }
      else
        LOG(LWARNING, ("Problem occured during route optimization, abort"));

      if(m_optimisationFinishFn)
        m_optimisationFinishFn(res);
    };

    auto progressCallback = [this](float percent)
    {
      if(m_optimisationProgressFn)
        m_optimisationProgressFn(percent);
    };

    // On ne garde pas la position de l'utilisateur dans le résultat mais on la prend tout de même
    // compte pour l'optimisation.
    m_optimizer->OptimizeRoute(problemePoints, !user_position, readyCallback, progressCallback, 0);
  }

  return true;
}

void MTRoutePlanningManager::stopCurrentOptimisation()
{
  m_optimizer->ClearState();
}

/**
 * Surcharge de la fonction "CreateBmCategory" du BookmarkManager.
 * Ceci pour voir passer les creation de catégories
 * et pouvoir les cacher par defaut sans avoir à toucher au code du
 * boomark_manager.hpp/cpp.
 **/
size_t MTRoutePlanningManager::CreateBmCategory(string const & name)
{
  threads::MutexGuard guard(m_routeListManagerMutex);
  size_t index = BookmarkManager::CreateBmCategory(name);
  BookmarkCategory * bmCat = GetBmCategory(index);
  if(bmCat)
  {
    BookmarkCategory::Guard guard(*bmCat);
    guard.m_controller.SetIsVisible(false);
    bmCat->SaveToKMLFile();
  }

  save_status();
  return index;
}

/**
 * Surcharge de la fonction "DeleteBmCategory" du BookmarkManager.
 * Ceci pour voir passer les suppresions de
 * catégories sans avoir à toucher au code du boomark_manager.hpp/cpp.
 **/
bool MTRoutePlanningManager::DeleteBmCategory(size_t index)
{
  threads::MutexGuard guard(m_routeListManagerMutex);
  bool res = BookmarkManager::DeleteBmCategory(index);
  if(res == true)
  {
    if(index < m_indexCurrentBmCat)
      m_indexCurrentBmCat--;
    else if(index == m_indexCurrentBmCat)
    {
      m_indexCurrentBmCat = INVALIDE_VALUE;
    }
  }

  save_status();
  return res;
}

/**
 * Surcharge de la fonction "LoadBookmark" du BookmarkManager.
 * Ceci pour voir passer les chargements de catégories
 * et pouvoir les cacher par defaut sans avoir à toucher au code du
 * boomark_manager.hpp/cpp.
 **/
void MTRoutePlanningManager::LoadBookmark(string const & filePath)
{
  threads::MutexGuard guard(m_routeListManagerMutex);
  BookmarkManager::LoadBookmark(filePath);
  // Hide the last bookmark load else if that the current bmCat.
  if((GetBmCategoriesCount() - 1) != m_indexCurrentBmCat)
  {
    BookmarkCategory * bmCat = GetBmCategory(GetBmCategoriesCount() - 1);

    if(bmCat)
    {
      BookmarkCategory::Guard guard(*bmCat);
      guard.m_controller.SetIsVisible(false);
      bmCat->SaveToKMLFile();
    }
  }
}
