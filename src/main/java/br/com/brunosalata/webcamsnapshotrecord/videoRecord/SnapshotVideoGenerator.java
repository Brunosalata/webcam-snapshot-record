package br.com.brunosalata.webcamsnapshotrecord.videoRecord;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.IRational;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

import java.awt.image.BufferedImage;
import java.util.List;

public class SnapshotVideoGenerator {
    public static void generateVideo(List<WritableImage> snapshots, String outputPath) {
        IMediaWriter writer = ToolFactory.makeWriter(outputPath);
        writer.addVideoStream(0, 0, IRational.make(1, 1), (int) snapshots.get(0).getWidth(), (int) snapshots.get(0).getHeight());

        for (WritableImage snapshot : snapshots) {
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snapshot, null);
            writer.encodeVideo(0, bufferedImage, 1000, java.util.concurrent.TimeUnit.MILLISECONDS);
        }

        writer.close();
    }
}
