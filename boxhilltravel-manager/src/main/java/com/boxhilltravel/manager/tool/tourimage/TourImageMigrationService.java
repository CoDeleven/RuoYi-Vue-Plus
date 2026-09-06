package com.boxhilltravel.manager.tool.tourimage;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.system.domain.SysOssExt;
import org.dromara.system.domain.vo.SysOssVo;
import org.dromara.system.service.ISysOssService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * 线路图片迁移服务。
 */
@Service
public class TourImageMigrationService {

    private static final String TABLE_NAME = "holidays_tour";
    private static final String SELECT_SQL = """
        select id, cover_image, map_image
        from holidays_tour
        where deleted_at is null
          and (cover_image is not null or map_image is not null)
        """;

    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final ISysOssService sysOssService;

    public TourImageMigrationService(NamedParameterJdbcTemplate namedJdbcTemplate, JdbcTemplate jdbcTemplate,
                                     ISysOssService sysOssService) {
        this.namedJdbcTemplate = namedJdbcTemplate;
        this.jdbcTemplate = jdbcTemplate;
        this.sysOssService = sysOssService;
    }

    public MigrationSummary migrate(TourImageMigrationOptions options) {
        List<TourRow> rows = loadRows(options);
        Path reportFile = options.getReportFile();
        createReportParent(reportFile);

        MigrationSummary summary = new MigrationSummary(reportFile);
        try (java.io.BufferedWriter writer = Files.newBufferedWriter(reportFile, StandardCharsets.UTF_8)) {
            writer.write("tour_id,column,source_ref,source_kind,source_name,source_url,old_oss_id,new_oss_id,new_url,action,result,detail");
            writer.newLine();
            for (TourRow row : rows) {
                summary.totalRows++;
                boolean rowChanged = false;
                rowChanged |= processColumn(row.id, "cover_image", row.coverImage, options, writer, summary, row);
                rowChanged |= processColumn(row.id, "map_image", row.mapImage, options, writer, summary, row);
                if (rowChanged) {
                    summary.updatedRows++;
                }
            }
        } catch (IOException ex) {
            throw new ServiceException("Failed to write report file: " + reportFile, ex);
        }
        return summary;
    }

    private List<TourRow> loadRows(TourImageMigrationOptions options) {
        StringBuilder sql = new StringBuilder(SELECT_SQL);
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (options.getTourIds() != null && !options.getTourIds().isEmpty()) {
            sql.append(" and id in (:tourIds)");
            params.addValue("tourIds", options.getTourIds());
        }
        sql.append(" order by id");
        if (options.getLimit() != null) {
            sql.append(" limit ").append(options.getLimit());
        }
        List<TourRow> rows = namedJdbcTemplate.query(sql.toString(), params, (rs, rowNum) ->
            new TourRow(rs.getLong("id"), rs.getString("cover_image"), rs.getString("map_image")));
        if (rows == null) {
            return Collections.emptyList();
        }
        return rows;
    }

    private boolean processColumn(Long tourId, String columnName, String rawValue, TourImageMigrationOptions options,
                                  java.io.BufferedWriter writer, MigrationSummary summary, TourRow row) throws IOException {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            summary.skippedFields++;
            writeReport(writer, tourId, columnName, rawValue, "EMPTY", null, null, null, null, null, "SKIP", "EMPTY", "blank value");
            return false;
        }

        ImageSource source = resolveSource(rawValue);
        if (source == null) {
            summary.failedFields++;
            writeReport(writer, tourId, columnName, rawValue, "UNKNOWN", null, null, null, null, null, "SKIP", "ERROR", "unable to resolve source");
            if (options.getFailFast()) {
                throw new ServiceException("Unable to resolve image source: " + rawValue);
            }
            return false;
        }

        if (source.skipBeforeDownload()) {
            summary.skippedFields++;
            writeReport(writer, tourId, columnName, rawValue, source.kind, source.sourceName, source.sourceUrl,
                source.oldOssId, null, null, "SKIP", "NOT_PNG", "extension indicates non-png");
            return false;
        }

