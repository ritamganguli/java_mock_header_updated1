# Modifying Request Headers with MITMProxy 🛠️

This project demonstrates how to modify request headers using MITMProxy in a backend setup. The project is set up using Java, and includes custom utilities for file modification and port allocation to facilitate testing.

## Cloning the Repository 📂

To get started, clone the repository using the following command:

```bash
git clone https://github.com/ritamganguli/java_mock_header_updated2.git
cd java_mock_header_updated2


Project Overview 📋
This project utilizes MITMProxy to mock request headers. The key components include:

PythonFileModifier.java: This utility modifies lines in a Python file to include mock data for testing.
PortAllocator.java: This utility allocates random ports for each session to avoid conflicts.
TestNGTodo2.java: This is a sample test class that sets up the testing environment, starts the MITMProxy server, and defines test cases.


PythonFileModifier.java
This class is responsible for modifying specific lines in a Python file to include mock data. Save this file in the directory where your test cases are written.

```
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

    public static void modifyLineInFile(String filePath, Map<String, Map<String, String>> mockData) {
        int lineNumberMockData = 4; // Line number for mock data
        String newContentMockData = "urls_to_mock = " + mapToPythonDict(mockData);

        // Get or create a lock for the specific file path
        ReentrantLock lock = lockMap.computeIfAbsent(filePath, k -> new ReentrantLock());

        lock.lock();
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            // Replace the specific line for mock data
            lines.set(lineNumberMockData - 1, newContentMockData);
            // Write back to the file
            Files.write(Paths.get(filePath), lines);
            System.out.println("Lines set successfully for file: " + filePath);
            System.out.println("Mock data content: " + newContentMockData);
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        } finally {
            lock.unlock();
            // Optional: Clean up the lock map if needed
            lockMap.remove(filePath);
        }
    }

    private static String mapToPythonDict(Map<String, Map<String, String>> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Map.Entry<String, Map<String, String>> entry : map.entrySet()) {
            sb.append("'").append(entry.getKey()).append("': {");
            for (Map.Entry<String, String> subEntry : entry.getValue().entrySet()) {
                sb.append("'").append(subEntry.getKey()).append("': '").append(subEntry.getValue()).append("', ");
            }
            sb.delete(sb.length() - 2, sb.length()); // Remove trailing comma and space
            sb.append("}, ");
        }
        sb.delete(sb.length() - 2, sb.length()); // Remove trailing comma and space
        sb.append("}");
        return sb.toString();
    }
}
```

