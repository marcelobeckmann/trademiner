SELECT RSS.*  FROM RSS ,LINK WHERE  RSS.link_id=LINK.ID AND PUBLISHED >= STR_TO_DATE('2013/07/01','%Y/%m/%d') 
AND LINK.TYPE_NEW<>'GOOGLE' 
LIMIT 10000
 
 
 SELECT Published,LOCALTIME_, CONVERT_TZ(Published,'Brazil/East','UTC'),utc_time_ ,link FROM rss
 WHERE  id IN 
 (
204591, 224014 ,224076 , 230975 ,
225742 , 243507 ,264008 ,298292 ,
298928 ,291376 ,277541 ,224087 ,
369141 ,369192 
 
 )
 
 
 
 
SELECT date_, TIME, utc_time_ FROM mktdata WHERE date_ >= STR_TO_DATE('2013/03/08','%Y/%m/%d') 
ORDER BY date_, utc_time_ DESC LIMIT 100


SELECT STR_TO_DATE('14:30:00','%H:%i:%s') ;

-- 
SELECT YEAR(date_) AS YEAR,MONTH(date_) AS MONTH, MIN(utc_time_), MAX(utc_time_) FROM mktdata GROUP BY YEAR(date_),MONTH(date_)

-- ESSA EH A QUERY PARA DESCOBRIR OS DESAJUSTADOS DE ABERTURA, PENSAR SOBRE UTC E ET SIMULTANEOUS TIME
SELECT published, next_trade_date,UTC_TIME, next_trade_time FROM rss  WHERE published >= STR_TO_DATE('2013/01/01','%Y/%m/%d') AND  published <= STR_TO_DATE('2013/03/08','%Y/%m/%d')
AND utc_time_ <= STR_TO_DATE('14:30:00','%H:%i:%s') AND DAY(Published)<DAY(next_trade_date)
-- TODO COLOCAR FILTRO UTC_TIME_ < 14:30 , MUDAR ESSES PARA 14:30

SELECT CONCAT(YEAR(date_),MONTH(date_)) AS yydmm, MIN(utc_time_) FROM mktdata GROUP BY CONCAT(YEAR(date_), MONTH(date_))




-- -------------------------- nova investigacao sobre as cagadas de time zone

SELECT MONTH(date_) AS MONTH, MIN(TIME), MAX(TIME), MIN(utc_time_), MAX(utc_time_) FROM mktdata GROUP BY MONTH(date_)


SELECT DATE_, TIME,UTC_TIME_,CONVERT_TZ(ADD_TIME(DATE_,TIME),'America/New_York','UTC') AS UTC_ ,COUNT(*) FROM mktdata 
WHERE MONTH(DATE_)=4 AND DAY(DATE_)=26
GROUP BY DATE_,TIME, UTC_TIME_, CONVERT_TZ(TIME,'America/New_York','UTC')  ORDER BY DATE_ ,TIME




SELECT DATE_, TIME,UTC_TIME_,CONVERT_TZ(ADD_TIME(DATE_,TIME),'America/New_York','UTC') AS UTC_  FROM mktdata 
WHERE MONTH(DATE_)=4 AND DAY(DATE_)=26
  ORDER BY DATE_ ,TIME


SELECT TIME ,utc_time_, CONVERT_TZ(TIMESTAMP(date_,TIME),'Etc/GMT+4','UTC') FROM mktdata 
WHERE MONTH(DATE_)=6 AND DAY(DATE_)=20
ORDER BY date_, TIME
LIMIT 100


SELECT date_, TIME, utc_time_ FROM mktdata WHERE date_ >= STR_TO_DATE('2013/03/08','%Y/%m/%d') 
ORDER BY date_, utc_time_ DESC LIMIT 100


SELECT * FROM mysql.time_zone_name WHERE NAME LIKE '%ET%'
 
SELECT TIME(published), CONVERT_TZ(published,'Etc/GMT+2','UTC'), utc_time_ FROM rss WHERE MONTH(published)=1 AND YEAR(published) =2013 

SELECT utc_time_, COUNT(*) FROM rss GROUP BY utc_time_ ORDER BY 2 DESC


-- RESOLUCAO FINAL PARA MKTDATA
UPDATE mktdata SET utc_time_= CONVERT_TZ(TIMESTAMP(date_,TIME),'Etc/GMT+4','UTC')


-- RESOLUCAO FINAL PARA RSS
UPDATE RSS SET utc_time_=CONVERT_TZ(Published,'Etc/GMT+2','UTC') 
-- WHERE MONTH(published)=1 AND YEAR(published) =2013 

DELETE FROM ALIGNMENT WHERE TICKET LIKE '%20150508'

