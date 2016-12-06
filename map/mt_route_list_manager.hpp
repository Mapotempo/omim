#pragma once

#include "map/bookmark.hpp"
#include "map/bookmark_manager.hpp"

#include "std/function.hpp"
#include "std/unique_ptr.hpp"

class MTRouteListManager : public BookmarkManager
{
private :
  static const size_t MT_DISTANCE_BOOKMARK_DONE = 20;

private :
  int64_t reorderCurrent(size_t current,size_t oldBmIndex, size_t newBmIndex);
  int64_t m_indexCurrentBmCat;
  int64_t m_indexCurrentBm;
  Framework &m_framework;

public :
  MTRouteListManager(Framework & f);
  ~MTRouteListManager();

  bool GetStatus();
  void StopManager();
  bool InitManager(int64_t indexBmCat, int64_t indexFirstBmToDisplay);
  void ResetManager();

  bool SetCurrentBookmark(int64_t indexBm);
  int64_t StepNextBookmark();
  int64_t StepPreviousBookmark();
  int64_t GetCurrentBookmarkCategory() const {return m_indexCurrentBmCat;}
  int64_t GetCurrentBookmark(){return m_indexCurrentBm;}
  bool checkCurrentBookmarkStatus(const double & curLat, const double & curLon);

  bool optimiseCurrentCategory();

public :
  // Override BookmarkManager virtual method
  bool DeleteBmCategory(size_t index);
  size_t CreateBmCategory(string const & name);
  void LoadBookmark(string const & filePath);
  bool ChangeBookmarkOrder(size_t catIndex, size_t curBmIndex, size_t newBmIndex);
};
