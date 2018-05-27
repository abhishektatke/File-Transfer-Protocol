//import com.google.common.annotations.VisibleForTesting;
import com.jcraft.jsch.*;

import java.io.*;
import java.util.Iterator;
import java.util.Properties;
import java.util.Scanner;
import java.util.Vector;


public class FTPSession {

    private Session session;
    private static ChannelSftp sftp;
    static Scanner inp = new Scanner(System.in);
    private static Properties prop = new Properties();
    private static InputStream input = null;


    /**
     * This method establishes the connection to FTP server
     * @param host
     * @param port
     * @param user
     * @param password
     * @return
     */
    public boolean login(String host, int port, String user, String password) {
        JSch jsch = new JSch();
        try {
            session = jsch.getSession(user, host, port);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(password);
            session.connect(3000);
            Channel channel = session.openChannel("sftp");
            sftp = (ChannelSftp) channel;
            sftp.connect(3000);
        } catch (JSchException e) {
            System.out.println(e.getMessage());
            return false;
        }

        return true;
    }

   /* @VisibleForTesting
    public boolean SetUpConnection(String remoteservername, String port, String uname, String password) {
        return login(remoteservername, Integer.parseInt(port)
                , uname, password);
    }*/

    /**
     * This method is executing the commands asked by user.
     * @param command
     */
    public void ExecCommand(String command) {
        if (session.isConnected() && FTPConnection.isLoggedIn) {

            if (command.equals(FTPExecCommandEnum.LS.command())) {
                Vector result = ExecLs();
                Iterator myiterator = result.iterator();
                while (myiterator.hasNext()) {
                    ChannelSftp.LsEntry current = (ChannelSftp.LsEntry) myiterator.next();
                    System.out.println(current.toString());
                }
            } else if (command.startsWith(FTPExecCommandEnum.MKDIR.command())) {
                ExecMkdir(command);
            } else if (command.equals(FTPExecCommandEnum.RD.command())) {
                try {
                    //GetSingleFileRemotely();
                    ExecGetFilesRemotely();
                } catch (Exception e) {
                    System.out.println("Error while getting the file remotely" + e);
                }
            } else if (command.startsWith(FTPExecCommandEnum.CHMOD.command())) {
                ExecChmod(command);
            } else if (command.contains(FTPExecCommandEnum.RM.command()) || command.contains(FTPExecCommandEnum.RMDIR.command())) {
                String[] commandArgs = command.split(" ");
                if (commandArgs.length > 1) {
                    String path = (String) commandArgs[1]; //.elementAt(1);
                    String commandExec = (String) commandArgs[0];
                    try {
                        if (commandExec.equals(FTPExecCommandEnum.RM.command())) {
                            sftp.rm(path);
                        } else if (commandExec.equals(FTPExecCommandEnum.RMDIR.command())) {
                            sftp.rmdir(path);
                        }

                        command = " ";
                    } catch (SftpException e) {
                        System.out.println(e.toString());
                    }
                }
                else{
                    System.out.println("Please give the file name or directory name to remove");
                }

            } else if (command.startsWith(FTPExecCommandEnum.MV.command())) {
                try {
                    ExecRename(command);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            } else if (command.equals(FTPExecCommandEnum.PUT.command())) {
                try {
                    ExecPutFilesRemotely();
                } catch (Exception e) {
                    System.out.println("Error while getting the file remotely" + e);
                }
            } else if (command.equals(FTPExecCommandEnum.LOGOUT.command())) {
                try {
                    Logout();
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
    }

    /**
     *This method downloads the remote file asked by the user
     * It first displays the list of files currently in the remote directory and then downloads them
     *
     */
    public static void ExecGetFilesRemotely() {
        String pwd = null;
        String pathToDownload = null, YOrNo = "n";

        OutputStream os;
        try {
            pwd = sftp.pwd();
            System.out.println("The present working directory in the remote server is" +pwd);
            System.out.println("Files/directory in current working directory include");
            FTPConnection.controller.ExecCommand("ls");

            System.out.println("Do you want to navigate to a different directory? [y/n]");
            YOrNo = inp.nextLine();
            while(!YOrNo.toLowerCase().equals("y") && !YOrNo.toLowerCase().equals("n"))
            {
                System.out.println("Enter valid option");

                YOrNo = inp.nextLine();
            }

            while(YOrNo.toLowerCase().equals("y")) {
                try {
                    YOrNo = "n";
                    System.out.println("Enter path to download");
                    pathToDownload = inp.nextLine();
                    sftp.cd(pathToDownload);
                    System.out.println("you are now in path" + sftp.pwd());
                    System.out.println("Files/directory in current working directory include");
                    FTPConnection.controller.ExecCommand("ls");
                    System.out.println("Do you want to navigate to a different directory? [y/n]");
                    YOrNo = inp.nextLine();
                    while(!YOrNo.toLowerCase().equals("y") && !YOrNo.toLowerCase().equals("n"))
                    {
                        System.out.println("Enter valid option");

                        YOrNo = inp.nextLine();
                    }

                }
                catch (Exception e)
                {
                    System.out.println(e.getMessage());
                    System.out.println("Do you want to continue navigating to a different directory? [y/n]");
                    YOrNo = inp.nextLine();

                    while(!YOrNo.toLowerCase().equals("y") && !YOrNo.toLowerCase().equals("n"))
                    {
                        System.out.println("Enter valid option");

                        YOrNo = inp.nextLine();
                    }

                }

            }
            System.out.println("Enter file name to download:");
            String fileToDownload = inp.nextLine();
            String filenames[]=null;
            if(fileToDownload.contains(" "));
            {
                filenames=fileToDownload.split(" ");
            }

            System.out.println("fileToDownload is"+fileToDownload);

            input = new FileInputStream("ftp.properties");

            // load  properties file
            prop.load(input);



            if (fileToDownload == null || fileToDownload.equals("")) {
                System.out.println("Filename cannot be blank.\n");
                return;
            }
            boolean check=false;
            String fDestDir=null;
            for(int i=0;i<filenames.length;i++) {
                fDestDir = prop.getProperty("directorytodownload");
                sftp.get(filenames[i], fDestDir);
                check=true;
            }
            if(check)
            {
                System.out.println("files successfully downloaded and saved in the path" +" "+ fDestDir);

            }
            else {
                System.out.println("files cannot be downloaded,error while downloading");

            }

        } catch (SftpException e)
        {
            System.out.println("There was an exception in downloading the file :" +e);
        }
        catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        catch(Exception e)
        {
            System.out.println("\nThere was an error in downloading the file" +e);
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     *This method uploads the remote files asked by the user
     * It first displays the list of file
     * s currently in the remote directory and then uploads them
     *
     */
    public static void ExecPutFilesRemotely() {
        String pwd = null;
        String pathToUpload = null, YOrNo = "n";
        FTPSession ctr = new FTPSession();
        try {
            pwd = sftp.pwd();
            System.out.println("Please place the file to be uploaded in project folder location");
            System.out.println("The present working directory in the remote server is" +pwd);
            System.out.println("Files/directory in current working directory include");
            //ctr.ExecCommand("ls");
            FTPConnection.controller.ExecCommand("ls");
            System.out.println("Please place the file to be uploaded in project folder location");
            System.out.println("Do you want to navigate to a different directory? [y/n]");
            YOrNo = inp.nextLine();
            while(!YOrNo.toLowerCase().equals("y") && !YOrNo.toLowerCase().equals("n"))
            {
                System.out.println("Enter valid option");

                YOrNo = inp.nextLine();
            }

            while(YOrNo.toLowerCase().equals("y")) {
                try {
                    YOrNo = "n";
                    System.out.println("Enter path to upload");
                    pathToUpload = inp.nextLine();
                    sftp.cd(pathToUpload);
                    System.out.println("you are now in path" + sftp.pwd());
                    System.out.println("Files/directory in current working directory include");
                    ctr.ExecCommand("ls");
                    System.out.println("Do you want to navigate to a different directory? [y/n]");
                    YOrNo = inp.nextLine();
                    while(!YOrNo.toLowerCase().equals("y") && !YOrNo.toLowerCase().equals("n"))
                    {
                        System.out.println("Enter valid option");

                        YOrNo = inp.nextLine();
                    }

                }
                catch (Exception e)
                {
                    System.out.println("There was an error in navigating" +e.getMessage());
                    System.out.println("Please enter a valid path");
                    System.out.println("Do you want to continue navigating to a different directory? [y/n]");
                    YOrNo = inp.nextLine();

                    while(!YOrNo.toLowerCase().equals("y") && !YOrNo.toLowerCase().equals("n"))
                    {
                        System.out.println("Enter valid option");

                        YOrNo = inp.nextLine();
                    }

                }

            }
            System.out.println("Enter file/files to upload:");
            String fileToUpload = inp.nextLine();
            String filenames[]=null;
            if(fileToUpload.contains(" "));
            {
                filenames=fileToUpload.split(" ");
            }
            System.out.println("fileToUpload is"+fileToUpload);

            input = new FileInputStream("ftp.properties");

            // load  properties file
            prop.load(input);



            if (fileToUpload == null || fileToUpload.equals("")) {
                System.out.println("Filename cannot be blank.\n");
                return;
            }
            boolean check=false;
            String fDestDir=null;
            for(int i=0;i<filenames.length;i++) {
                sftp.put(filenames[i]);
                check=true;
            }
            if(check)
            {
                System.out.println("files successfully uploaded and saved in the path");

            }
            else {
                System.out.println("files cannot be uploaded,error while uploading");

            }

        } catch (SftpException e)
        {
            System.out.println("There was an exception in uploading the file :" +e);
        }
        catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        catch(Exception e)
        {
            System.out.println("\nThere was an error in uploading the file" +e);
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }





    /**
     *This method Lists the files/folder inside a directory
     * @return
     */
    public Vector ExecLs() {
        Vector vector = null;
        try {
            vector = sftp.ls(".");
        } catch (SftpException e) {
            System.out.println("ls exception");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return vector;
    }

    /**
     * This method is making the directory on remote
     * @param command
     * @return
     */
    public boolean ExecMkdir(String command) {
        String[] commandArgs = command.split(" ");
        try {
            if (commandArgs.length > 1) {
                sftp.mkdir(commandArgs[1]);
                System.out.println("created directory " + commandArgs[1]);
                return true;
            } else {
                System.out.println("Please provide the directory name");
                return false;
            }
        } catch (SftpException e) {
            System.out.println("Can not create directory. Either directory already exist or name contains invalid symbol. Please enter correct Directory name and try again ! ");
            return false;
        }
    }

    /**
     * Change Permission
     * @param command
     * @return
     */
    public boolean ExecChmod(String command){
        String[] commandArgs = command.split(" ");
        String filename =null;
        int permissionType=0;
        try {
            if (commandArgs.length > 2) {
                permissionType = Integer.parseInt(commandArgs[1],8);
                filename = commandArgs[2];
                sftp.chmod(permissionType,filename);
                return true;
            } else {
                System.out.println("Please provide the command properly");
                return false;
            }
        } catch (SftpException e) {
            System.out.println("Either the file "+ filename+" does not exist" + " or permissionType "+ permissionType+ " is wrong. \n" +
                    "Please try again with correct command." );
            return false;
        }catch (NumberFormatException e) {
            System.out.println("Either the file "+ filename+" does not exist" + " or permissionType "+ permissionType+ " is wrong. \n" +
                    "Please try again with correct command." );
            return false;
        }
    }

    /**
     * This method is closing the session
     */
    public void CloseSession() {
        if (session != null) {
            session.disconnect();
        }
    }

    /**
     * This method renames the file
     * @param command
     */
    public void ExecRename(String command) {
        String[] commandArgs = command.split(" ");
        try {
            if (commandArgs.length == 3) {
                if (!CheckFileExist(commandArgs[1]))
                    System.out.println("Old file does not exist.");
                else if (CheckFileExist(commandArgs[2]))
                    System.out.println("New file name exists already.");
                else {
                    sftp.rename(commandArgs[1], commandArgs[2]);
                    System.out.println("Change " + commandArgs[1] + " to " + commandArgs[2] + " successfully.");
                }
            } else {
                System.out.println("Please provide the old name and the new name.");
            }
        } catch (SftpException e) {

            System.out.println(e.getMessage());

        }

    }

    /**
     * This method checks if the file exists.
     * @param fileName
     * @return true if file exist
     *
     */
    public boolean CheckFileExist(String fileName) {
        boolean find = true;
        try {
            sftp.ls(fileName);
            System.out.println("exist");
        } catch (SftpException e) {
            find = false;
            //e.printStackTrace();
        }
        return find;

    }

    /*
    * This method logout
    * */
    private void Logout() {
        sftp.disconnect();
        System.out.println("You have been logged out!");
        FTPConnection.isLoggedIn = false;
        CloseSession();
    }


}