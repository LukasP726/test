package Parts;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Log {
    private File file;
    private FileWriter writer;

    public Log(){
        try {
            file = new File("clientLog.txt");
            if (!file.createNewFile()) {
                file.delete();
                file = new File("clientLog.txt");
                file.createNewFile();
            }
            writer = new FileWriter("clientLog.txt");
        }
        catch(Exception exception){
            System.out.println("ERROR#1 - DATA SAVING");
            //exception.printStackTrace();
        }
    }

    public void toLog(String logData){
        try {
            this.writer.write(logData);
        }
        catch (IOException e) {
            System.out.println("ERROR#1 - DATA SAVING");
            e.printStackTrace();
        }
    }
    public void close(){
        try {
            this.writer.close();
        } catch (IOException e) {
            System.out.println("ERROR#1 - writer wont quit!");
            e.printStackTrace();
        }
    }
}
