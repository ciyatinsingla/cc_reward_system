package com.lms.ccrp.util;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

@Log4j2
@Component
public class RewardUtils {


    /**
     * Renames the source file by appending a timestamp in the format "yyyyMMdd'T'HHmmss"
     * before the file extension. This is typically used to archive or version the original file.
     * <p>
     * Example: if the original file name is "data.xlsx", after execution it becomes
     * "data_20250421T225830.xlsx".
     * </p>
     * <p>
     * The method logs the result of the rename operation using debug level logging.
     */
    public void notifySourceSystem(String sourceFile) {
        String timestamp = new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(new Date());
        File file = new File(sourceFile);
        String parent = file.getParent();
        String nameWithoutExt = file.getName().replaceFirst("[.][^.]+$", "");
        String extension = sourceFile.substring(sourceFile.lastIndexOf('.'));
        log.debug(file.renameTo(new File(parent, nameWithoutExt + "_" + timestamp + extension)));
    }

}
