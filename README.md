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
