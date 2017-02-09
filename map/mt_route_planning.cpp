#include "mt_route_planning.hpp"
#include "bookmark.hpp"

MTRoutePlanning::MTRoutePlanning(BookmarkCategory& bm_category):
  m_bm_category(bm_category),
  m_index_count(bm_category.GetUserMarkCount()),
  m_index_current(0),
  m_index_start(INVALIDE_VALUE),
  m_index_stop(INVALIDE_VALUE)
{
  m_listener_key = bm_category.AddEventListener([this](UserMarkEvent event){
    userMarkContainerUpdateListener(event);
  });
}

MTRoutePlanning::~MTRoutePlanning()
{
  m_bm_category.DeleteEventListener(m_listener_key);
}

void MTRoutePlanning::PlanningInitialisation()
{
  m_index_current = 0;
  m_index_start = INVALIDE_VALUE;
  m_index_stop = INVALIDE_VALUE;
}

bool MTRoutePlanning::SetCurrentPlanId(size_t indexBm)
{
  LOG(LDEBUG, ("SetCurrent with value : ", indexBm));

  bool res = false;
  if(indexBm < m_index_count)
  {
    m_index_current = indexBm;
    res = true;
  }
  return res;
}

size_t MTRoutePlanning::StepNextPlanId()
{
  m_index_current++;
  if(m_index_current >= m_index_count)
    m_index_current = 0;

  LOG(LDEBUG, ("StepNext with value : ", m_index_current));
  return m_index_current;
}

size_t MTRoutePlanning::StepPreviousPlanId()
{
  if(m_index_current == 0)
    m_index_current = m_index_count - 1;
  else
    m_index_current--;

  LOG(LDEBUG, ("StepPrevious with value : ", m_index_current));
  return m_index_current;
}

/*###################################################
             BookmarkCategory helper.
  ###################################################*/

void MTRoutePlanning::update_add(size_t index_new)
{
  m_index_count = m_bm_category.GetUserMarkCount();
  LOG(LDEBUG, ("update_add with value index_new : ", index_new));
}

void MTRoutePlanning::update_delete(size_t index_old)
{
  m_index_count = m_bm_category.GetUserMarkCount();

  if(index_old < m_index_count)
  {
    // On remet l'index courant du planning à zéro.
    if(index_old < m_index_current)
      m_index_current--;
    else if(index_old == m_index_current)
      m_index_current = 0;
  }
  LOG(LDEBUG, ("update_delete value index_old : ", index_old));
}

void MTRoutePlanning::update_clear()
{
  m_index_current = -1;
  m_index_count = -1;
  LOG(LDEBUG, ("update_clear with value m_index_current : ", m_index_current));
}

void MTRoutePlanning::update_move(size_t old_index, size_t new_index)
{
  m_index_count = m_bm_category.GetUserMarkCount();

  if(old_index == m_index_current)
    m_index_current = new_index;
  else if(m_index_current > old_index
    && m_index_current <= new_index)
    m_index_current--;
  else if(m_index_current < old_index
    && m_index_current >= new_index)
    m_index_current++;

  LOG(LDEBUG, ("update_move with value old_index : ", old_index, " - new_index :", new_index));
}
