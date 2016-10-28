#include "mt_route_list_manager.hpp"

#include "map/framework.hpp"
#include "map/bookmark.hpp"
#include "base/logging.hpp"

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

