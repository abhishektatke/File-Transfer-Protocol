
/**
 * Created by Priyanka on 8/1/2016.
 */
public enum FTPExecCommandEnum {
    LS("ls"),
    MKDIR("mkdir"),
    RD("rd"),
    CHMOD("chmod"),
    RM("rm"),
    RMDIR("rmdir"),
    MV("mv"),
    PUT("put"),
    LOGOUT("logout");

    private String command;

    FTPExecCommandEnum(String command){
        this.command=command;
    }

    public String command(){
        return command;
    }

}
