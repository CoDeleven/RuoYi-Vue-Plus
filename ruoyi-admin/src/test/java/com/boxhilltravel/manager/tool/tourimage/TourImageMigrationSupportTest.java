package com.boxhilltravel.manager.tool.tourimage;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;

@Tag("dev")
public class TourImageMigrationSupportTest {

    private static HttpServer server;
    private static int port;

    @BeforeAll
    static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.createContext("/not-found", exchange -> {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
        });
        server.start();
        port = server.getAddress().getPort();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void shouldDetectPngBytes() throws Exception {
        byte[] png = createPngBytes(true);
        Assertions.assertTrue(TourImageMigrationSupport.isPng(png));
    }

    @Test
    void shouldConvertTransparentPngToOpaqueJpeg() throws Exception {
        byte[] png = createPngBytes(true);
        byte[] jpeg = TourImageMigrationSupport.convertPngToJpeg(png, 0.8f);

        Assertions.assertFalse(TourImageMigrationSupport.isPng(jpeg));
        BufferedImage converted = ImageIO.read(new java.io.ByteArrayInputStream(jpeg));
        Assertions.assertNotNull(converted);
        Assertions.assertEquals(2, converted.getWidth());
        Assertions.assertEquals(2, converted.getHeight());
        Assertions.assertFalse(converted.getColorModel().hasAlpha());
    }

    @Test
    void shouldRejectJpegAsPng() throws Exception {
        byte[] png = createPngBytes(false);
        byte[] jpeg = TourImageMigrationSupport.convertPngToJpeg(png, 0.8f);
        Assertions.assertFalse(TourImageMigrationSupport.isPng(jpeg));
    }

    @Test
    void shouldFailOnDownloadError() {
        URI uri = URI.create("http://127.0.0.1:" + port + "/not-found");
        Assertions.assertThrows(IOException.class, () -> TourImageMigrationSupport.downloadBytes(uri));
    }

    private byte[] createPngBytes(boolean transparent) throws IOException {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(new Color(255, 0, 0, transparent ? 120 : 255));
            graphics.fillRect(0, 0, 2, 2);
            if (transparent) {
                graphics.setColor(new Color(0, 0, 255, 0));
                graphics.fillRect(0, 0, 1, 1);
            }
        } finally {
            graphics.dispose();
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return outputStream.toByteArray();
    }
}
