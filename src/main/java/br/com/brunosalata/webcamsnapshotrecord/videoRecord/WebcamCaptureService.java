package br.com.brunosalata.webcamsnapshotrecord.videoRecord;

import com.github.sarxos.webcam.Webcam;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class WebcamCaptureService extends Service<List<BufferedImage>> {
    private final int numberOfFramesToCapture;
    private final int frameCaptureInterval;
    private final Webcam webcam;
    private volatile boolean stopCaptureRequested = false;

    public WebcamCaptureService(int numberOfFramesToCapture, int frameCaptureInterval, Webcam webcam) {
        this.numberOfFramesToCapture = numberOfFramesToCapture;
        this.frameCaptureInterval = frameCaptureInterval;
        this.webcam = webcam;
    }

    public void stopCapture() {
        stopCaptureRequested = true;
    }

    @Override
    protected Task<List<BufferedImage>> createTask() {
        return new Task<>() {
            @Override
            protected List<BufferedImage> call() throws Exception {
                System.out.println("Captura Webcam Iniciada na Classe Separada");
                List<BufferedImage> capturedImages = new ArrayList<>();

                for (int i = 0; i < numberOfFramesToCapture && !stopCaptureRequested; i++) {
                    BufferedImage bufferedImage = captureImage();
                    capturedImages.add(bufferedImage);

                    Thread.sleep(frameCaptureInterval); // Ajuste conforme necessário
                }
                return capturedImages;
            }
        };
    }

    private BufferedImage captureImage() {
        if (webcam != null && webcam.isOpen()) {
            BufferedImage bufferedImage = new BufferedImage(webcam.getViewSize().width, webcam.getViewSize().height, BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D bGr = bufferedImage.createGraphics();
            bGr.drawImage(webcam.getImage(), 0, 0, null);
            bGr.dispose();

            return bufferedImage;
        } else {
            throw new RuntimeException("Webcam não está aberta ou é nula.");
        }
    }

//    private WritableImage captureImage() {
//        if (webcam != null && webcam.isOpen()) {
//            Image webcamImage = SwingFXUtils.toFXImage(webcam.getImage(), null);
//            return new WritableImage(webcamImage.getPixelReader(), (int) webcamImage.getWidth(), (int) webcamImage.getHeight());
////            // Substitua este bloco pelo que você precisa
////            BufferedImage bufferedImage = webcam.getImage();
////
////            // Converta para BufferedImage.TYPE_3BYTE_BGR
////            BufferedImage convertedImage = new BufferedImage(
////                    bufferedImage.getWidth(),
////                    bufferedImage.getHeight(),
////                    BufferedImage.TYPE_3BYTE_BGR);
////
////            // Desenhe a imagem convertida
////            convertedImage.getGraphics().drawImage(bufferedImage, 0, 0, null);
////
////            return SwingFXUtils.toFXImage(convertedImage, null);
//        } else {
//            throw new RuntimeException("Webcam não está aberta ou é nula.");
//        }
//    }
}
