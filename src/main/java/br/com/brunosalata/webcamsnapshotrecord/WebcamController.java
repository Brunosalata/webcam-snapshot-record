package br.com.brunosalata.webcamsnapshotrecord;

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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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
    private HBox hbOutputRecArea;
    @FXML
    private VBox root;
    @FXML
    private TextField txtCounting, txtTime;
    @FXML
    private ComboBox<Webcam> cbWebcamOptions;
    private static volatile boolean videoRecording = false;
    private volatile boolean webcamOpened = false;
    private Dimension webcamResolution;

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
                webcam = newValue; // Define a webcam selecionada como a referência da webcam
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

        // Iniciar o serviço de captura
        captureService.start();
    }

    /**
     * Recebe imagem convertida e vaores para atualização da UI
     * @param image Image convertida
     * @param counting contagem
     * @param time tempo
     */
    private void updateUIValues(Image image, String counting, String time) {
        // Use Platform.runLater para atualizar as variáveis da interface
        Platform.runLater(() -> {
            txtCounting.setText(counting);
            txtTime.setText(time);

            // Atualiza a ImageView com a imagem capturada
            imgView.setImage(image);
        });
    }


    @FXML
    private void closeWebcam() {
        webcamOpened = false;
    }

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

//    @FXML
//    private void screenshot() {
//        CompletableFuture<WritableImage> snapshotFuture = new CompletableFuture<>();
//
//        Platform.runLater(() -> {
//            WritableImage image = hbOutputRecArea.snapshot(new SnapshotParameters(), null);
//            snapshotFuture.complete(image);
//        });
//
//        snapshotFuture.thenAcceptAsync(image -> {
//            // Processamento adicional fora do bloco Platform.runLater()
//            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
//
//            String caminhoDestino = "snapshot.png";
//            // Salva a imagem no formato PNG no caminho especificado
//            try {
//                File fileDestino = new File(caminhoDestino);
//                ImageIO.write(bufferedImage, "png", fileDestino);
//                System.out.println("Imagem salva com sucesso em: " + caminhoDestino);
//            } catch (IOException e) {
//                System.out.println("Erro ao salvar a imagem: " + e.getMessage());
//            }
//        }, Platform::runLater);
//    }

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

        // Inicia o serviço de captura de tela
        screenshotService.start();
    }

    // Classe do serviço para captura de tela
    private class ScreenshotService extends Service<Void> {
        private BufferedImage bufferedImage;

        @Override
        protected Task<Void> createTask() {
            return new Task<>() {
                @Override
                protected Void call() throws Exception {
                    CompletableFuture<WritableImage> snapshotFuture = new CompletableFuture<>();

                    // Captura de tela dentro do bloco Platform.runLater()
                    Platform.runLater(() -> {
                        WritableImage image = hbOutputRecArea.snapshot(new SnapshotParameters(), null);
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



//    ****************************

    @FXML
    private void startRecord() {

    }

    private void recordScreen() {

    }

    @FXML
    private void stopRecord() {

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
}