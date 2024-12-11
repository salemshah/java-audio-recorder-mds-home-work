package org.salemshah.audiorecorder;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javax.imageio.ImageIO;


public class AudioController {
    @FXML
    private Label welcomeText;

    private TargetDataLine targetDataLine;

    @FXML
    protected void startRecording() {
        AudioFormat format = getAudioFormat();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("Line not supported");
            return;
        }

        try {
            targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
            targetDataLine.open(format);
            targetDataLine.start();

            Thread recordingThread = new Thread(() -> {
                AudioInputStream audioStream = new AudioInputStream(targetDataLine);
                File audioFile = new File("recording.wav");
                try {
                    AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, audioFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            recordingThread.start();
            welcomeText.setText("Recording started...");
        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    protected void stopRecording() {
        if (targetDataLine != null) {
            targetDataLine.stop();
            targetDataLine.close();
            welcomeText.setText("Recording stopped.");
        }
    }

    @FXML
    protected void getLocation() {

        String ipApiUrl = "https://api.ipify.org?format=json";
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest ipRequest = HttpRequest.newBuilder()
                .uri(URI.create(ipApiUrl))
                .build();

        client.sendAsync(ipRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(this::handleIpResponse)
                .exceptionally(e -> {
                    e.printStackTrace();
                    javafx.application.Platform.runLater(() -> welcomeText.setText("Failed to retrieve IP address."));
                    return null;
                });
    }

    @FXML
    protected void takeScreenshot() {
        // Get the current stage from any UI component
        Stage stage = (Stage) welcomeText.getScene().getWindow();

        // Create a WritableImage and snapshot the scene
        WritableImage image = stage.getScene().snapshot(null);

        // Define the output file for the screenshot
        File screenshotFile = new File("screenshot.png");
        try {
            // Convert the JavaFX image to a BufferedImage and write to disk
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", screenshotFile);
            welcomeText.setText("Screenshot saved as screenshot.png");
        } catch (IOException e) {
            e.printStackTrace();
            welcomeText.setText("Failed to save screenshot.");
        }
    }


    @FXML
    protected void takeFullScreenScreenshot() {
        try {
            // Get the screen size
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

            // Create a Robot instance to capture the screen
            Robot robot = new Robot();
            BufferedImage screenFullImage = robot.createScreenCapture(screenRect);

            // Save the captured image to a file
            File screenshotFile = new File("full_screenshot.png");
            ImageIO.write(screenFullImage, "png", screenshotFile);

            welcomeText.setText("Full screen screenshot saved as full_screenshot.png");
        } catch (Exception e) {
            e.printStackTrace();
            welcomeText.setText("Failed to capture full screen screenshot.");
        }
    }

    private void handleIpResponse(String responseBody) {

        try {
            JSONObject json = new JSONObject(responseBody);
            String ipAddress = json.getString("ip");
            String apiKey = "a22a5113a4c8c0";
            String locationApiUrl = "https://ipinfo.io/" + ipAddress + "?token=" + apiKey;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest locationRequest = HttpRequest.newBuilder()
                    .uri(URI.create(locationApiUrl))
                    .build();

            client.sendAsync(locationRequest, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(this::handleLocationResponse)
                    .exceptionally(e -> {
                        e.printStackTrace();
                        javafx.application.Platform.runLater(() -> welcomeText.setText("Failed to retrieve location."));
                        return null;
                    });

        } catch (Exception e) {
            e.printStackTrace();
            javafx.application.Platform.runLater(() -> welcomeText.setText("Failed to parse IP address."));
        }
    }

    private void handleLocationResponse(String responseBody) {

        try {
            JSONObject json = new JSONObject(responseBody);
            String location = json.getString("loc");
            String[] parts = location.split(",");
            String latitude = parts[0];
            String longitude = parts[1];

            javafx.application.Platform.runLater(() -> {
                welcomeText.setText("Latitude: " + latitude + ", Longitude: " + longitude);
            });
        } catch (Exception e) {
            e.printStackTrace();
            javafx.application.Platform.runLater(() -> {
                welcomeText.setText("Failed to retrieve location.");
            });
        }
    }

    private AudioFormat getAudioFormat() {
        float sampleRate = 16000;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }
}
