package com.boxhilltravel.manager.tool.tourimage;

import org.dromara.system.domain.vo.SysOssVo;
import org.dromara.system.service.ISysOssService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("dev")
public class TourImageMigrationServiceTest {

    @Mock
    private NamedParameterJdbcTemplate namedJdbcTemplate;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ISysOssService sysOssService;

    @TempDir
    Path tempDir;

    @Test
    void shouldMigrateDefaultTourImages() throws Exception {
        TourImageMigrationService service = new TourImageMigrationService(namedJdbcTemplate, jdbcTemplate, sysOssService);
        byte[] png = createPngBytes();
        when(namedJdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
            .thenAnswer(invocation -> {
                String sql = invocation.getArgument(0, String.class);
                MapSqlParameterSource params = invocation.getArgument(1, MapSqlParameterSource.class);
                Assertions.assertTrue(sql.contains("holidays_tour"));
                Assertions.assertTrue(sql.contains("cover_image"));
                Assertions.assertTrue(sql.contains("map_image"));
                Assertions.assertEquals(List.of(7L), params.getValue("ids"));
                @SuppressWarnings("rawtypes")
                RowMapper rowMapper = invocation.getArgument(2, RowMapper.class);
                ResultSet rs = mock(ResultSet.class);
                when(rs.getLong("id")).thenReturn(7L);
                when(rs.getString("cover_image")).thenReturn("123");
                when(rs.getString("map_image")).thenReturn("tour-banner.jpg");
                return List.of(rowMapper.mapRow(rs, 0));
            });
        SysOssVo source = new SysOssVo();
        source.setOssId(123L);
        source.setUrl("http://static.example.com/source.png");
        source.setOriginalName("source.png");
        source.setFileSuffix("png");
        when(sysOssService.getById(123L)).thenReturn(source);
        when(sysOssService.download(123L)).thenReturn(ResponseEntity.ok(png));
        SysOssVo uploaded = new SysOssVo();
        uploaded.setOssId(999L);
        uploaded.setUrl("http://static.example.com/new.jpg");
        when(sysOssService.upload(any(MultipartFile.class), any())).thenReturn(uploaded);

        TourImageMigrationOptions options = new TourImageMigrationOptions();
        options.setTourIds(List.of(7L));
        options.setReportFile(tempDir.resolve("tour.csv"));

        MigrationSummary summary = service.migrate(options);

        Assertions.assertEquals("done: rows=1, updatedRows=1, convertedFields=1, skippedFields=1, failedFields=0, report="
            + options.getReportFile(), summary.toConsoleLine());
        verify(jdbcTemplate).update(
            "update holidays_tour set cover_image = ?, update_time = now() where id = ?",
            "999",
            7L
        );
        verify(jdbcTemplate, never()).update(
            "update holidays_tour set map_image = ?, update_time = now() where id = ?",
            "999",
            7L
        );
    }

    @Test
    void shouldMigrateDestinationImageWithIdsFilter() throws Exception {
        TourImageMigrationService service = new TourImageMigrationService(namedJdbcTemplate, jdbcTemplate, sysOssService);
        byte[] png = createPngBytes();
        when(namedJdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
            .thenAnswer(invocation -> {
                String sql = invocation.getArgument(0, String.class);
                MapSqlParameterSource params = invocation.getArgument(1, MapSqlParameterSource.class);
                Assertions.assertTrue(sql.contains("holidays_destination"));
                Assertions.assertTrue(sql.contains("image"));
                Assertions.assertEquals(List.of(42L), params.getValue("ids"));
                @SuppressWarnings("rawtypes")
                RowMapper rowMapper = invocation.getArgument(2, RowMapper.class);
                ResultSet rs = mock(ResultSet.class);
                when(rs.getLong("id")).thenReturn(42L);
                when(rs.getString("image")).thenReturn("42");
                return List.of(rowMapper.mapRow(rs, 0));
            });
        SysOssVo source = new SysOssVo();
        source.setOssId(42L);
        source.setUrl("http://static.example.com/source.png");
        source.setOriginalName("destination.png");
        source.setFileSuffix("png");
        when(sysOssService.getById(42L)).thenReturn(source);
        when(sysOssService.download(42L)).thenReturn(ResponseEntity.ok(png));
        SysOssVo uploaded = new SysOssVo();
        uploaded.setOssId(1001L);
        uploaded.setUrl("http://static.example.com/destination.jpg");
        when(sysOssService.upload(any(MultipartFile.class), any())).thenReturn(uploaded);

        TourImageMigrationOptions options = new TourImageMigrationOptions();
        options.setTarget("destination");
        options.setIds(List.of(42L));
        options.setReportFile(tempDir.resolve("destination.csv"));

        MigrationSummary summary = service.migrate(options);

        Assertions.assertTrue(summary.toConsoleLine().contains("rows=1"));
        verify(jdbcTemplate).update(
            "update holidays_destination set image = ?, update_time = now() where id = ?",
            "1001",
            42L
        );
    }

    @Test
    void shouldSkipDatabaseUpdateDuringDryRun() throws Exception {
        TourImageMigrationService service = new TourImageMigrationService(namedJdbcTemplate, jdbcTemplate, sysOssService);
        when(namedJdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
            .thenAnswer(invocation -> {
                @SuppressWarnings("rawtypes")
                RowMapper rowMapper = invocation.getArgument(2, RowMapper.class);
                ResultSet rs = mock(ResultSet.class);
                when(rs.getLong("id")).thenReturn(9L);
                when(rs.getString("image")).thenReturn("9");
                return List.of(rowMapper.mapRow(rs, 0));
            });
        SysOssVo source = new SysOssVo();
        source.setOssId(9L);
        source.setUrl("http://static.example.com/source.png");
        source.setOriginalName("dry-run.png");
        source.setFileSuffix("png");
        when(sysOssService.getById(9L)).thenReturn(source);
        when(sysOssService.download(9L)).thenReturn(ResponseEntity.ok(createPngBytes()));

        TourImageMigrationOptions options = new TourImageMigrationOptions();
        options.setTarget("destination");
        options.setDryRun(true);
        options.setIds(List.of(9L));
        options.setReportFile(tempDir.resolve("dry-run.csv"));

        MigrationSummary summary = service.migrate(options);

        Assertions.assertTrue(summary.toConsoleLine().contains("convertedFields=1"));
        verify(sysOssService, never()).upload(any(MultipartFile.class), any());
        verifyNoInteractions(jdbcTemplate);
    }

    private byte[] createPngBytes() throws IOException {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(new Color(255, 0, 0, 120));
            graphics.fillRect(0, 0, 2, 2);
        } finally {
            graphics.dispose();
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return outputStream.toByteArray();
    }
}
