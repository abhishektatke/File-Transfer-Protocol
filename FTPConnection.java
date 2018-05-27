import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

/**
 * This class loads the property file containing login credentials
 * establishes the connection to FTP server by calling FTPSession
 * and asks for the command to input.
 */
public class FTPConnection {

    private static final int TIMEOUT = 3000;
    private static Scanner scanner = new Scanner(System.in).useDelimiter("\n");
    private static Properties properties = new Properties();
    private static InputStream inputstream = null;
    private static String userName = null;
    private static String passWord = null;

    public static boolean isLoggedIn = false;
    public static FTPSession controller = new FTPSession();


    /**
     * main method
     * @param args
     */
    public static void main(String[] args) {
        isLoggedIn = false;
        try {
            inputstream = new FileInputStream("ftp.properties");
            // load a properties file
            properties.load(inputstream);
            System.out.println("This program can be used to connect CAT PSU remotely");
            createsession(controller);

            userCommand(controller);
            controller.CloseSession();
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private static void createsession(FTPSession controller) {
        System.out.println("Please enter the user name: ");
        userName = scanner.next();
        System.out.println("Please enter the password: ");
        passWord = scanner.next();
        isLoggedIn = controller.login(properties.getProperty("remoteservername"), Integer.parseInt(properties.getProperty("port"))
                , userName, passWord);
        if (!isLoggedIn) {
            System.out.println("Connection fails");
            System.exit(1);
        }
        System.out.println("Some of the Available commands are: \nls: List all file and directories\n" +
                "mkdir: Create directory on remote server\n" +
                "rd: Get file from remote server\n" +
                "chmod: Change permissions on remote server\n" +
                "rm: Remove file or directory on remote server\n" +
                "mv: Rename file or directory on remote server\n" +
                "put: Put file on remote server\n" +
                "logout: log out\n");
    }



    /**
     *This method asks the user to provide the desired command
     * and perform the operation specific to command.
     * @param controller
     */
    public static void userCommand(FTPSession controller) {
        System.out.println("Please enter your command from the list:");
        String command = scanner.next();
        while (!command.equalsIgnoreCase("q")) {
            if (isLoggedIn){

            controller.ExecCommand(command);
            System.out.println("Please enter your command from the list:");
            command = scanner.next();
            }
            else{
                System.out.println("You are not logged in! Please login");
                createsession(controller);

            }

        }
    }
}