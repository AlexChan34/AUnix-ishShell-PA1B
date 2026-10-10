import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code sort [-r] [<file>...]}: reads every line from all given files (or standard input, if
 * none given), concatenated in argument order, and prints them sorted lexicographically —
 * ascending by default, descending with {@code -r}.
 *
 * <p>Unlike {@code head}/{@code tail}, files are merged into one sorted stream rather than
 * processed (and headed) independently — that's how real {@code sort} behaves too.
 */
public class Sort extends ShellCommand {

    public Sort(String[] args) {
        super(args);
    }

    public static void main(String[] args) {
        ShellCommand.start(Sort.class, args);
    }

    @Override
    protected void runCommand() throws IOException {
        boolean reverse = cmdArgs.length > 0 && cmdArgs[0].equals("-r");
        int fileStart = reverse ? 1 : 0;
        String[] files = java.util.Arrays.copyOfRange(cmdArgs, fileStart, cmdArgs.length);

        List<Line> lines = new ArrayList<>();

        if (files.length == 0) {
            readLines(System.in, lines);
        } else {
            for (String file : files) {
                Path path = Paths.get(file);
                try (InputStream in = getFileInput(path)) {
                    readLines(in, lines);
                } catch (IllegalArgumentException e) {
                    System.err.println(e.getMessage());
                }
            }
        }

        Collections.sort(lines, (a, b) -> a.text.compareTo(b.text));
        if (reverse) {
            Collections.reverse(lines);
        }


        for (Line line : lines) {
            if (line.hadNewline) {
                System.out.println(line.text);
            } else {
                System.out.print(line.text);
            }
        }
    }

    private void readLines(InputStream stream, List<Line> lines) throws IOException {
        String raw;
        while ((raw = readLineRaw(stream)) != null) {
            String text = raw.replaceAll("[\r\n]+$", "");
            boolean hadNewline = raw.endsWith("\n") || raw.endsWith("\r");
            lines.add(new Line(text, hadNewline));
        }
    }

    private static class Line {
        final String text;
        final boolean hadNewline;

        Line(String text, boolean hadNewline) {
            this.text = text;
            this.hadNewline = hadNewline;
        }
    }
}