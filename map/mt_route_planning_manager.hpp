#pragma once

#include "map/bookmark.hpp"
#include "map/bookmark_manager.hpp"

#include "std/function.hpp"
#include "std/unique_ptr.hpp"

#include "routing/async_optimizer.hpp"

//#include "base/mutex.hpp"

/**
 * La classe MTRouteListManager permet de gerer une liste de bookmark.
 * En héritant du BookmarkManager elle gere la suppression et le déplacement
 * du signet dans sa liste, ainsi que le chargement des bookmarks depuis les
 * kml au chargement de l'application.
 */
class MTRoutePlanningManager : public BookmarkManager
{
  using TOptimisationFinishFn = function<void (bool)>;
  /// Called to notify UI that mapotempo routing is deactivate;
  using TOptimisationProgessFn = function<void (float)>;

private :
  static const size_t MT_DISTANCE_BOOKMARK_DONE = 50;

private :
  Framework &m_framework;

  size_t m_indexCurrentBmCat;

  unique_ptr<routing::AsyncOptimizer> m_optimizer;
  TOptimisationFinishFn m_optimisationFinishFn;
  TOptimisationProgessFn m_optimisationProgressFn;

  // Guard mutex categoryManager and category
  // This is a guard on the current bookmark category to prevent another thread from deleting during optimization.
  mutable threads::Mutex m_routeListManagerMutex;

public :
  static const size_t INVALIDE_VALUE;

  MTRoutePlanningManager(Framework & f) : BookmarkManager(f),
  m_framework(f),
  m_indexCurrentBmCat(INVALIDE_VALUE),
  m_optimizer(nullptr),
  m_optimisationFinishFn(nullptr),
  m_optimisationProgressFn(nullptr){};

  ~MTRoutePlanningManager() {};

  void SetRouter(unique_ptr<routing::IRouter> && router);

  // Route status manager
  bool GetStatus();
  void StopManager();
  bool InitManager(size_t indexBmCat, size_t indexFirstBmToDisplay);
  void ResetManager();

  // Get current bookmark planning
  bool SetCurrentBookmark(size_t indexBm);
  size_t StepNextBookmark();
  size_t StepPreviousBookmark();
  size_t GetCurrentBookmarkCategory() const {return m_indexCurrentBmCat;}
  size_t GetCurrentBookmark();

  bool CheckCurrentBookmarkStatus(const double & curLat, const double & curLon);

  // Optimisation
  bool optimiseBookmarkCategory(size_t indexBmCat);
  void stopCurrentOptimisation();
  void SetOptimisationListeners(TOptimisationFinishFn const & finishListener,
                                TOptimisationProgessFn const & progressListener);

public :
  // Override BookmarkManager virtual method
  bool DeleteBmCategory(size_t index) override;
  size_t CreateBmCategory(string const & name) override;
  void LoadBookmark(string const & filePath) override;

private :
  void save_status();
};
