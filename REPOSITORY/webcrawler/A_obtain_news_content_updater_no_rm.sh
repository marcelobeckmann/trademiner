#!/bin/bash
java -cp .:"$RAPIDMINER_HOME/lib/plugins/rapidminer-Trademiner Extension-5.0.000.jar":"$RAPIDMINER_HOME/lib/jdbc/mysql-connector-java-5.1.17-bin.jar" com.rapidminer.operator.trademiner.acquisition.ContentUpdater


