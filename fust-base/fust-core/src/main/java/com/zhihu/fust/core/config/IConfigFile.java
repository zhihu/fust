package com.zhihu.fust.core.config;

/**
 * file config
 */
public interface IConfigFile {
    /**
     * Get file content of the namespace
     * @return file content, {@code null} if there is no content
     */
    String getContent();

    /**
     * Whether the config file has any content
     * @return true if it has content, false otherwise.
     */
    boolean hasContent();

    /**
     * Get the namespace of this config file instance
     * @return the namespace
     */
    String getNamespace();

    /**
     * Add change listener to this config file instance.
     *
     * @param listener the config file change listener
     */
    void addChangeListener(IConfigFileChangeListener listener);

    /**
     * Remove the change listener
     *
     * @param listener the specific config change listener to remove
     * @return true if the specific config change listener is found and removed
     */
    boolean removeChangeListener(IConfigFileChangeListener listener);
}
