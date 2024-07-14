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


public class TestNGTodo3 {
    private Tunnel t;
    private RemoteWebDriver driver;
    private String Status = "failed";
    private Process mitmproxyProcess;
    private String newFilePath;

    @BeforeMethod
    public void setup(Method m, ITestContext ctx) throws Exception {

        String username = "shubhamr";
        String authkey = "dl8Y8as59i1YyGZZUeLF897aCFvIDmaKkUU1e6RgBmlgMLIIhh";
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

        // Get a unique port number


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
        api1MockData.put("Server", "ritam3.com");
        mockData.put("https://www.lambdatest.com/resources/js/zohocrm.js", api1MockData);


        Map<String, String> api2MockData = new HashMap<>();
        api2MockData.put("Referrer-Policy", "value3");
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

        // Clean up the copied Python file
        //Files.deleteIfExists(Paths.get(newFilePath));
//        try {
//            Files.deleteIfExists(Paths.get(newFilePath));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
