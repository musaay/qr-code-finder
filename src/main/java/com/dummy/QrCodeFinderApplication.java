package com.dummy;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;
import lombok.extern.log4j.Log4j2;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

@Log4j2
@SpringBootApplication
public class QrCodeFinderApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(QrCodeFinderApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            Map<DecodeHintType, Object> tmpHintsMap = new EnumMap<>(DecodeHintType.class);
            tmpHintsMap.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            tmpHintsMap.put(DecodeHintType.POSSIBLE_FORMATS, EnumSet.allOf(BarcodeFormat.class));
            tmpHintsMap.put(DecodeHintType.PURE_BARCODE, Boolean.FALSE);
            byte[] pdfByteArray = convertPDFToByteArray();
            int pageNumberOfPdf = 10;
            for (int i = 0; i < pageNumberOfPdf; i++) {
                byte[] imageByteArray = convertPDFtoImage(i, pdfByteArray);
                InputStream in = new ByteArrayInputStream(imageByteArray);
                BufferedImage bImageFromConvert = ImageIO.read(in);
                LuminanceSource source = new BufferedImageLuminanceSource(bImageFromConvert);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                QRCodeMultiReader qrCodeMultiReader = new QRCodeMultiReader();

                Result[] results = qrCodeMultiReader.decodeMultiple(bitmap, tmpHintsMap);
                log.info("---- " + (i + 1) + ". page ----");
                for (Result result : results) {
                    log.info(result.getText());
                }
            }
        } catch (Exception e) {
            log.error("error", e);
        }
    }


    private static byte[] convertPDFtoImage(int pageIndex, byte[] bytesPDF) {
        InputStream targetStream = new ByteArrayInputStream(bytesPDF);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PDDocument document = null;
        try {
            document = PDDocument.load(targetStream);
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage bi = renderer.renderImage(pageIndex);
            ImageIO.write(bi, "png", baos);
            baos.flush();
        } catch (Exception e) {
            log.error("error", e);
        } finally {
            if (document != null) {
                try {
                    document.close();
                    baos.close();
                } catch (IOException e) {
                    log.error("error", e);
                }
            }
        }
        return baos.toByteArray();
    }

    private static byte[] convertPDFToByteArray() {
        InputStream inputStream = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            // filePath is the location of file
            inputStream = new FileInputStream("filePath");
            byte[] buffer = new byte[9192];
            baos = new ByteArrayOutputStream();

            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
        } catch (FileNotFoundException e) {
            log.error(e);
        } catch (IOException e) {
            log.error(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return baos.toByteArray();
    }
}

