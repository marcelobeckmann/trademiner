package weka.agent;

import java.io.File;
import java.io.FilenameFilter;

public class ArffFilter implements FilenameFilter {

    public static final String TRAIN = "TRAIN";
    public static final String TEST = "TEST";
    public static final String PATTERN = ".arff";

    /*
     * (non-Javadoc)
     * 
     * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
     */
    @Override
    public boolean accept(File dir, String name) {

        if (name.endsWith(TRAIN+PATTERN)) {

            File file = new File(dir.getAbsolutePath() +"/" +name.replace(TRAIN, TEST));
         
            if (file.exists()) {

                return true;
            } else {
                return false;

            }

        } else
            return false;

    }

}
