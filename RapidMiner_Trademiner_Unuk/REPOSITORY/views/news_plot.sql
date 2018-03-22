/*
SELECT last as `last price`, utc_time_ as `utc time` , class AS label  FROM MKTDATA a WHERE  a.symbol='MSFT'   
 AND date_ =  STR_TO_DATE('2013/08/14','%Y/%m/%d')
 ORDER BY utc_time_
 */
 
 SELECT n.`next_trade_time` , n.`Title`  ,label FROM alignment a , rss n WHERE a.news_id=n.id AND a.ticket LIKE '%20150903'
AND a.symbol='BAC'   AND  n.`next_trade_date` = STR_TO_DATE('2013/07/10','%Y/%m/%d')
AND label<>0
ORDER BY n.next_trade_time



-- MSFT 2013-01-30

SELECT n.`next_trade_date` ,a.symbol,  COUNT(*) FROM alignment a , rss n WHERE a.news_id=n.id AND a.ticket LIKE '%20150903'
-- AND a.symbol='MSFT'   
-- AND  n.`next_trade_date` = STR_TO_DATE('2013/08/14','%Y/%m/%d')
AND n.next_trade_time >  STR_TO_DATE('13:30:00','%H:%i:%s')
AND label<>0  GROUP BY  n.next_trade_date, a.symbol
ORDER BY COUNT(*) DESC



SELECT label , COUNT(*) FROM
(
SELECT IF(label='-2','0',label) AS label  FROM alignment a , rss n WHERE a.news_id=n.id AND a.ticket LIKE '%20150903'
) m GROUP BY label