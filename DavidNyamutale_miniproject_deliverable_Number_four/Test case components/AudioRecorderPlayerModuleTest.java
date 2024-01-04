package testTwo;

import audiorecorderplayermodule.AudioRecorderPlayerModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.sound.sampled.*;

import static org.junit.Assert.*;

public class AudioRecorderPlayerModuleTest {

    private Connection connection;
    private InputStream originalIn;

    @Before
    public void setUp() throws SQLException {
        // Connect to your test database (modify the connection details accordingly)
        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/audio_library_test?zeroDateTimeBehavior=convertToNull", "root", "");

        // Ensure the test table exists in the database
        try (java.sql.Statement statement = connection.createStatement()) {
            // No need to create the table here, as it has been created in AudioLibraryTest
        } catch (SQLException e) {
            e.printStackTrace();
            fail("Failed to set up the test environment: " + e.getMessage());
        }

        // Save original System.in to restore later
        originalIn = System.in;
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

        // Restore System.in
        System.setIn(originalIn);
    }

    /**
     *
     * @throws LineUnavailableException
     * @throws IOException
     * @throws UnsupportedAudioFileException
     */
    @Test
    public void testAudioRecorderPlayer() throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        String simulatedInput = "1\nIntegrationTestRecording\n3\n2\nIntegrationTestRecording\n3\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        AudioRecorderPlayerModule.main(null);
        assertTrue(outputStream.toString().contains("Recording complete."));
        assertTrue(outputStream.toString().contains("Playing audio for 'IntegrationTestRecording'..."));
        assertTrue(outputStream.toString().contains("Playback complete."));
    }
}
