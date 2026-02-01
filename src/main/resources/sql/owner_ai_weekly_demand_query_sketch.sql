-- Owner AI insights: weekly demand forecast query sketches.
-- Replace :restaurantId, :weekStart, :weekEnd, :weeksBack as needed.

-- 1) Baseline demand by weekday (avg per day over last N weeks).
SELECT
  DAYOFWEEK(d.slot_date) AS weekday,
  AVG(d.daily_count) AS avg_count
FROM (
  SELECT
    slot.slot_date,
    COUNT(*) AS daily_count
  FROM reservations r
  JOIN restaurant_reservation_slots slot ON slot.slot_id = r.slot_id
  WHERE slot.restaurant_id = :restaurantId
    AND r.status IN ('CONFIRMED', 'PREPAID_CONFIRMED', 'COMPLETED')
    AND slot.slot_date >= DATE_SUB(:weekStart, INTERVAL :weeksBack WEEK)
    AND slot.slot_date < :weekStart
  GROUP BY slot.slot_date
) d
GROUP BY DAYOFWEEK(d.slot_date)
ORDER BY weekday;

-- 2) Cafeteria menus for this week.
SELECT
  cm.user_id,
  cm.served_date,
  cm.main_menu_names,
  cm.raw_text
FROM cafeteria_menus cm
WHERE cm.served_date BETWEEN :weekStart AND :weekEnd
ORDER BY cm.served_date ASC;

-- 3) User preference keywords.
SELECT
  sm.user_id,
  s.keyword,
  s.is_liked
FROM speciality_mappings sm
JOIN specialities s ON s.speciality_id = sm.speciality_id;

-- 4) Public bookmark signal for this restaurant.
SELECT
  COUNT(*) AS public_bookmark_count
FROM bookmarks b
WHERE b.restaurant_id = :restaurantId
  AND b.is_public = 1;

-- 5) Approved bookmark link count (network density proxy).
SELECT
  COUNT(*) AS approved_link_count
FROM bookmark_links bl
WHERE bl.status = 'APPROVED';

-- 6) Company distribution of recent visitors.
SELECT
  u.company_name,
  COUNT(DISTINCT r.user_id) AS visitor_count
FROM reservations r
JOIN restaurant_reservation_slots slot ON slot.slot_id = r.slot_id
JOIN users u ON u.user_id = r.user_id
WHERE slot.restaurant_id = :restaurantId
  AND r.status IN ('CONFIRMED', 'PREPAID_CONFIRMED', 'COMPLETED')
  AND slot.slot_date >= DATE_SUB(:weekStart, INTERVAL :weeksBack WEEK)
  AND slot.slot_date < :weekStart
GROUP BY u.company_name
ORDER BY visitor_count DESC;
