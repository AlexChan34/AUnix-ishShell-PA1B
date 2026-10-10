import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * {@code grep <pattern> [<file>...]}: prints every line, from the given files or from standard
 * input, that matches the {@link java.util.regex.Pattern regular expression} {@code pattern}.
 *
 * <p>With more than one file, each matching line is prefixed with {@code "<filename>:"}.
 */
public class Grep extends ShellCommand {

    public Grep(String[] args) {
        super(args);
    }

    public static void main(String[] args) {
        ShellCommand.start(Grep.class, args);
    }

    @Override
    protected void runCommand() throws IOException{
        if (cmdArgs.length == 0) {
            System.err.println("Usage: grep <pattern> [<file>...]");
            return;
        }
        
        String patternString = cmdArgs[0];
        String[] files = Arrays.copyOfRange(cmdArgs, 1, cmdArgs.length);
        Pattern pattern = Pattern.compile(patternString);

        try {
            if (files.length == 0) {
                String line;
                while ((line = readLineRaw(System.in)) != null) {
                    if (pattern.matcher(line).find()) {
                        System.out.print(line);
                    }
                }
                return;
            }

            boolean showFilename = files.length > 1;

            for (String file : files) {
                Path path = Paths.get(file);
                try (InputStream in = getFileInput(path)) {
                    String line;
                    while ((line = readLineRaw(in)) != null) {
                        if (pattern.matcher(line).find()) {
                            if (showFilename) {
                                System.out.print(file + ":");
                            }
                            System.out.print(line);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    System.err.println(e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println(cmdName + ": " + e.getMessage());
        }
    }
}
