package br.com.brunosalata.webcamsnapshotrecord;

import br.com.brunosalata.webcamsnapshotrecord.videoRecord.SnapshotCaptureService;
import br.com.brunosalata.webcamsnapshotrecord.videoRecord.SnapshotVideoGenerator;
import br.com.brunosalata.webcamsnapshotrecord.videoRecord.WebcamCaptureService;
import br.com.brunosalata.webcamsnapshotrecord.videoRecord.WebcamVideoGenerator;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamListener;
import com.github.sarxos.webcam.WebcamResolution;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

public class WebcamController implements Initializable {
    @FXML
    private ImageView imgView;
    private static Webcam webcam;
    @FXML
    private VBox snapshotArea;
    @FXML
    private TextField txtCounting, txtTime;
    @FXML
    private ComboBox<Webcam> cbWebcamOptions;
    private static volatile boolean videoRecording = false;
    private volatile boolean webcamOpened = false;
    private Dimension webcamResolution;
    private WebcamCaptureService captureService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        webcamResolution = WebcamResolution.VGA.getSize();
//        webcam.setViewSize(new Dimension(640,480));

        webcam = Webcam.getDefault();
        webcamListener();
        webcam.setViewSize(webcamResolution);
//        webcam.open();

        webcamList();

        cbWebcamOptions.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                webcam = newValue; // Define webcam with the selected one reference
                System.out.println("Webcam selecionada: " + webcam.getName());
            }
        });

    }

    private void webcamList() {
        List<Webcam> webcamOptions = Webcam.getWebcams();
        cbWebcamOptions.getItems().addAll(webcamOptions);
    }

    @FXML
    private void openWebcam() {
        webcam.open();
        webcamOpened = true;
        captureFrames();
    }

    private void webcamListener() {
        if (webcam != null) {
            webcam.addWebcamListener(new WebcamListener() {
                @Override
                public void webcamOpen(WebcamEvent webcamEvent) {
                    System.out.println("Webcam opened");
                }

                @Override
                public void webcamClosed(WebcamEvent webcamEvent) {
                    System.out.println("Webcam closed");
                }

                @Override
                public void webcamDisposed(WebcamEvent webcamEvent) {
                    System.out.println("Webcam Disposed");
                }

                @Override
                public void webcamImageObtained(WebcamEvent webcamEvent) {
//                    System.out.println("Image taken ");
                }
            });
        }
    }

    private void captureFrames() {

        Service<Void> captureService = new Service<>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() throws Exception {

                        AtomicLong frameCounter = new AtomicLong(0);
                        AtomicLong lastFPSCheckTime = new AtomicLong(System.currentTimeMillis());
                        long startTime = System.currentTimeMillis();
                        Image image = null;
                        while (webcamOpened && !Thread.currentThread().isInterrupted()) {
                            try {
                                // Capture a imagem da webcam ou da tela
                                image = SwingFXUtils.toFXImage(webcam.getImage(), null);
                                frameCounter.incrementAndGet();

                                long currentTime = System.currentTimeMillis();
                                long elapsedTime = currentTime - lastFPSCheckTime.get();
                                if (elapsedTime >= 1000) {
                                    double fps = (frameCounter.get() * 1000.0) / elapsedTime;
                                    System.out.println("FPS: " + fps);
                                    frameCounter.set(0);
                                    lastFPSCheckTime.set(currentTime);
                                }

                                // Atualiza as variáveis relacionadas à interface
                                updateUIValues(image, frameCounter.toString(), String.valueOf(currentTime - startTime));

                                Thread.sleep(30);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                e.printStackTrace();
                            }
                        }
                        webcam.close();
                        return null;
                    }
                };
            }
        };

        // Start Webcam Capture Service
        captureService.start();
    }

    /**
     * Receives the converted image and data to UI updates
     *
     * @param image    converted Image
     * @param counting just frame counting
     * @param time     time in milliseconds
     */
    private void updateUIValues(Image image, String counting, String time) {
        // Use Platform.runLater to update UI ImageView and Data elements
        Platform.runLater(() -> {
            txtCounting.setText(counting);
            txtTime.setText(time);

            // ImageView update with the captured image
            imgView.setImage(image);
        });
    }

    @FXML
    private void closeWebcam() {
        webcamOpened = false;
    }

