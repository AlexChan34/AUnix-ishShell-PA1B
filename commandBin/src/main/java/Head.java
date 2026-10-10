import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * {@code head [-n <count>] [<file>...]}: prints the first {@code count} lines (default {@code 10})
 * of each file.
 *
 * <p>With more than one file, each file's output is preceded by an {@code "==> <file> <=="}
 * header line, with a blank line separating consecutive files' output (no header for a single
 * file or standard input).
 */
public class Head extends ShellCommand {
    private static final int DEFAULT_COUNT = 10;

    public Head(String[] args) {
        super(args);
    }

    public static void main(String[] args) {
        ShellCommand.start(Head.class, args);
    }

    @Override
    protected void runCommand() throws IOException {
        int count = DEFAULT_COUNT;
        int fileStart = 0;

        if (cmdArgs.length > 0 && cmdArgs[0].equals("-n")) {
            if (cmdArgs.length < 2) {
                System.err.println("Usage: head [-n <count>] [<file>...]");
                return;
            }
            try {
                count = Integer.parseInt(cmdArgs[1]);
            } catch (NumberFormatException e) {
                System.err.println("Usage: head [-n <count>] [<file>...]");
                return;
            }
            fileStart = 2;
        }
        
        String[] files = Arrays.copyOfRange(cmdArgs, fileStart, cmdArgs.length);

        if (files.length == 0) {
            String line;
            int linesRead = 0;
            while (linesRead < count && (line = readLineRaw(System.in)) != null){
                System.out.println(line.replaceAll("[\r\n]+$", ""));
                linesRead++;
            }
            return;
        }

        boolean showHeader = files.length > 1;
        boolean firstFile = true;

        for(String file : files){
            Path path = Paths.get(file);
            try(InputStream in = getFileInput(path)){
                if(!firstFile){
                    System.out.println();
                }
                firstFile = false;
                if(showHeader){
                    System.out.printf("==> " + file + " <==%n");
                }
                String line;
                int linesRead = 0;
                while (linesRead < count && (line = readLineRaw(in)) != null){
                    System.out.println(line.replaceAll("[\r\n]+$", ""));
                    linesRead++;
                }
            }
            catch (IllegalArgumentException e){
                System.err.printf(e.getMessage());
                System.err.println();
            }
        }
    }
}
