package is.trinit.sepp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

public class ConfigurationLoader {
    public static Optional<Properties> loadConfiguration() {
        var input = Program.class.getClassLoader().getResourceAsStream("config.properties");
        if (input == null) return Optional.empty();

        var props = new Properties();
        try {
            props.load(input);
        } catch (IOException e) {
            return Optional.empty();
        }

        return Optional.of(props);
    }
}
