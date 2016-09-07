#pragma once

#include "map/bookmark.hpp"
#include "map/bookmark_manager.hpp"

#include "std/function.hpp"
#include "std/unique_ptr.hpp"
class MTRoutingManager : public BookmarkManager
{
  Framework & m_framework;
public:
  MTRoutingManager(Framework & f);
  ~MTRoutingManager();

  bool GetStatus();
  bool InitMTRouteManager(int64_t indexBmCat, int64_t indexFirstBmToDisplay);
  int64_t GetCurrentBookmarkCategory() const {return m_indexCurrentBmCat;}

  void ResetManager();

  int64_t GetCurrentBookmark(){return m_indexCurrentBm;}
  bool SetCurrentBookmark(int64_t indexBm);
  int64_t StepNextBookmark();
  int64_t StepPreviousBookmark();

  // Redefine
  bool DeleteBmCategory(size_t index);
  size_t CreateBmCategory(string const & name);
  void LoadBookmark(string const & filePath);

private :
  int64_t m_indexCurrentBmCat;
  int64_t m_indexCurrentBm;
};
