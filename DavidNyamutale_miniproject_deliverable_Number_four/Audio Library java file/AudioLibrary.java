package audiolibrary;

import javax.sound.sampled.*;
import java.io.*;
import java.sql.*;

/**
 * A library for audio recording and playback.
 */
public class AudioLibrary {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/audio_library?zeroDateTimeBehavior=convertToNull";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    /**
     * Initializes the database and creates the necessary table if it does not exist.
     */
    static {
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/audio_library?zeroDateTimeBehavior=convertToNull", "root", "");
            Statement statement = connection.createStatement();

            // Create a table to store audio recordings if it does not exist
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS audio_recordings (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(255) NOT NULL," +
                    "audio LONGBLOB NOT NULL)");

            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Records audio for the specified duration.
     *
     * @param duration The duration of the recording in seconds.
     * @return The recorded audio data.
     * @throws LineUnavailableException If a line matching the specified AudioFormat cannot be opened.
     * @throws IOException              If an I/O error occurs during recording.
     */
    public static byte[] recordAudio(int duration) throws LineUnavailableException, IOException {
        // Set up audio recording parameters
        AudioFormat audioFormat = new AudioFormat(44100, 16, 2, true, true);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);

        try (TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(info)) {
            // Open and start capturing audio
            targetLine.open(audioFormat);
            targetLine.start();

            System.out.println("Recording... Speak now.");

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];

            // Record audio for the specified duration
            long endTime = System.currentTimeMillis() + duration * 1000;
            while (System.currentTimeMillis() < endTime) {
                int count = targetLine.read(buffer, 0, buffer.length);
                if (count > 0) {
                    byteArrayOutputStream.write(buffer, 0, count);
                }
            }

            // Stop and close the audio line
            targetLine.stop();
            targetLine.close();

            System.out.println("Recording complete.");

            return byteArrayOutputStream.toByteArray();
        }
    }

    /**
     * Saves audio data to the database.
     *
     * @param connection The database connection.
     * @param name       The name of the recording.
     * @param audioData  The audio data to be saved.
     * @throws SQLException If a database access error occurs.
     */
    public static void saveAudioToDatabase(Connection connection, String name, byte[] audioData) throws SQLException {
        // Save audio data to the database
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO audio_recordings (name, audio) VALUES (?, ?)")) {
            preparedStatement.setString(1, name);
            preparedStatement.setBytes(2, audioData);
            preparedStatement.executeUpdate();

            System.out.println("Audio recording for '" + name + "' saved to the database.");
        }
    }

    /**
     * Plays audio from the database.
     *
     * @param connection The database connection.
     * @param name       The name of the recording to play.
     * @throws SQLException               If a database access error occurs.
     * @throws IOException                If an I/O error occurs during playback.
     * @throws UnsupportedAudioFileException If the audio file format is not supported.
     * @throws LineUnavailableException    If a line matching the specified AudioFormat cannot be opened.
     */
    public static void playAudioFromDatabase(Connection connection, String name)
            throws SQLException, IOException, UnsupportedAudioFileException, LineUnavailableException {
        // Play audio data from the database
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(
                    "SELECT audio FROM audio_recordings WHERE name = '" + name + "'");
            if (resultSet.next()) {
                byte[] audioData = resultSet.getBytes("audio");
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(audioData);

                AudioFormat audioFormat = new AudioFormat(44100, 16, 2, true, true);
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);

                try (SourceDataLine sourceLine = (SourceDataLine) AudioSystem.getLine(info)) {
                    // Open and start playing audio
                    sourceLine.open(audioFormat);
                    sourceLine.start();

                    System.out.println("Playing audio for '" + name + "'...");

                    byte[] buffer = new byte[1024];
                    int count;
                    // Read and play audio data
                    while ((count = byteArrayInputStream.read(buffer)) != -1) {
                        sourceLine.write(buffer, 0, count);
                    }

                    // Drain and stop the audio line
                    sourceLine.drain();
                    sourceLine.stop();

                    System.out.println("Playback complete.");
                }
            } else {
                System.out.println("No audio recording found for '" + name + "'.");
            }
        }
    }
}
