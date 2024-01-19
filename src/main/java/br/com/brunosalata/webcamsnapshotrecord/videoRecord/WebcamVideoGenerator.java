package br.com.brunosalata.webcamsnapshotrecord.videoRecord;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.IRational;

import java.awt.image.BufferedImage;
import java.util.List;

public class WebcamVideoGenerator {
    public static void webcamGenerateVideo(List<BufferedImage> images, String outputPath, int frameDurationMillis) {
        IMediaWriter writer = ToolFactory.makeWriter(outputPath);
        System.out.println("writer criado");
        writer.addVideoStream(0, 0, IRational.make(30, 1), images.get(0).getWidth(), images.get(0).getHeight());
        System.out.println("writer configurado");

        int i=0;
        long frameDuration = frameDurationMillis / 1000L; // Convertendo para microssegundos
        for (BufferedImage image : images) {
            if (image != null) {  // Verifique se a imagem não é nula
                System.out.println("imagem " + i + " não nula");
                try{
                    writer.encodeVideo(0, image, frameDuration, java.util.concurrent.TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("imagem " + i + " adicionada");
                i++;
            }
        }

        System.out.println("Geração do video Webcam Finalizada na Classe Separada");
        writer.close();
    }
}