#!/bin/bash
export RAPIDMINER_HOME="/home/nelson/rm/RapidMiner5"
cd $RAPIDMINER_HOME/scripts
./rapidminer -f PROCESS /home/nelson/rm/REPOSITORY/webcrawler/D_quote_label.rmp
