-- 1 load 
INSERT INTO knn_und_processed ( neg_count,pos_count, link,symbol,classifier_label,label) 
SELECT DISTINCT (
SELECT COUNT FROM knn_und_aggr k WHERE k.link=m.tlink AND k.symbol=m.symbol AND k.label=0

) AS  neg_count , 
 (
SELECT COUNT FROM knn_und_aggr k WHERE k.link=m.tlink AND k.symbol=m.symbol AND k.label=1

) AS  pos_count ,

 tlink, symbol, class_label,label FROM (SELECT  
stripped_url(link) AS TLINK, a.symbol, a.label AS class_label,IF((SELECT COUNT(*) FROM knn_und k WHERE k.id = a.id )>0,0,1) AS label 
FROM  alignment a, rss n
WHERE a.status=1 AND a.news_id=n.id AND a.ticket LIKE '%20150903%' AND clean_content IS NOT NULL) m

-- 2 fill null fields
UPDATE knn_und_processed SET neg_count=0 WHERE neg_count IS NULL

-- 3 check quantities
SELECT symbol, SUM(POS_COUNT) FROM knn_und_processed WHERE neg_count=0 AND pos_count>0 GROUP BY symbol

-- 4 this is the blak list!
INSERT INTO knn_und_blacklist (symbol,link,pos_count) 
SELECT symbol,link, pos_count FROM knn_und_processed WHERE neg_count=0 AND pos_count>0 ORDER BY symbol,link

-- 5 this is how to obtain the alignment ids for the black list, according the ticket
SELECT COUNT(*) FROM (
SELECT a.id
FROM  alignment a, rss n
WHERE a.status=1 AND a.news_id=n.id AND a.symbol='CSCO' AND a.ticket LIKE '%20150903%' AND clean_content IS NOT NULL
AND stripped_url(n.link) IN (SELECT link FROM knn_und_blacklist b WHERE b.symbol=a.symbol)
) m


-- this is to generate csv files to R
"C:\Program Files\MySQL\MySQL Server 5.5\bin\mysql.exe" -uroot -proot -e"SELECT link,symbol,neg_count, pos_count, IF(classifier_label=0,'NO', 'YES') AS class_label, IF(label=0,'NO', 'YES') AS label FROM trademiner.knn_und_processed where symbol='DIS" >c:/var/tmp/output/ALL/dis.csv


-- I don't know what is the purpose of this...

INSERT INTO knn_und_aggr (link,symbol,label,COUNT)
SELECT link, symbol, label, COUNT(*) FROM knn_und_processed GROUP BY link, symbol,label

