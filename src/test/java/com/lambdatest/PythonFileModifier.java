package com.lambdatest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class PythonFileModifier {

    private static final ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    public static void modifyLineInFile(String filePath, String apiUrl, Map<String, String> headersToMock) {
        int lineNumberApiUrl = 5; // Line number for API URL
        String newContentApiUrl = "api_url = '" + apiUrl + "'";

        // Get or create a lock for the specific file path
        ReentrantLock lock = lockMap.computeIfAbsent(filePath, k -> new ReentrantLock());

        lock.lock();
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            // Replace the specific line for API URL
            lines.set(lineNumberApiUrl - 1, newContentApiUrl);

            // Update headers by inserting them after a specific line, assuming line 7 for this example
            int headerStartLine = 7;
            for (Map.Entry<String, String> entry : headersToMock.entrySet()) {
                String newContentHeader = "'" + entry.getKey() + "': '" + entry.getValue() + "',";
                lines.add(headerStartLine++, newContentHeader);
            }

            // Write back to the file
            Files.write(Paths.get(filePath), lines);
            System.out.println("Lines set successfully for file: " + filePath);
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        } finally {
            lock.unlock();
            // Optional: Clean up the lock map if needed
            lockMap.remove(filePath);
        }
    }
}
