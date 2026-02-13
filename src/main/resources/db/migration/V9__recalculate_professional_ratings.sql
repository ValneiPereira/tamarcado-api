UPDATE professionals p
SET average_rating = sub.avg_rating,
    total_ratings = sub.total
FROM (
    SELECT r.professional_id,
           ROUND(AVG(r.rating)::numeric, 2) AS avg_rating,
           COUNT(*)::integer AS total
    FROM reviews r
    GROUP BY r.professional_id
) sub
WHERE p.id = sub.professional_id;
