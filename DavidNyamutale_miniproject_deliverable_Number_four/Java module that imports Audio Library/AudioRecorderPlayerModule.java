package audiorecorderplayermodule;

import audiolibrary.AudioLibrary;
import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * A simple project demonstrating the use of AudioLibrary for audio recording and playback.
 */
public class AudioRecorderPlayerModule {

    /**
     * Main method to run the audio recording and playback application.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        Connection connection = null;

        try {
            // Connect to MySQL database
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/audio_library?zeroDateTimeBehavior=convertToNull", "root", "");
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("\n1. Record audio");
                System.out.println("2. Play recorded audio");
                System.out.println("3. Exit");
                System.out.print("Enter your choice (1/2/3): ");

                if (scanner.hasNextLine()) {
                    String choice = scanner.nextLine().trim();

                    switch (choice) {
                        case "1":
                            System.out.print("Enter a name for the recording: ");
                            String name = scanner.nextLine().trim();
                            System.out.print("Enter the duration of the recording in seconds: ");
                            int duration = Integer.parseInt(scanner.nextLine().trim());
                            byte[] audioData = AudioLibrary.recordAudio(duration);
                            AudioLibrary.saveAudioToDatabase(connection, name, audioData);
                            break;
                        case "2":
                            System.out.print("Enter the name of the recording to play: ");
                            String playName = scanner.nextLine().trim();
                            AudioLibrary.playAudioFromDatabase(connection, playName);
                            break;
                        case "3":
                            connection.close();
                            System.exit(0);
                            break;
                        default:
                            System.out.println("Invalid choice. Please enter 1, 2, or 3.");
                    }
                } else {
                    System.out.println("No input found. Please try again.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
