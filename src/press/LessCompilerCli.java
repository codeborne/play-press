package press;

import play.Play;
import play.vfs.VirtualFile;

import java.io.File;

public class LessCompilerCli {
  public static void main(String[] args) {
    Play.applicationPath = new File(".");
    Play.roots.add(VirtualFile.open(new File(".")));
    for (File module : new File("modules").listFiles()) {
      if (module.isDirectory()) Play.roots.add(VirtualFile.open(module));
    }
    Play.roots.add(VirtualFile.open(new File("../ibank")));
    System.err.println("Play roots set to " + Play.roots);
    PlayLessEngine less = new PlayLessEngine();
    System.out.println(less.compile(new File(args[0]), true));
  }
}
