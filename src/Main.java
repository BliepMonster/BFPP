import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: bfcomp <src> <dest>");
            return;
        }
        String src = Files.readString(Paths.get(args[0]));
        System.out.println(new Compiler().compile(new Scanner(src).scan()));
    }
}