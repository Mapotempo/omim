#include "mt_routing_manager.hpp"
#include "base/logging.hpp"

bool MTRoutingManager::GetStatus()
{
  if(m_currentBmCat < 0)
    return false;
  return true;
}

bool MTRoutingManager::InitManager(int64_t indexBmCat, int64_t indexFirstBmToDisplay)
{
  BookmarkCategory * bmCat = m_bmManager->GetBmCategory(indexBmCat);
  if(bmCat == NULL || bmCat->GetUserMarkCount() <= indexFirstBmToDisplay)
  {
    return false;
  }

  // Hide all other category
  for(int i = 0; i < m_bmManager->GetBmCategoriesCount(); i++)
  {
      bool visibily = false;
      if(i == indexBmCat)
      {
        visibily = true;
      }
      BookmarkCategory * otherCat = m_bmManager->GetBmCategory(i);
      {
        BookmarkCategory::Guard guard(*otherCat);
        guard.m_controller.SetIsVisible(visibily);
      }
      otherCat->SaveToKMLFile();
  }

  m_currentBmCat = indexBmCat;
  m_currentBm = indexFirstBmToDisplay;
  return true;
}

void MTRoutingManager::ResetManager(){
  m_currentBmCat = -1;
  m_currentBm = -1;
}

int64_t MTRoutingManager::StepNextBookmark()
{
  BookmarkCategory * bmCat = m_bmManager->GetBmCategory(m_currentBmCat);

  m_currentBm++;
  if(bmCat && (m_currentBm >= bmCat->GetUserPointCount()))
    m_currentBm = 0;

  return GetCurrentBookmark();
}

int64_t MTRoutingManager::StepPreviousBookmark()
{
  BookmarkCategory * bmCat = m_bmManager->GetBmCategory(m_currentBmCat);

  m_currentBm--;
  if(bmCat && (m_currentBm < 0))
   m_currentBm = bmCat->GetUserPointCount() - 1;

  return GetCurrentBookmark();
}
