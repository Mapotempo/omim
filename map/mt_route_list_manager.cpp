#include "mt_route_list_manager.hpp"

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
#include <boost/log/utility/setup/console.hpp>

#include "routing/osrm_engine.hpp"
#include "geometry/point2d.hpp"
#include "drape/color.hpp"

MTRouteListManager::MTRouteListManager(Framework & f)
  : BookmarkManager(f)  
    ,m_framework(f)
    ,m_indexCurrentBmCat(-1)
    ,m_indexCurrentBm(-1)
{
}

MTRouteListManager::~MTRouteListManager()
{
}

bool MTRouteListManager::GetStatus()
{
  if(m_indexCurrentBmCat < 0)
    return false;
  return true;
}

void MTRouteListManager::StopManager()
{
  m_indexCurrentBmCat = -1;
  m_indexCurrentBm = -1;

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

  m_framework.MT_SaveRoutingManager();
}

bool MTRouteListManager::InitManager(int64_t indexBmCat, int64_t indexFirstBmToDisplay)
{
  boost::log::add_console_log(std::cout,
                              boost::log::keywords::format = "%Message%");

  static bool init = false;
  BookmarkCategory * bmCat = GetBmCategory(indexBmCat);
  if(bmCat == NULL || bmCat->GetUserMarkCount() <= indexFirstBmToDisplay)
  {
    return false;
  }

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

  m_indexCurrentBmCat = indexBmCat;
  m_indexCurrentBm = indexFirstBmToDisplay;
  m_framework.MT_SaveRoutingManager();
  return true;
}

void MTRouteListManager::ResetManager(){
  m_indexCurrentBmCat = -1;
  m_indexCurrentBm = -1;
  m_framework.MT_SaveRoutingManager();
}

bool MTRouteListManager::SetCurrentBookmark(int64_t indexBm)
{
  bool res = false;
  BookmarkCategory * bmCat = GetBmCategory(m_indexCurrentBmCat);
  if(bmCat && indexBm < bmCat->GetUserPointCount() && indexBm >= 0)
  {
    m_indexCurrentBm = indexBm;
    res = true;
  }
  m_framework.MT_SaveRoutingManager();
  return res;
}

int64_t MTRouteListManager::StepNextBookmark()
{
  BookmarkCategory * bmCat = GetBmCategory(m_indexCurrentBmCat);

  m_indexCurrentBm++;
  if(bmCat && (m_indexCurrentBm >= bmCat->GetUserPointCount()))
    m_indexCurrentBm = 0;

  m_framework.MT_SaveRoutingManager();
  return GetCurrentBookmark();
}

int64_t MTRouteListManager::StepPreviousBookmark()
{
  BookmarkCategory * bmCat = GetBmCategory(m_indexCurrentBmCat);

  m_indexCurrentBm--;
  if(bmCat && (m_indexCurrentBm < 0))
    m_indexCurrentBm = bmCat->GetUserPointCount() - 1;

  m_framework.MT_SaveRoutingManager();
  return GetCurrentBookmark();
}

bool MTRouteListManager::checkCurrentBookmarkStatus(const double & curLat, const double & curLon)
{
  if(GetStatus())
  {
    BookmarkCategory * cat = GetBmCategory(m_indexCurrentBmCat);
    const UserMark * bm = cat->GetUserMark(m_indexCurrentBm);
    ms::LatLon bmPosition = bm->GetLatLon();
    if(cat)
    {
      double const d = ms::DistanceOnEarth(curLat, curLon, bmPosition.lat, bmPosition.lon);
      if(d < MTRouteListManager::MT_DISTANCE_BOOKMARK_DONE)
      {
        return true;
      }
    }
  }
  return false;
}


bool MTRouteListManager::ChangeBookmarkOrder(size_t catIndex, size_t curBmIndex, size_t newBmIndex)
{
  bool res = BookmarkManager::ChangeBookmarkOrder(catIndex, curBmIndex, newBmIndex);
  if(res)
    m_indexCurrentBm = reorderCurrent(m_indexCurrentBm, curBmIndex, newBmIndex);
  return res;
}

