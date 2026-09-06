package com.boxhilltravel.manager.tool.tourimage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 命令行参数。
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public final class TourImageMigrationOptions {

    private static final float DEFAULT_QUALITY = 0.85f;

    private Boolean dryRun;
    private Float quality;
    private Long tourId;
    private List<Long> tourIds;
    private Integer limit;
    private Boolean failFast;
    private String jdbcUrl;
    private String jdbcUsername;
    private String jdbcPassword;
    private String jdbcDriverClassName;
    private Path reportFile;
}
