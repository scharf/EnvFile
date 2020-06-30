package net.ashald.envfile.providers.dotenv;

import net.ashald.envfile.AbstractEnvVarsProvider;
import net.ashald.envfile.EnvFileErrorException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DotEnvFileParser extends AbstractEnvVarsProvider {

    public DotEnvFileParser(boolean shouldSubstituteEnvVar) {
        super(shouldSubstituteEnvVar);
    }

    @NotNull
    @Override
    protected Map<String, String> getEnvVars(@NotNull Map<String, String> runConfigEnv, @NotNull String path) throws EnvFileErrorException {
        Map<String, String> result = new LinkedHashMap<>();

        try {
            List<String> lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
            for (String l: lines) {
                String strippedLine = l.trim();
                // remove `export` at the beginning of the line if a variable follows
                strippedLine = strippedLine.replaceAll("^\\s*export\\s+(?=\\w)", "");
                if (!strippedLine.startsWith("#") && strippedLine.contains("=")) {
                    String[] tokens = strippedLine.split("=", 2);
                    String key = tokens[0];
                    String value = trim(tokens[1]);
                    result.put(key, value);
                }
            }
        } catch (IOException ex) {
            throw new EnvFileErrorException(ex);
        }

        return result;
    }

    private static String trim(String value) {
        String trimmed = value.trim();

        if ((trimmed.startsWith("\"") && trimmed.endsWith("\"")) || (trimmed.startsWith("'") && trimmed.endsWith("'")))
            return trimmed.substring(1, trimmed.length() - 1);

        return trimmed.replaceAll("\\s#.*$", "").replaceAll("(\\s)\\\\#", "$1#").trim();
    }
}
