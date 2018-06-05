#!/bin/bash
echo "####### TOTAL OF RAW_MKTDATA"
mysql -uroot -proot -e "select count(*) from trademiner.raw_mktdata"
echo "####### LAST 15 MINS"
mysql -uroot -proot -e "select date_,time, count(*) from trademiner.raw_mktdata group by date_,time order by id desc limit 15"
echo "####### TOTAL OF RSS"
mysql -uroot -proot -e "select count(*) from trademiner.rss"
echo "####### LAST 15 MINS"
mysql -uroot -proot -e "select published , count(*) from trademiner.rss group by published order by published desc limit 15"