-- ----- AVERIGUACOES APOS AJUSTES 

SELECT E.* FROM EXPERIMENT_RESULT_OPTMIZE E, EXPERIMENT D WHERE 
D.ID = E.EXPERIMENT_ID AND D.DESCRIPTION LIKE '%20150425+_00_00_00%'

-- ANTES 
SELECT AVG((`avg auc`+`avg acc`+`avg fmeasure`)/3) FROM 
EXPERIMENT_RESULT_OPTMIZE E, EXPERIMENT D WHERE 
D.ID = E.EXPERIMENT_ID AND D.DESCRIPTION LIKE '%20150425+_00_00_00%'

-- 


SELECT AVG((`avg auc`+`avg acc`+`avg fmeasure`)/3) FROM 
EXPERIMENT_RESULT_OPTMIZE E, EXPERIMENT D WHERE 
D.ID = E.EXPERIMENT_ID AND D.DESCRIPTION LIKE '%20150425+_00_00_00%'



SELECT AVG(`avg auc`),AVG(`avg acc`),AVG(`avg fmeasure`) FROM 
EXPERIMENT_RESULT_OPTMIZE E, EXPERIMENT D WHERE 
D.ID = E.EXPERIMENT_ID AND (D.DESCRIPTION LIKE '%20150425+_00_00_00%'
OR e.experiment_id IN (198))


-- resultados finais
SELECT  experiment_id,SUBSTR(e.description,1,50) AS DESCR, 
AVG(`avg auc`) , AVG(`avg acc`) , AVG(`avg fmeasure`)  FROM 
EXPERIMENT_RESULT_auc5_djia er , experiment e WHERE 
er.experiment_id=e.id 
GROUP BY experiment_id,DESCR





--
SELECT symbol FROM 
EXPERIMENT_RESULT_auc4 er , experiment e WHERE 
er.experiment_id=e.id AND
e.description LIKE '%%{experiment_description_}%' AND er.experiment_id = (SELECT MAX(id) FROM experiment)
AND `avg fmeasure`>0.55




--

-- THE BEST EXPERIMENT RESULT EVER!
SELECT DISTINCT EXPERIMENT_ID, MAX(avg_fmeasure) maxt_fmeasure , ROUND(AVG(avg_fmeasure),4) AS avgt_fmeasure, ROUND(STD(avg_fmeasure),4) stdev_fmeasure FROM (
SELECT experiment_id, ROUND(`avg fmeasure`,4) AS avg_fmeasure FROM EXPERIMENT_RESULT_auc2 er WHERE `avg fmeasure`>0.60 UNION ALL
SELECT experiment_id, ROUND(`avg fmeasure`,4) AS avg_fmeasure FROM EXPERIMENT_RESULT_auc3 er WHERE `avg fmeasure`>0.60 UNION ALL
SELECT experiment_id, ROUND(`avg fmeasure`,4) AS avg_fmeasure FROM EXPERIMENT_RESULT_auc4 er WHERE `avg fmeasure`>0.60
) M GROUP BY EXPERIMENT_ID ORDER BY  avgt_fmeasure DESC


SELECT * FROM EXPERIMENT_RESULT_auc4 WHERE experiment_id=205 
-- ---------------------------------------------------------

SELECT 'all_avg' AS symbol, experiment_id , AVG(avg_auc) AS avg_auc, AVG(avg_acc) AS avg_acc, AVG(avg_fmeasure) AS avg_fmeasure, STD(avg_fmeasure) AS std_fmeasure FROM (
SELECT symbol, experiment_id,SUBSTR(e.description,1,50) AS DESCR, 
ROUND(`avg auc`,4) AS avg_auc , ROUND(`avg acc`,4) AS avg_acc , ROUND(`avg fmeasure`,4) AS avg_fmeasure FROM 
EXPERIMENT_RESULT_auc4 er , experiment e WHERE 
er.experiment_id=e.id AND
er.EXPERIMENT_ID =   (SELECT MAX(id) FROM experiment)
  AND `avg fmeasure`>0.4
) M
UNION 
SELECT symbol, experiment_id, 
ROUND(`avg auc`,4) AS avg_auc , ROUND(`avg acc`,4) AS avg_acc , ROUND(`avg fmeasure`,4) AS avg_fmeasure, 0 AS std_fmeasure FROM 
EXPERIMENT_RESULT_auc4 er , experiment e WHERE 
er.experiment_id=e.id AND
er.EXPERIMENT_ID =    (SELECT MAX(id) FROM experiment)

  AND `avg fmeasure`>0.4


SELECT * FROM EXPERIMENT WHERE ID=260