        byte[] sourceBytes;
        try {
            sourceBytes = source.downloadBytes();
        } catch (IOException ex) {
            summary.failedFields++;
            writeReport(writer, tourId, columnName, rawValue, source.kind, source.sourceName, source.sourceUrl,
                source.oldOssId, null, null, "FAIL", "DOWNLOAD_ERROR", ex.getMessage());
            if (options.getFailFast()) {
                throw new ServiceException("Download failed for " + rawValue, ex);
            }
            return false;
        }

        if (!TourImageMigrationSupport.isPng(sourceBytes)) {
            summary.skippedFields++;
            writeReport(writer, tourId, columnName, rawValue, source.kind, source.sourceName, source.sourceUrl,
                source.oldOssId, null, null, "SKIP", "NOT_PNG", "binary signature is not png");
            return false;
        }

        byte[] jpegBytes;
        try {
            jpegBytes = TourImageMigrationSupport.convertPngToJpeg(sourceBytes, options.getQuality());
        } catch (IOException ex) {
            summary.failedFields++;
            writeReport(writer, tourId, columnName, rawValue, source.kind, source.sourceName, source.sourceUrl,
                source.oldOssId, null, null, "FAIL", "CONVERT_ERROR", ex.getMessage());
            if (options.getFailFast()) {
                throw new ServiceException("Convert failed for " + rawValue, ex);
            }
            return false;
        }

        if (options.getDryRun()) {
            summary.convertedFields++;
            writeReport(writer, tourId, columnName, rawValue, source.kind, source.sourceName, source.sourceUrl,
                source.oldOssId, null, null, "DRY_RUN", "READY", "would upload " + jpegBytes.length + " bytes");
            return true;
        }