int64_t MTRouteListManager::reorderCurrent(size_t current,size_t oldBmIndex, size_t newBmIndex)
{
  size_t res = current;
  if(oldBmIndex == res)
    res = newBmIndex;
  else if(res > oldBmIndex
    && res <= newBmIndex)
    res--;
  else if(res < oldBmIndex
    && res >= newBmIndex)
    res++;
  return res;
}

bool MTRouteListManager::optimiseCurrentCategory()
{
  if(m_indexCurrentBmCat < 0)
  {
    return false;
  }

  BookmarkCategory * bmCat = GetBmCategory(m_indexCurrentBmCat);

  {
    double lat, lon;
    m_framework.GetCurrentPosition(lat, lon);
    vector<m2::PointD> problemePoints(bmCat->GetUserMarkCount() + 1);
    LOG(LDEBUG, (bmCat->GetUserMarkCount() , "user_mark founds"));

    problemePoints[0] = MercatorBounds::FromLatLon(lat, lon); 

    for(int i = 0; i < bmCat->GetUserMarkCount(); i++)
    {
      const UserMark * user_mark = bmCat->GetUserMark(i);
      problemePoints[i + 1] = MercatorBounds::FromLatLon(user_mark->GetLatLon().lat, user_mark->GetLatLon().lon);
    }

    std::pair<std::list<size_t>, size_t> result;
    m_framework.OptimizeRoute(problemePoints, result);

    if(result.first.size() < 1)
      return false;

    // pop the first current position point
    result.first.pop_front();
    SortUserMarks(m_indexCurrentBmCat, result.first);
    m_indexCurrentBm = 0;

    m2::PolylineD points;
    points.Add(problemePoints[0]);
    for(int i = 0; i < bmCat->GetUserMarkCount(); i++)
    {
      const UserMark * user_mark = bmCat->GetUserMark(i);
      points.Add(user_mark->GetPivot());
    }

    Track::Params params;
    params.m_name = "new_track";
    params.m_colors.push_back({ 15.0f, dp::Color::Black()});
    
    //Track const track(points, params);
    bmCat->ClearTracks();
    bmCat->AddTrack(make_unique<Track>(points, params));
    BookmarkCategory::Guard guard(*bmCat);
    guard.m_controller.SetIsVisible(false);
    guard.m_controller.SetIsVisible(true);
  }
  
  return true;
  
}

/**
 * Redefinition du create pour voir passer les creation de catégories
 * et pouvoir les cacher par defaut sans avoir à toucher au code du
 * boomark_manager.hpp/cpp.
 **/
size_t MTRouteListManager::CreateBmCategory(string const & name)
{
  size_t index = BookmarkManager::CreateBmCategory(name);
  BookmarkCategory * bmCat = GetBmCategory(index);
  if(bmCat)
  {
    BookmarkCategory::Guard guard(*bmCat);
    guard.m_controller.SetIsVisible(false);
    bmCat->SaveToKMLFile();
  }

  m_framework.MT_SaveRoutingManager();
  return index;
}

/**
 * Redefinition du delete pour voir passer les suppresions de
 * catégories sans avoir à toucher au code du boomark_manager.hpp/cpp.
 **/
bool MTRouteListManager::DeleteBmCategory(size_t index)
{
  bool res = BookmarkManager::DeleteBmCategory(index);
  if(res == true)
  {
    if(index < m_indexCurrentBmCat)
      m_indexCurrentBmCat--;
    else if(index == m_indexCurrentBmCat)
    {
      m_indexCurrentBmCat = -1;
      m_indexCurrentBm = -1;
    }
  }

  m_framework.MT_SaveRoutingManager();
  return res;
}

/**
 * Redefinition du load pour voir passer les chargements de catégories
 * et pouvoir les cacher par defaut sans avoir à toucher au code du
 * boomark_manager.hpp/cpp.
 **/
void MTRouteListManager::LoadBookmark(string const & filePath)
{
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

