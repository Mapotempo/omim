#include "mt_route_manager.hpp"

#include "map/bookmark.hpp"
#include "base/logging.hpp"

MTRoutingManager::MTRoutingManager(Framework & f)
  : BookmarkManager(f)
    ,m_framework(f)
    ,m_indexCurrentBmCat(-1)
    ,m_indexCurrentBm(-1)
{
}

MTRoutingManager::~MTRoutingManager()
{
}

bool MTRoutingManager::GetStatus()
{
  if(m_indexCurrentBmCat < 0)
    return false;
  return true;
}

bool MTRoutingManager::InitMTRouteManager(int64_t indexBmCat, int64_t indexFirstBmToDisplay)
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
      {
        visibily = true;
      }
      BookmarkCategory * otherCat = GetBmCategory(i);
      {
        BookmarkCategory::Guard guard(*otherCat);
        guard.m_controller.SetIsVisible(visibily);
      }
      otherCat->SaveToKMLFile();
  }

  m_indexCurrentBmCat = indexBmCat;
  m_indexCurrentBm = indexFirstBmToDisplay;
  return true;
}

void MTRoutingManager::ResetManager(){
  m_indexCurrentBmCat = -1;
  m_indexCurrentBm = -1;
}

bool MTRoutingManager::SetCurrentBookmark(int64_t indexBm)
{
  bool res = false;
  BookmarkCategory * bmCat = GetBmCategory(m_indexCurrentBmCat);
  if(bmCat && indexBm < bmCat->GetUserPointCount() && indexBm >= 0)
  {
    m_indexCurrentBm = indexBm;
    res = true;
  }
  return res;
}

int64_t MTRoutingManager::StepNextBookmark()
{
  BookmarkCategory * bmCat = GetBmCategory(m_indexCurrentBmCat);

  m_indexCurrentBm++;
  if(bmCat && (m_indexCurrentBm >= bmCat->GetUserPointCount()))
    m_indexCurrentBm = 0;

  return GetCurrentBookmark();
}

int64_t MTRoutingManager::StepPreviousBookmark()
{
  BookmarkCategory * bmCat = GetBmCategory(m_indexCurrentBmCat);

  m_indexCurrentBm--;
  if(bmCat && (m_indexCurrentBm < 0))
    m_indexCurrentBm = bmCat->GetUserPointCount() - 1;

  return GetCurrentBookmark();
}

/**
 * Redefinition du create pour voir passer les creation de catégories
 * et pouvoir les cacher par defaut sans avoir à toucher au code du
 * boomark_manager.hpp/cpp.
 **/
size_t MTRoutingManager::CreateBmCategory(string const & name)
{
  size_t index = BookmarkManager::CreateBmCategory(name);
  BookmarkCategory * bmCat = GetBmCategory(index);
  if(bmCat)
  {
    BookmarkCategory::Guard guard(*bmCat);
    guard.m_controller.SetIsVisible(false);
    bmCat->SaveToKMLFile();
  }

  return index;
}

/**
 * Redefinition du delete pour voir passer les suppresions de
 * catégories sans avoir à toucher au code du boomark_manager.hpp/cpp.
 **/
bool MTRoutingManager::DeleteBmCategory(size_t index)
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
  return res;
}

/**
 * Redefinition du load pour voir passer les cahrgements de catégories
 * et pouvoir les cacher par defaut sans avoir à toucher au code du
 * boomark_manager.hpp/cpp.
 **/
void MTRoutingManager::LoadBookmark(string const & filePath)
{
  BookmarkManager::LoadBookmark(filePath);
  for(int i = 0; i < GetBmCategoriesCount(); i++)
  {
      BookmarkCategory * bmCat = GetBmCategory(i);
      BookmarkCategory::Guard guard(*bmCat);
      guard.m_controller.SetIsVisible(false);
      bmCat->SaveToKMLFile();
  }
}

