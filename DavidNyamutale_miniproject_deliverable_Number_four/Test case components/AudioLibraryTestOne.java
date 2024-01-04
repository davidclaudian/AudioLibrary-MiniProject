import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import audiolibrary.AudioLibrary;
import java.sql.Statement;
//import javax.sound.sampled.LineUnavailableException;
//import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * A JUnit test class for testing the functionality of the AudioLibrary class.
 */
public class AudioLibraryTestOne {

    private Connection connection;

    @Before
public void setUp() throws SQLException {
    // Connect to your test database (modify the connection details accordingly)
    connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/audio_library_test?zeroDateTimeBehavior=convertToNull", "root", "");

    // Ensure the test table exists in the database
    try (Statement statement = connection.createStatement()) {
        // Create the audio_recordings_test table if it does not exist
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS audio_recordings_test (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(255) NOT NULL," +
                "audio LONGBLOB NOT NULL)");
    } catch (SQLException e) {
        e.printStackTrace();
        fail("Failed to set up the test environment: " + e.getMessage());
    }
}


    @After
    public void tearDown() throws SQLException {
        if (connection != null) {
            // Clean up the test database
            try {
                // Drop the test table
                connection.createStatement().executeUpdate("DROP TABLE IF EXISTS audio_recordings_test");
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Close the connection
            connection.close();
        }
    }

    /**
     * Test the recordAudio method of the AudioLibrary class.
     */
    @Test
    public void testRecordAudio() {
        try {
            // Record audio for 5 seconds
            byte[] audioData = AudioLibrary.recordAudio(5);
            
            // Check if the recorded audio data is not null and has a non-zero length
            assertNotNull("Recorded audio data should not be null", audioData);
            assertTrue("Recorded audio data should have a non-zero length", audioData.length > 0);
        } catch (Exception e) {
            // Fail the test if an unexpected exception occurs
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    /**
 * Test the saveAudioToDatabase and playAudioFromDatabase methods of the AudioLibrary class.
 */
    @Test
    public void testSaveAndPlayAudio() {
        try {
            // Test saving and playing audio
            String name = "testRecording";
            byte[] audioData = AudioLibrary.recordAudio(3);
            AudioLibrary.saveAudioToDatabase(connection, name, audioData);

            // Attempt to play the audio, checking for any exceptions
            try {
                AudioLibrary.playAudioFromDatabase(connection, name);
            } catch (Exception e) {
                fail("Exception should not be thrown while playing audio: " + e.getMessage());
            }
        } catch (Exception e) {
            // Fail the test if an unexpected exception occurs
            fail("Exception should not be thrown while saving audio: " + e.getMessage());
        }
    }


    /**
     * Test the playAudioFromDatabase method of the AudioLibrary class.
     */
    @Test
    public void testPlayAudioFromDatabase() {
        try {
            // Save example audio data to the database
            String name = "testRecording";
            byte[] audioData = new byte[]{1, 2, 3}; // Example audio data
            AudioLibrary.saveAudioToDatabase(connection, name, audioData);
            
            // Check if the audio can be played (not an exhaustive test)
            AudioLibrary.playAudioFromDatabase(connection, name);
        } catch (Exception e) {
            // Fail the test if an unexpected exception occurs
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }
}
