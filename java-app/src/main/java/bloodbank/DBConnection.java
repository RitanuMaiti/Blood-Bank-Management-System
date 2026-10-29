package bloodbank;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.function.Supplier;

public class DBConnection {

    private static final String DEFAULT_URL = "jdbc:oracle:thin:@localhost:1521:XE";
    private static final String DEFAULT_USERNAME = "bloodbank";
    private static final String DEFAULT_PASSWORD = "bloodbank";

    // Test connection supplier hook (allows isolated in-memory testing without Oracle)
    private static Supplier<Connection> customConnectionSupplier = null;

    private static String url;
    private static String username;
    private static String password;

    static {
        loadConfiguration();
    }

    public static synchronized void loadConfiguration() {
        Properties props = new Properties();

        // 1. Try loading from working directory db.properties
        File file = new File("db.properties");
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
            } catch (Exception ignored) {
            }
        } else {
            // 2. Try loading from classpath
            try (InputStream is = DBConnection.class.getResourceAsStream("/db.properties")) {
                if (is != null) {
                    props.load(is);
                }
            } catch (Exception ignored) {
            }
        }

        // Precedence: System Property > Environment Variable > Properties File > Default Fallback
        url = getSetting("db.url", "DB_URL", props.getProperty("db.url", DEFAULT_URL));
        username = getSetting("db.username", "DB_USERNAME", props.getProperty("db.username", DEFAULT_USERNAME));
        if (username.equals(DEFAULT_USERNAME)) {
            // Also check DB_USER env var
            String envUser = System.getenv("DB_USER");
            if (envUser != null && !envUser.isBlank()) {
                username = envUser;
            }
        }
        password = getSetting("db.password", "DB_PASSWORD", props.getProperty("db.password", DEFAULT_PASSWORD));
    }

    private static String getSetting(String sysProp, String envVar, String defaultValue) {
        String sys = System.getProperty(sysProp);
        if (sys != null && !sys.isBlank()) {
            return sys.trim();
        }
        String env = System.getenv(envVar);
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        return defaultValue;
    }

    public static Connection getConnection() throws SQLException {
        if (customConnectionSupplier != null) {
            return customConnectionSupplier.get();
        }

        return DriverManager.getConnection(url, username, password);
    }

    /**
     * For unit and integration testing: allows tests to supply an in-memory test database connection.
     */
    public static synchronized void setCustomConnectionSupplier(Supplier<Connection> supplier) {
        customConnectionSupplier = supplier;
    }

    public static synchronized void resetCustomConnectionSupplier() {
        customConnectionSupplier = null;
    }

    public static String getSanitizedUrl() {
        return url;
    }

    public static String getUsername() {
        return username;
    }
}