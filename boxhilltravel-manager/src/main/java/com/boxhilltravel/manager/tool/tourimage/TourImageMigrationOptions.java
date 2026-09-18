package com.boxhilltravel.manager.tool.tourimage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.util.List;

/**
 * 命令行参数。
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public final class TourImageMigrationOptions {

    private static final float DEFAULT_QUALITY = 0.85f;

    private String target;
    private List<Long> ids;
    private Boolean dryRun = false;
    private Float quality = DEFAULT_QUALITY;
    private Long tourId;
    private List<Long> tourIds;
    private Integer limit;
    private Boolean failFast = false;
    private String jdbcUrl;
    private String jdbcUsername;
    private String jdbcPassword;
    private String jdbcDriverClassName;
    private Path reportFile;
}
