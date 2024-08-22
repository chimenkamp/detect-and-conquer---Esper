package ubt.process_analytics.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PROBS {

    private static volatile PROBS instance;
    private Properties properties;

    /**
     * Singleton class
     */
    private PROBS() {
        properties = new Properties();

        String CONFIG_FILE_NAME = STR."\{System.getProperty("user.dir")}/config.properties";
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE_NAME)) {
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Public method to provide access to the single instance of PROBS.
     *
     * @return The singleton instance of PROBS.
     */
    public static PROBS getInstance() {
        if (instance == null) {
            synchronized (PROBS.class) {
                if (instance == null) {
                    instance = new PROBS();
                }
            }
        }
        return instance;
    }

    /**
     * Retrieves a property value as a String.
     *
     * @param key The key of the property.
     * @return The value associated with the key.
     */
    public String getString(String key) {
        return properties.getProperty(key);
    }

    /**
     * Retrieves a property value as an Integer.
     *
     * @param key The key of the property.
     * @return The Integer value associated with the key.
     * @throws NumberFormatException if the value is not a valid integer.
     */
    public Integer getInt(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }
}
