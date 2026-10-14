UPDATE menu_items
SET price = CASE id
  WHEN 'sesame' THEN 229.00
  WHEN 'sunrise' THEN 279.00
  WHEN 'lox' THEN 399.00
  WHEN 'garden' THEN 329.00
  WHEN 'tomato' THEN 329.00
  WHEN 'granola' THEN 259.00
  WHEN 'latte' THEN 169.00
  WHEN 'coldbrew' THEN 149.00
END
WHERE (id = 'sesame' AND price = 9.00)
   OR (id = 'sunrise' AND price = 12.00)
   OR (id = 'lox' AND price = 15.00)
   OR (id = 'garden' AND price = 13.00)
   OR (id = 'tomato' AND price = 13.00)
   OR (id = 'granola' AND price = 11.00)
   OR (id = 'latte' AND price = 6.00)
   OR (id = 'coldbrew' AND price = 5.00);