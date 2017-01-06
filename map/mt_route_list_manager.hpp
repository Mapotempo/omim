#pragma once

#include "map/bookmark.hpp"
#include "map/bookmark_manager.hpp"

#include "std/function.hpp"
#include "std/unique_ptr.hpp"

#include "routing/async_optimizer.hpp"

#include "base/mutex.hpp"

class MTRouteListManager : public BookmarkManager
{
  using TOptimisationFinishFn = function<void (bool)>;
  /// Called to notify UI that mapotempo routing is deactivate;
  using TOptimisationProgessFn = function<void (float)>;

private :
  static const size_t MT_DISTANCE_BOOKMARK_DONE = 20;

private :
  Framework &m_framework;

  int64_t m_indexCurrentBmCat;
  int64_t m_indexCurrentBm;

  unique_ptr<routing::AsyncOptimizer> m_optimizer;
  TOptimisationFinishFn m_optimisationFinishFn;
  TOptimisationProgessFn m_optimisationProgressFn;

  // Guard mutex categoryManager and category
  // This is a guard on the current bookmark category to prevent another thread from deleting during optimization.
  mutable threads::Mutex m_routeListManagerMutex;
  unique_ptr<BookmarkCategory::Guard> optimizerBookmarkCategoryGuard;

public :
  MTRouteListManager(Framework & f) : BookmarkManager(f),
  m_framework(f),
  m_indexCurrentBmCat(-1),
  m_indexCurrentBm(-1),
  m_optimizer(nullptr),
  m_optimisationFinishFn(nullptr),
  m_optimisationProgressFn(nullptr),
  optimizerBookmarkCategoryGuard(nullptr){};

  ~MTRouteListManager() {};

  void SetRouter(unique_ptr<routing::IRouter> && router);

  // Route status manager
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

  // Optimisation
  bool optimiseCurrentRoute();
  void SetOptimisationListeners(TOptimisationFinishFn const & finishListener,
                                TOptimisationProgessFn const & progressListener);

public :
  // Override BookmarkManager virtual method
  bool DeleteBmCategory(size_t index);
  size_t CreateBmCategory(string const & name);
  void LoadBookmark(string const & filePath);
  bool ChangeBookmarkOrder(size_t catIndex, size_t curBmIndex, size_t newBmIndex);
};
