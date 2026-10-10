import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * {@code tail [-n <count>] [<file>...]}: prints the last {@code count} lines (default {@code 10})
 * of each file.
 *
 * <p>With more than one file, each file's output is preceded by an {@code "==> <file> <=="}
 * header line, with a blank line separating consecutive files' output (no header for a single
 * file or standard input).
 */
public class Tail extends ShellCommand {
    private static final int DEFAULT_COUNT = 10;

    public Tail(String[] args) {
        super(args);
    }

    public static void main(String[] args) {
        ShellCommand.start(Tail.class, args);
    }

    @Override
    protected void runCommand() throws IOException {
        int count = DEFAULT_COUNT;
        int fileStart = 0;

        if (cmdArgs.length > 0 && cmdArgs[0].equals("-n")) {
            if (cmdArgs.length < 2) {
                System.err.println("Usage: tail [-n <count>] [<file>...]");
                return;
            }
            try {
                count = Integer.parseInt(cmdArgs[1]);
            } catch (NumberFormatException e) {
                System.err.println("Usage: tail [-n <count>] [<file>...]");
                return;
            }
            fileStart = 2;
        }
        
        String[] files = Arrays.copyOfRange(cmdArgs, fileStart, cmdArgs.length);

        if (files.length == 0) {
            List<String> lines = new ArrayList<>();
            List<Boolean> hadNewline = new ArrayList<>();
            String line;
            while ((line = readLineRaw(System.in)) != null) {
                lines.add(line.replaceAll("[\r\n]+$", ""));
                hadNewline.add(line.endsWith("\n") || line.endsWith("\r"));
                if (lines.size() > count) {
                    lines.remove(0);
                    hadNewline.remove(0);
                }
            }
            for (int i = 0; i < lines.size(); i++) {
                if (hadNewline.get(i)) {
                    System.out.println(lines.get(i));
                } else {
                    System.out.print(lines.get(i));
                }
            }
            return;
        }

        boolean showHeader = files.length > 1;
        boolean firstEntry = true;

        for (String file : files) {
            Path path = Paths.get(file);
            try (InputStream in = getFileInput(path)) {
                if (!firstEntry) {
                    System.out.println();
                }
                firstEntry = false;

                if (showHeader) {
                    System.out.println("==> " + file + " <==");
                }

                List<String> lines = new ArrayList<>();
                List<Boolean> hadNewline = new ArrayList<>();
                String line;
                while ((line = readLineRaw(in)) != null) {
                    lines.add(line.replaceAll("[\r\n]+$", ""));
                    hadNewline.add(line.endsWith("\n") || line.endsWith("\r"));
                    if (lines.size() > count) {
                        lines.remove(0);
                        hadNewline.remove(0);
                    }
                }
                for (int i = 0; i < lines.size(); i++) {
                    if (hadNewline.get(i)) {
                        System.out.println(lines.get(i));
                    } else {
                        System.out.print(lines.get(i));
                    }
                }
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
