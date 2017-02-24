#pragma once

#include "base/logging.hpp"
#include "map/user_mark_container.hpp"

/**
 * La classe MTRoutePlanning permet de gerer un planning à partir d'un BookmarkCategory.
 * TODO Initialisation de la liste en fonction des bookmarks (prise en compte des états etc...).
 * TODO Implémentation d'une machine à état.
 */
class BookmarkCategory;

class MTRoutePlanning
{

private :
  size_t m_index_count;
  size_t m_index_current;
  size_t m_index_start;
  size_t m_index_stop;
  bool m_valididy;

public :
  BookmarkCategory &m_bm_category;

  static const size_t INVALIDE_VALUE = numeric_limits<size_t>::max();

  /**
   * Constructor.
   */
  MTRoutePlanning(BookmarkCategory &bm_category);

  /**
   * Destructor.
   */
  ~MTRoutePlanning();

  /**
   * PlanningInitialisation.
   * Initialise le planning en fonction de l'état des bookmarks.
   */
  void PlanningInitialisation();

    /**
   * IsValidPlanning.
   * @return True if planning is valid.
   */
  bool IsValidPlanning();

  /**
  * SetCurrentPlanId.
  * @indexBm
  * @return "true" if planning set with new index.
  */
  bool SetCurrentBookmarkId(size_t indexBm);

  /**
   * StepNextPlanId.
   * @return Index suivant.
   */
  size_t StepNextBookmarkId();

   /**
   * StepPreviousPlanId.
   * @return Index précedant.
   */
  size_t StepPreviousBookmarkId();

  /**
   * GetCurrentPlanId.
   * @return Index courant.
   */
  size_t GetCurrentBookmarkId(){return m_index_current;};

  /**
   * GetPlanStartID.
   * @return Index du point de départ.
   */
  size_t GetBookmarkStartID(){return m_index_start;};

  /**
   * GetPlanStopID.
   * @return Index du point de d'arrivé.
   */
  size_t GetBookmarkStopID(){return m_index_stop;};

private:

/*###################################################
             BookmarkCategory helper.

  Mécanisme de suivit de la liste :
  Le BookmarkCategory hérite de UserMarkContainer, on
  accroche un listener au UserMarkContainer pour 
  pouvoir suivre les modifications sur celui ci.
  ###################################################*/

  void userMarkContainerUpdateListener(UserMarkEvent event)
  {
    switch(event.actionType)
    {
      case UserMarkEventType::CREATE_MARK :
        update_add(event.new_index);
        break;
      case UserMarkEventType::DELETE_MARK :
        update_delete(event.old_index);
        break;
      case UserMarkEventType::CLEAR_MARK :
        update_clear();
        break;
      case UserMarkEventType::MOVE_MARK :
        update_move(event.old_index, event.new_index);
        break;
      case UserMarkEventType::REVERSE_MARK :
        m_index_current = 0;
        break;
      default :
        break;
    }
  }

  size_t m_listener_key;

  void update_add(size_t index_new);

  void update_delete(size_t index_old);

  void update_clear();

  void update_move(size_t old_index, size_t new_index);
};
