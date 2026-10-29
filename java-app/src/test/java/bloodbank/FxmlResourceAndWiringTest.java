package bloodbank;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class FxmlResourceAndWiringTest {

    private static final String[] FXML_FILES = {
            "/bloodbank/dashboard.fxml",
            "/bloodbank/donors.fxml",
            "/bloodbank/donations.fxml",
            "/bloodbank/hospitals.fxml",
            "/bloodbank/inventory.fxml",
            "/bloodbank/requests.fxml"
    };

    @Test
    @DisplayName("Verify that all FXML files exist on the classpath")
    void testFxmlResourcesExist() {
        for (String file : FXML_FILES) {
            InputStream is = getClass().getResourceAsStream(file);
            assertNotNull(is, "FXML resource not found on classpath: " + file);
        }
    }

    @Test
    @DisplayName("Verify that FXML controller classes and event handler methods exist")
    void testControllerAndHandlerWiring() throws Exception {
        Pattern controllerPattern = Pattern.compile("fx:controller=\"([^\"]+)\"");
        Pattern onActionPattern = Pattern.compile("onAction=\"#([^\"]+)\"");

        for (String file : FXML_FILES) {
            try (InputStream is = getClass().getResourceAsStream(file)) {
                assertNotNull(is);
                String content = new Scanner(is).useDelimiter("\\A").next();

                // Find controller
                Matcher cMatcher = controllerPattern.matcher(content);
                assertTrue(cMatcher.find(), "No fx:controller found in " + file);
                String controllerClassName = cMatcher.group(1);

                Class<?> controllerClass = Class.forName(controllerClassName);
                assertNotNull(controllerClass, "Could not load controller class: " + controllerClassName);

                // Find all onAction methods
                Matcher aMatcher = onActionPattern.matcher(content);
                while (aMatcher.find()) {
                    String methodName = aMatcher.group(1);
                    boolean found = false;

                    for (Method m : controllerClass.getDeclaredMethods()) {
                        if (m.getName().equals(methodName)) {
                            // Method should take either 0 args or 1 ActionEvent arg
                            if (m.getParameterCount() == 0 ||
                                    (m.getParameterCount() == 1 && m.getParameterTypes()[0].getSimpleName().equals("ActionEvent"))) {
                                found = true;
                                break;
                            }
                        }
                    }

                    assertTrue(found, "Handler method '" + methodName + "' referenced in "
                            + file + " does not exist with valid signature on " + controllerClassName);
                }
            }
        }
    }
}