//    **************************** WEBCAM PICTURE GENERATOR

    @FXML
    protected void WebcamPictureCapture() {

        boolean notOpenedWebcamMode = false;
        if (!webcamOpened) {
            notOpenedWebcamMode = true;
        }

        if (webcam != null) {
            if (notOpenedWebcamMode) {
                webcam.open();
            }
            try {
                ImageIO.write(webcam.getImage(), "PNG", new File("webcamImage.png"));
                if (notOpenedWebcamMode) {
                    webcam.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("Webcam disconnected");
        }
    }

//    **************************** SNAPSHOT PICTURE GENERATOR

    @FXML
    private void screenshot() {
        ScreenshotService screenshotService = new ScreenshotService();

        screenshotService.setOnSucceeded(event -> {
            // Processamento adicional fora do bloco Platform.runLater()
            BufferedImage bufferedImage = screenshotService.getBufferedImage();

            String caminhoDestino = "snapshot.png";
            // Salva a imagem no formato PNG no caminho especificado
            try {
                File fileDestino = new File(caminhoDestino);
                ImageIO.write(bufferedImage, "png", fileDestino);
                System.out.println("Imagem salva com sucesso em: " + caminhoDestino);

                // Agora você pode usar bufferedImage para operações adicionais, como a geração de um vídeo
                // Exemplo: gerarVideo(bufferedImage);
            } catch (IOException e) {
                System.out.println("Erro ao salvar a imagem: " + e.getMessage());
            }
        });

        // Start Screen Capture Service
        screenshotService.start();
    }

    /**
     * Classe do serviço para captura de tela
     */
    private class ScreenshotService extends Service<Void> {
        private BufferedImage bufferedImage;

        @Override
        protected Task<Void> createTask() {
            return new Task<>() {
                @Override
                protected Void call() {
                    CompletableFuture<WritableImage> snapshotFuture = new CompletableFuture<>();

                    // Screem capture inside the Platform.runLater() structure
                    Platform.runLater(() -> {
                        WritableImage image = snapshotArea.snapshot(new SnapshotParameters(), null);
                        snapshotFuture.complete(image);
                    });

                    WritableImage writableImage = snapshotFuture.join();
                    bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);

                    return null;
                }
            };
        }

        public BufferedImage getBufferedImage() {
            return bufferedImage;
        }
    }

//    **************************** WEBCAM VIDEO GENERATOR

    @FXML
    private void startWebcamRecord() {
        int numberOfFramesToCapture = 120; // Substitua pelo valor desejado
        int frameCaptureInterval = 30; // Substitua pelo valor desejado

        captureService = new WebcamCaptureService(numberOfFramesToCapture, frameCaptureInterval, webcam);
        WebcamVideoGenerationService generationService = new WebcamVideoGenerationService();

        captureService.setOnSucceeded(event -> {
            List<BufferedImage> capturedImages = captureService.getValue();
            System.out.println("Captura Webcam Finalizada");

            int count = 0;
            for(BufferedImage item : capturedImages){
                System.out.println(item);
                count++;
            }
            System.out.println(count);

            generationService.setCapturedImages(capturedImages);
            generationService.start();
        });

        generationService.setOnSucceeded(event -> {
            System.out.println("Geração do vídeo Webcam Finalizada");
            System.out.println("Vídeo gerado com sucesso!");
        });

        generationService.setOnFailed(event -> {
            System.out.println("Erro na geração do vídeo: " + event.getSource().getException());
        });

        captureService.start();
    }

    @FXML
    private void stopWebcamRecord() {
        if (captureService != null && captureService.isRunning()) {
            captureService.stopCapture();
            captureService.cancel();
        }
    }

    // Classe do serviço para geração do vídeo
    private static class WebcamVideoGenerationService extends Service<Void> {
        private List<BufferedImage> capturedImages;

        public void setCapturedImages(List<BufferedImage> capturedImages) {
            this.capturedImages = capturedImages;
        }

        @Override
        protected Task<Void> createTask() {
            return new Task<>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        System.out.println("Geração do vídeo Webcam Iniciada");
                        WebcamVideoGenerator.webcamGenerateVideo(capturedImages, "output.mp4", 30);
                        System.out.println("Geração do vídeo Webcam Concluída");
                    } catch (Exception e) {
                        // Relatar qualquer exceção que ocorra durante a geração do vídeo
                        e.printStackTrace();
                        throw new RuntimeException("Erro na geração do vídeo", e);
                    }
                    return null;
                }
            };
        }
    }

//    **************************** SNAPSHOT VIDEO GENERATOR

    @FXML
    private void startSnapshotRecord() {
        SnapshotCaptureService captureService = new SnapshotCaptureService();
        SnapshotVideoGenerationService generationService = new SnapshotVideoGenerationService();

        // Configurar parâmetros da captura
        captureService.setNumberOfSnapshotsToCapture(20); // Altere conforme necessário
        captureService.setHbOutputRecArea(snapshotArea); // Substitua 'suaHBox' pela sua instância de HBox
        captureService.setSnapshotCaptureInterval(Duration.seconds(2)); // Altere conforme necessário

        captureService.setOnSucceeded(event -> {
            List<WritableImage> capturedSnapshots = captureService.getValue();
            generationService.setCapturedSnapshots(capturedSnapshots);
            generationService.start();
        });

        generationService.setOnSucceeded(event -> {
            System.out.println("Vídeo gerado com sucesso!");
        });

        captureService.start();
    }

    @FXML
    private void stopSnapshotRecord() {

    }

    // Classe do serviço para geração do vídeo
    private static class SnapshotVideoGenerationService extends Service<Void> {
        private List<WritableImage> capturedSnapshots;

        public void setCapturedSnapshots(List<WritableImage> capturedSnapshots) {
            this.capturedSnapshots = capturedSnapshots;
        }

        @Override
        protected Task<Void> createTask() {
            return new Task<>() {
                @Override
                protected Void call() throws Exception {
                    SnapshotVideoGenerator.generateVideo(capturedSnapshots, "output.mp4");
                    return null;
                }
            };
        }
    }

    //    **************************** OTHERS FEATURES TO FUTURE


}