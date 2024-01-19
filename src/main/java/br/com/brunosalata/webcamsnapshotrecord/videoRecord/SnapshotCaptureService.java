package br.com.brunosalata.webcamsnapshotrecord.videoRecord;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class SnapshotCaptureService extends Service<List<WritableImage>> {
    private int numberOfSnapshotsToCapture = 10; // Valor padrão
    private VBox snapshotArea = new VBox(); // Valor padrão
    private Duration snapshotCaptureInterval = Duration.seconds(1); // Valor padrão

    public void setNumberOfSnapshotsToCapture(int numberOfSnapshotsToCapture) {
        this.numberOfSnapshotsToCapture = numberOfSnapshotsToCapture;
    }

    public void setHbOutputRecArea(VBox snapshotArea) {
        this.snapshotArea = snapshotArea;
    }

    public void setSnapshotCaptureInterval(Duration snapshotCaptureInterval) {
        this.snapshotCaptureInterval = snapshotCaptureInterval;
    }

    @Override
    protected Task<List<WritableImage>> createTask() {
        return new Task<>() {
            @Override
            protected List<WritableImage> call() throws Exception {
                // Lógica de captura de snapshots usando os parâmetros configurados
                List<WritableImage> capturedSnapshots = new ArrayList<>();
                for (int i = 0; i < numberOfSnapshotsToCapture; i++) {
                    // Capturar snapshot e adicioná-lo à lista
                    updateProgress(i, numberOfSnapshotsToCapture);
                    updateMessage("Capturando snapshot " + (i + 1));

                    // Lógica de captura de snapshot
                    WritableImage snapshot = snapshotArea.snapshot(new SnapshotParameters(), null);
                    capturedSnapshots.add(snapshot);

                    Thread.sleep((long) snapshotCaptureInterval.toMillis()); // Ajuste conforme necessário
                }
                updateMessage("Captura de snapshots concluída.");
                return capturedSnapshots;
            }
        };
    }
}
