import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code uniq [-c] [<file>]}: collapses runs of <b>adjacent</b> duplicate lines into a single
 * line — non-adjacent duplicates are left alone, same as real {@code uniq} (pair with
 * {@code sort} first to dedupe an entire file). With {@code -c}, each output line is prefixed
 * with the number of times it occurred in its run.
 *
 * <p>Unlike the other commands, {@code uniq} takes at most <b>one</b> file argument — that's a
 * real constraint of the actual Unix command, not a simplification for this project.
 */
public class Uniq extends ShellCommand {

    public Uniq(String[] args) {
        super(args);
    }

    public static void main(String[] args) {
        ShellCommand.start(Uniq.class, args);
    }

    @Override
    protected void runCommand() throws IOException {
        boolean showCount = false;
        List<String> fileArgs = new ArrayList<>();

        for (String arg : cmdArgs) {
            if (arg.equals("-c")) {
                showCount = true;
            } else {
                fileArgs.add(arg);
            }
        }

        if (fileArgs.size() > 1) {
            System.err.println("Usage: uniq [-c] [<file>]");
            return;
        }

        if (fileArgs.isEmpty()) {
            printUniqueLines(System.in, showCount);
            return;
        }

        Path path = Paths.get(fileArgs.get(0));
        try (InputStream in = getFileInput(path)) {
            printUniqueLines(in, showCount);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
    }

    private void printUniqueLines(InputStream stream, boolean showCount) throws IOException {
        String previous = null;
        int runCount = 0;

        String raw;
        while ((raw = readLineRaw(stream)) != null) {
            String line = raw.replaceAll("[\r\n]+$", "");

            if (line.equals(previous)) {
                runCount++;
            } else {
                if (previous != null) {
                    printLine(previous, runCount, showCount);
                }
                previous = line;
                runCount = 1;
            }
        }

        if (previous != null) {
            printLine(previous, runCount, showCount);
        }
    }

    private void printLine(String line, int count, boolean showCount) {
        if (showCount) {
            System.out.println(count + " " + line);
        } else {
            System.out.println(line);
        }
    }
}