package com.rapidminer.operator.trademiner.acquisition;
import java.sql.SQLException;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;

public class CustomContentUpdater  extends Operator {


		public CustomContentUpdater(OperatorDescription description) {
		super(description);
		
	}
		
		/**
		 * @param args
		 * @throws SQLException 
		 */

		
		@Override
		public void doWork() throws OperatorException {
			
			ContentUpdater contentUpdater = new ContentUpdater();
     		contentUpdater.execute();
					
		}
				
	}
