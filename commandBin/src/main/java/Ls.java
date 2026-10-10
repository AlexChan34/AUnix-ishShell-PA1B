import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.io.File;
import java.util.List;

/**
 * {@code ls [<path>...]}: lists files and directories. With no arguments, lists the current
 * directory.
 *
 * <p>Files print first (sorted by name), then directories (sorted by name), blank-line separated.
 * A directory's contents get a {@code "<dir>:"} header only when more than one argument was given.
 */
public class Ls extends ShellCommand {

    public Ls(String[] args) {
        super(args);
    }

    public static void main(String[] args) {
        ShellCommand.start(Ls.class, args);
    }

    @Override
    protected void runCommand() {
        String currentDir = System.getProperty("user.dir");
        String[] paths = cmdArgs.length > 0 ? cmdArgs : new String[] { currentDir };
        boolean showHeader = paths.length > 1;

        List<String> files = new ArrayList<>();
        List<String> dirs = new ArrayList<>();

        for (String p : paths) {
            Path path = Paths.get(p);
            try {
                checkExists(path);
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
                continue;
            }
            if (path.toFile().isDirectory()) {
                dirs.add(p);
            } else {
                files.add(p);
            }
        }

        files.sort(String::compareTo);
        dirs.sort(String::compareTo);

        for(String file : files){
            System.out.println(file);
        }

        if(!files.isEmpty() && !dirs.isEmpty()){
            System.out.println();
        }

        for (int i = 0; i < dirs.size(); i++) {
            if (i > 0) {
                System.out.println();
            }
            if (showHeader) {
                System.out.println(dirs.get(i) + ":");
            }
            File[] dirFiles = Paths.get(dirs.get(i)).toFile().listFiles();
            List<String> dirFileNames = new ArrayList<>();

            if (dirFiles != null) {
                for(File dirFile : dirFiles){
                    dirFileNames.add(dirFile.getName());
                }
            }

            dirFileNames.sort(String::compareTo);

            for (String name : dirFileNames) {
                System.out.println(name);
            }
        }
    }

}
