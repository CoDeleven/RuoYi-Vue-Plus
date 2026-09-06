package com.boxhilltravel.manager.tool.tourimage;

import java.nio.file.Path;

/**
 * 迁移统计结果。
 */
public final class MigrationSummary {

    private final Path reportFile;
    long totalRows;
    long updatedRows;
    long convertedFields;
    long skippedFields;
    long failedFields;

    MigrationSummary(Path reportFile) {
        this.reportFile = reportFile;
    }

    public String toConsoleLine() {
        return "done: rows=" + totalRows
            + ", updatedRows=" + updatedRows
            + ", convertedFields=" + convertedFields
            + ", skippedFields=" + skippedFields
            + ", failedFields=" + failedFields
            + ", report=" + reportFile;
    }
}
