package com.boxhilltravel.manager.tool.tourimage;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Locale;

/**
 * 图片编解码与下载工具。
 */
public final class TourImageMigrationSupport {

    private static final byte[] PNG_MAGIC = new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

    private TourImageMigrationSupport() {
    }

    public static boolean isPng(byte[] bytes) {
        if (bytes == null || bytes.length < PNG_MAGIC.length) {
            return false;
        }
        for (int i = 0; i < PNG_MAGIC.length; i++) {
            if (bytes[i] != PNG_MAGIC[i]) {
                return false;
            }
        }
        return true;
    }

    public static byte[] downloadBytes(URI uri) throws IOException {
        URL url = uri.toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(true);
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(30000);
        connection.setRequestProperty("User-Agent", "TourImageMigration/1.0");
        int status = connection.getResponseCode();
        if (status >= 400) {
            throw new IOException("HTTP " + status + " for " + uri);
        }
        try (InputStream inputStream = connection.getInputStream()) {
            return readAll(inputStream);
        } finally {
            connection.disconnect();
        }
    }

    public static byte[] convertPngToJpeg(byte[] pngBytes, float quality) throws IOException {
        BufferedImage source = ImageIO.read(new java.io.ByteArrayInputStream(pngBytes));
        if (source == null) {
            throw new IOException("Unable to read PNG image");
        }
        BufferedImage rgb = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = rgb.createGraphics();
        try {
            graphics.setComposite(AlphaComposite.Src);
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
            graphics.drawImage(source, 0, 0, null);
        } finally {
            graphics.dispose();
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        try (ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(output)) {
            writer.setOutput(imageOutputStream);
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
            }
            writer.write(null, new IIOImage(rgb, null, null), param);
        } finally {
            writer.dispose();
        }
        return output.toByteArray();
    }

    public static String detectExtension(String sourceName) {
        if (sourceName == null || sourceName.trim().isEmpty()) {
            return "";
        }
        String fileName = fileNameOf(sourceName).toLowerCase(Locale.ROOT);
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1);
    }

    public static String asJpegName(String sourceName, Long tourId, String column) {
        String fileName = sourceName == null || sourceName.trim().isEmpty() ? null : fileNameOf(sourceName);
        if (fileName == null || fileName.trim().isEmpty()) {
            return "tour-" + tourId + "-" + column + ".jpg";
        }
        int dot = fileName.lastIndexOf('.');
        String base = dot < 0 ? fileName : fileName.substring(0, dot);
        if (base.trim().isEmpty()) {
            base = "tour-" + tourId + "-" + column;
        }
        return base + ".jpg";
    }

    public static boolean looksLikePngByName(String sourceName) {
        return "png".equalsIgnoreCase(detectExtension(sourceName));
    }

    public static boolean looksLikeJpegByName(String sourceName) {
        String extension = detectExtension(sourceName);
        return "jpg".equalsIgnoreCase(extension) || "jpeg".equalsIgnoreCase(extension);
    }

    private static String fileNameOf(String sourceName) {
        String candidate = sourceName;
        int query = candidate.indexOf('?');
        if (query >= 0) {
            candidate = candidate.substring(0, query);
        }
        int fragment = candidate.indexOf('#');
        if (fragment >= 0) {
            candidate = candidate.substring(0, fragment);
        }
        int slash = Math.max(candidate.lastIndexOf('/'), candidate.lastIndexOf('\\'));
        if (slash >= 0 && slash + 1 < candidate.length()) {
            candidate = candidate.substring(slash + 1);
        }
        return candidate;
    }

    private static byte[] readAll(InputStream inputStream) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int len;
        while ((len = inputStream.read(buffer)) >= 0) {
            output.write(buffer, 0, len);
        }
        return output.toByteArray();
    }
}
