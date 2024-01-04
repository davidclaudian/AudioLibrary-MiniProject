import jpype
#from JVMWrapper import JVMWrapper

# Start the JVM with the path to AudioLibrary.jar file
jpype.startJVM(classpath=[r"C:\Users\David Claudian\Documents\NetBeansProjects\AudioLibrary\dist\AudioLibrary.jar"])

# Import Java classes
AudioLibrary = jpype.JClass("audiolibrary.AudioLibrary")

# Connect to the MySQL database
connection = jpype.JClass("java.sql.DriverManager").getConnection("jdbc:mysql://localhost:3306/audio_library?zeroDateTimeBehavior=convertToNull", "root", "")

try:
    while True:
        print("\n1. Record audio")
        print("2. Play recorded audio")
        print("3. Exit")
        choice = input("Enter your choice (1/2/3): ").strip()

        if choice == "1":
            name = input("Enter a name for the recording: ").strip()
            duration = int(input("Enter the duration of the recording in seconds: ").strip())
            audio_data = AudioLibrary.recordAudio(duration)
            AudioLibrary.saveAudioToDatabase(connection, name, audio_data)
        elif choice == "2":
            play_name = input("Enter the name of the recording to play: ").strip()
            AudioLibrary.playAudioFromDatabase(connection, play_name)
        elif choice == "3":
            connection.close()
            jpype.shutdownJVM()
            exit(0)
        else:
            print("Invalid choice. Please enter 1, 2, or 3.")

except jpype.JException as ex:
    ex.printStackTrace()

except KeyboardInterrupt:
    # Handle keyboard interrupt (Ctrl+C) to ensure JVM shutdown
    connection.close()
    jpype.shutdownJVM()

except Exception as e:
    print("An error occurred:", str(e))

finally:
    jpype.shutdownJVM()
