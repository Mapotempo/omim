#ifndef MT_ROUTING_MANAGER_H
#define MT_ROUTING_MANAGER_H

#include "map/bookmark.hpp"
#include "map/bookmark_manager.hpp"

class MTRoutingManager
{
public:
  MTRoutingManager()
    :m_currentBmCat(-1),
    m_currentBm(-1),
    m_bmManager(){}

  MTRoutingManager(BookmarkManager * bmManager)
    :m_currentBmCat(-1),
    m_currentBm(-1)
  {
    m_bmManager = bmManager;
  }

  bool GetStatus();
  bool InitManager(int64_t indexBmCat, int64_t indexFirstBmToDisplay);
  int64_t GetCurrentBookmarkCategory() const {return m_currentBmCat;}

  void ResetManager();

  int64_t GetCurrentBookmark(){return m_currentBm;}
  int64_t StepNextBookmark();
  int64_t StepPreviousBookmark();

private :
  BookmarkManager * m_bmManager;
  int64_t m_currentBmCat;
  int64_t m_currentBm;
};

#endif // MT_ROUTING_MANAGER_H
