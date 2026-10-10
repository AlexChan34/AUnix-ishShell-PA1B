import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * {@code wc [<file>...]}: prints newline, word, and byte counts for each file, and a total line
 * if more than one file is specified. A word is a non-zero-length sequence of characters
 * delimited by white space.
 */
public class Wc extends ShellCommand {
    /**
     * Format specifier for printing output counts. Do not change.
     */
    public static final String formatSpecifier = "%8d %8d %8d %s%n";

    public Wc(String[] args) {
        super(args);
    }

    public static void main(String[] args) {
        ShellCommand.start(Wc.class, args);
    }

    @Override
    protected void runCommand() throws IOException {
        if (cmdArgs.length == 0) {
            long[] counts = countStream(System.in);
            System.out.printf(formatSpecifier, counts[0], counts[1], counts[2], "");
            return;
        }

        boolean showTotal = cmdArgs.length > 1;
        long totalNewlines = 0;
        long totalWords = 0;
        long totalBytes = 0;

        for (String file : cmdArgs) {
            Path path = Paths.get(file);

            try {
                checkExists(path);
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
                continue;
            }

            if (Files.isDirectory(path)) {
                try {
                    checkIsNotDir(path);
                } catch (IllegalArgumentException e) {
                    System.err.println(e.getMessage());
                }
                System.out.printf(formatSpecifier, 0, 0, 0, file);
                continue;
            }

            try (InputStream in = getFileInput(path)) {
                long[] counts = countStream(in);
                System.out.printf(formatSpecifier, counts[0], counts[1], counts[2], file);
                totalNewlines += counts[0];
                totalWords += counts[1];
                totalBytes += counts[2];
            }
        }

        if (showTotal) {
            System.out.printf(formatSpecifier, totalNewlines, totalWords, totalBytes, "total");
        }
    }

    private long[] countStream(InputStream stream) throws IOException {
        long newlines = 0;
        long words = 0;
        long bytes = 0;
        boolean inWord = false;

        String line;
        while ((line = readLineRaw(stream)) != null) {
            byte[] rawBytes = line.getBytes(StandardCharsets.UTF_8);
            bytes += rawBytes.length;

            for (byte b : rawBytes) {
                if (b == '\n') {
                    newlines++;
                }
                boolean isWhitespace = Character.isWhitespace((char) (b & 0xFF));
                if (isWhitespace) {
                    inWord = false;
                } else if (!inWord) {
                    words++;
                    inWord = true;
                }
            }
        }

        return new long[] { newlines, words, bytes };
    }
}