        String jpgName = TourImageMigrationSupport.asJpegName(source.sourceName, tourId, columnName);
        InMemoryMultipartFile file = new InMemoryMultipartFile("file", jpgName, "image/jpeg", jpegBytes);
        SysOssExt ext = buildExt(rawValue, tourId, columnName, source, jpegBytes.length);
        SysOssVo uploaded = sysOssService.upload(file, ext);
        if (uploaded == null || uploaded.getOssId() == null) {
            throw new ServiceException("Upload failed for " + rawValue);
        }
        String newRef = String.valueOf(uploaded.getOssId());
        updateTourColumn(tourId, columnName, newRef);
        summary.convertedFields++;
        writeReport(writer, tourId, columnName, rawValue, source.kind, source.sourceName, source.sourceUrl,
            source.oldOssId, uploaded.getOssId(), uploaded.getUrl(), "UPDATE", "OK", "uploaded and referenced by ossId");
        return true;
    }

    private void updateTourColumn(Long tourId, String columnName, String newRef) {
        String sql = "update " + TABLE_NAME + " set " + columnName + " = ?, update_time = now() where id = ?";
        jdbcTemplate.update(sql, newRef, tourId);
    }

    private ImageSource resolveSource(String rawValue) {
        String trimmed = rawValue.trim();
        if (trimmed.matches("\\d+")) {
            Long ossId = Long.parseLong(trimmed);
            SysOssVo vo = sysOssService.getById(ossId);
            if (vo == null) {
                return null;
            }
            return new ImageSource("OSS_ID", trimmed, ossId, vo.getUrl(), vo.getOriginalName(),
                vo.getFileSuffix(), () -> {
                    byte[] body = sysOssService.download(ossId).getBody();
                    if (body == null) {
                        throw new IOException("OSS body is empty for " + ossId);
                    }
                    return body;
                });
        }
        String sourceName = guessSourceName(trimmed);
        java.net.URI uri;
        try {
            uri = java.net.URI.create(trimmed);
        } catch (Exception ex) {
            return new ImageSource("URL", trimmed, null, trimmed, sourceName,
                TourImageMigrationSupport.detectExtension(sourceName), () -> {
                    throw new IOException("Invalid image URI: " + trimmed, ex);
                });
        }
        return new ImageSource("URL", trimmed, null, trimmed, sourceName,
            TourImageMigrationSupport.detectExtension(sourceName), () -> TourImageMigrationSupport.downloadBytes(uri));
    }

    private String guessSourceName(String rawValue) {
        try {
            String path = java.net.URI.create(rawValue).getPath();
            if (path == null || path.isEmpty()) {
                return rawValue;
            }
            int slash = path.lastIndexOf('/');
            return slash >= 0 ? path.substring(slash + 1) : path;
        } catch (Exception ex) {
            return rawValue;
        }
    }

    private SysOssExt buildExt(String rawValue, Long tourId, String columnName, ImageSource source, long fileSize) {
        SysOssExt ext = new SysOssExt();
        ext.setBizType("tour-image-migration");
        ext.setSource("systemImport");
        ext.setRefType(TABLE_NAME + "." + columnName);
        ext.setRefId(String.valueOf(tourId));
        ext.setRemark("migrated from " + source.kind + ":" + rawValue);
        ext.setFileSize(fileSize);
        ext.setContentType("image/jpeg");
        return ext;
    }

    private void createReportParent(Path reportFile) {
        try {
            Path parent = reportFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (IOException ex) {
            throw new ServiceException("Failed to create report directory: " + reportFile, ex);
        }
    }

    private void writeReport(java.io.BufferedWriter writer, Long tourId, String columnName, String sourceRef,
                             String sourceKind, String sourceName, String sourceUrl, Long oldOssId, Long newOssId,
                             String newUrl, String action, String result, String detail) throws IOException {
        writer.write(csv(tourId));
        writer.write(',');
        writer.write(csv(columnName));
        writer.write(',');
        writer.write(csv(sourceRef));
        writer.write(',');
        writer.write(csv(sourceKind));
        writer.write(',');
        writer.write(csv(sourceName));
        writer.write(',');
        writer.write(csv(sourceUrl));
        writer.write(',');
        writer.write(csv(oldOssId));
        writer.write(',');
        writer.write(csv(newOssId));
        writer.write(',');
        writer.write(csv(newUrl));
        writer.write(',');
        writer.write(csv(action));
        writer.write(',');
        writer.write(csv(result));
        writer.write(',');
        writer.write(csv(detail));
        writer.newLine();
        writer.flush();
    }

    private String csv(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value);
        if (text.indexOf(',') < 0 && text.indexOf('"') < 0 && text.indexOf('\n') < 0 && text.indexOf('\r') < 0) {
            return text;
        }
        return '"' + text.replace("\"", "\"\"") + '"';
    }

    private static final class TourRow {
        private final Long id;
        private final String coverImage;
        private final String mapImage;

        private TourRow(Long id, String coverImage, String mapImage) {
            this.id = id;
            this.coverImage = coverImage;
            this.mapImage = mapImage;
        }
    }

    private static final class ImageSource {
        private final String kind;
        private final String rawValue;
        private final Long oldOssId;
        private final String sourceUrl;
        private final String sourceName;
        private final String extension;
        private final Downloader downloader;

        private ImageSource(String kind, String rawValue, Long oldOssId, String sourceUrl, String sourceName,
                            String extension, Downloader downloader) {
            this.kind = kind;
            this.rawValue = rawValue;
            this.oldOssId = oldOssId;
            this.sourceUrl = sourceUrl;
            this.sourceName = sourceName;
            this.extension = extension == null ? "" : extension;
            this.downloader = downloader;
        }

        private boolean skipBeforeDownload() {
            return TourImageMigrationSupport.looksLikeJpegByName(sourceName)
                || "gif".equalsIgnoreCase(extension)
                || "webp".equalsIgnoreCase(extension);
        }

        private byte[] downloadBytes() throws IOException {
            return downloader.download();
        }
    }

    @FunctionalInterface
    private interface Downloader {
        byte[] download() throws IOException;
    }
}
