package is.trinit.sepp.latex;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class LatexGenerator {
    private static LatexGenerator s_instance;
    public static synchronized LatexGenerator getInstance() {
        if (s_instance == null)
            s_instance = new LatexGenerator();
        return s_instance;
    }

    private final Properties properties;
    private final Path formulaFilePath;

    private LatexGenerator() {
        formulaFilePath = Path.of(getApplicationPath(), "formula.tex");
        properties = new Properties();

        try {
            properties.load(LatexGenerator.class.getClassLoader().getResourceAsStream("latex.properties"));

            var formulaFileBytes = LatexGenerator.class.getClassLoader()
                    .getResourceAsStream("formula.tex")
                    .readAllBytes();
            Files.write(formulaFilePath, formulaFileBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getApplicationPath() {
        var rawPath = LatexGenerator.class.getProtectionDomain()
                .getCodeSource().getLocation().getPath();

        var jarFile = new File(rawPath);
        return jarFile.getParentFile().getPath();
    }

    public synchronized Optional<InputStream> formulaToImage(String formula) {
        var outputPath = Paths.get(formulaFilePath.getParent().toString(), "output.png");
        try {
            Files.delete(outputPath);
        } catch (IOException ignored) {
        }

        var proc = new ProcessBuilder();
        proc.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        proc.directory(formulaFilePath.getParent().toFile());
        proc.command("cmd.exe", String.format(properties.getProperty("pdflatex_command"), formula));

        var processCompleted = runAndDestroyProcess(proc);
        if (!processCompleted)
            return Optional.empty();

        proc = new ProcessBuilder();
        proc.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        proc.directory(formulaFilePath.getParent().toFile());
        proc.command("cmd.exe", properties.getProperty("magick_command"));

        processCompleted = runAndDestroyProcess(proc);
        if (!processCompleted || !Files.exists(outputPath))
            return Optional.empty();

        try {
            var output = new FileInputStream(outputPath.toFile());
            var returnStream = new ByteArrayInputStream(output.readAllBytes());

            return Optional.of(returnStream);
        } catch (IOException ignored) {
        }

        return Optional.empty();
    }

    private boolean runAndDestroyProcess(ProcessBuilder procBuilder) {
        Process process = null;
        try {
            process = procBuilder.start();
            process.waitFor(3, TimeUnit.SECONDS);

            var didNotExit = process.isAlive();
            process.destroyForcibly();
            return !didNotExit;
        } catch (InterruptedException | IOException ignored) {
            if (process != null) process.destroyForcibly();
            return false;
        }
    }
}
