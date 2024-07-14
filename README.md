# Modifying Request Headers with MITMProxy 🛠️

This project demonstrates how to modify request headers using MITMProxy in a backend setup. The project is set up using Java, and includes custom utilities for file modification and port allocation to facilitate testing.

## Cloning the Repository 📂

To get started, clone the repository using the following command:

```bash
git clone https://github.com/ritamganguli/java_mock_header_updated2.git
cd java_mock_header_updated2
```


##Project Overview 📋


This project utilizes MITMProxy to mock request headers. The key components include:

PythonFileModifier.java: This utility modifies lines in a Python file to include mock data for testing.
PortAllocator.java: This utility allocates random ports for each session to avoid conflicts.
TestNGTodo2.java: This is a sample test class that sets up the testing environment, starts the MITMProxy server, and defines test cases.


mock_proxy.py

This is the code script for mock proxy server which modifies the request headears

```
from mitmproxy import http
import json

# Define the URLs and headers to modify
def response(flow: http.HTTPFlow) -> None:
    api_url = flow.request.pretty_url
    if api_url in urls_to_mock:
        # Modify the specified headers
        for header, value in urls_to_mock[api_url].items():
            flow.response.headers[header] = value

        # Save headers to a file
        headers = {k: v for k, v in flow.response.headers.items()}
        with open("modified_headers.json", "w") as file:
            json.dump(headers, file, indent=2)
        print(f"Modified headers for {api_url}: {headers}")

```


PythonFileModifier.java
This class is responsible for modifying specific lines in a Python file to include mock data. Save this file in the directory where your test cases are written.
Please ensure that you place this class over the test class directory

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

PortAllocator.java
This class allocates random ports for each session, ensuring no two sessions use the same port. Save this file at the class level of the test cases.

```
package com.lambdatest;

import java.util.Random;

public class PortAllocator {
    private static int currentPort = generateRandomPort();
    private static final Object lock = new Object();
    private static final int EXCLUDED_PORT = 8080;
    private static final int MIN_PORT = 1000;
    private static final int MAX_PORT = 9999;

    private static int generateRandomPort() {
        Random random = new Random();
        int port;
        do {
            port = random.nextInt((MAX_PORT - MIN_PORT) + 1) + MIN_PORT;
        } while (port == EXCLUDED_PORT);
        return port;
    }

    public static int getNextPort() {
        synchronized (lock) {
            return currentPort++;
        }
    }
}
```

TestNGTodo2.java
This is an example of a test class using the Maven Tunnel to start up the tunnel and run tests.


```

package com.lambdatest;

import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.lambdatest.tunnel.Tunnel;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestNGTodo2 {
    private Tunnel t;
    private RemoteWebDriver driver;
    private String Status = "failed";
    private Process mitmproxyProcess;
    private String newFilePath;

    @BeforeMethod
    public void setup(Method m, ITestContext ctx) throws Exception {
        String username = "ritamg";
        String authkey = "acees_key";
        String hub = "@hub.lambdatest.com/wd/hub";

        int port = PortAllocator.getNextPort();

        t = new Tunnel();
        HashMap<String, String> options = new HashMap<>();
        options.put("user", username);
        options.put("key", authkey);
        options.put("mitm", "true");
        options.put("proxyHost", "localhost");
        options.put("proxyPort", String.valueOf(port));
        options.put("ingress-only", "true");
        options.put("tunnelName",m.getName() + this.getClass().getName());
        t.start(options);

        // Start the LambdaTest Tunnel
        String originalFilePath = "ritam.py";
        newFilePath = m.getName() + "_" + this.getClass().getName() + ".py";
        Files.copy(Paths.get(originalFilePath), Paths.get(newFilePath), StandardCopyOption.REPLACE_EXISTING);

        // Start mitmdump with the copied script
        ProcessBuilder processBuilder = new ProcessBuilder("./start_mitmproxy.sh", newFilePath, String.valueOf(port));
        mitmproxyProcess = processBuilder.start();
        System.out.println("Proxy server started on port " + port);

        // Capture and handle process output streams to prevent blocking
        Executors.newSingleThreadExecutor().submit(() -> {
            try (InputStream is = mitmproxyProcess.getInputStream()) {
                byte[] buffer = new byte[1024];
                while (is.read(buffer) != -1) {
                    // Do nothing, just consume the stream to prevent blocking
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Executors.newSingleThreadExecutor().submit(() -> {
            try (InputStream is = mitmproxyProcess.getErrorStream()) {
                byte[] buffer = new byte[1024];
                while (is.read(buffer) != -1) {
                    // Do nothing, just consume the stream to prevent blocking
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Configure DesiredCapabilities
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("platform", "Windows 10");
        caps.setCapability("browserName", "chrome");
        caps.setCapability("version", "latest");
        caps.setCapability("build", "TestNG With Java");
        caps.setCapability("name", m.getName() + this.getClass().getName());
        caps.setCapability("plugin", "git-testng");
        caps.setCapability("tunnel", true);
        caps.setCapability("tunnelName",m.getName() + this.getClass().getName());
        caps.setCapability("network", true);

        String[] Tags = new String[]{"Feature", "Magicleap", "Severe"};
        caps.setCapability("tags", Tags);

        driver = new RemoteWebDriver(new URL("https://" + username + ":" + authkey + hub), caps);
    }

    @Test
    public void basicTest() throws InterruptedException {
        System.out.println("Loading Url");

        Map<String, Map<String, String>> mockData = new HashMap<>();

        // Define the mock data for a single API
        Map<String, String> api1MockData = new HashMap<>();
        api1MockData.put("Server", "ritam2.com");
        mockData.put("https://www.lambdatest.com/resources/js/zohocrm.js", api1MockData);

        Map<String, String> api2MockData = new HashMap<>();
        api2MockData.put("Referrer-Policy", "value2");
        mockData.put("https://www.lambdatest.com/resources/js/zohoscript.js",api2MockData);

        // Modify the Python file with the mock data
        PythonFileModifier.modifyLineInFile(newFilePath,mockData);

        Thread.sleep(10000);
        driver.get("https://lambdatest.com/");
        Thread.sleep(15000);

        Status = "passed";
        System.out.println("Test Finished");
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.executeScript("lambda-status=" + Status);
            driver.quit();
        }

        // Stop the LambdaTest Tunnel
        if (t != null) {
            try {
                t.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Stop mitmproxy process
        if (mitmproxyProcess != null) {
            mitmproxyProcess.destroy();
            try {
                if (!mitmproxyProcess.waitFor(50, TimeUnit.SECONDS)) {
                    mitmproxyProcess.destroyForcibly();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
```

