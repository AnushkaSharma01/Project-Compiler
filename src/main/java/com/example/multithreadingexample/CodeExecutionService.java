package com.example.multithreadingexample;

import org.springframework.stereotype.Service;

import java.io.*;
import java.util.concurrent.*;

@Service
public class CodeExecutionService {

    private ExecutorService executorService = Executors.newFixedThreadPool(3);
    private BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private String currentJavaCode = "";

    public String executeJavaCode(String javaCode) {
        currentJavaCode = javaCode;
        return execute(javaCode);
    }

    public String provideUserInput(String userInput) {
        return execute(currentJavaCode, userInput);
    }

    private String execute(String javaCode) {
        return execute(javaCode, null);
    }

    private String execute(String javaCode, String userInput) {
        String executionResult = null;

        try {
            queue.put(javaCode);
            Future<String> future = executorService.submit(new CodeExecutionThread(queue, userInput));
            executionResult = future.get(); // Get the execution result from the thread
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            executionResult = "Error executing code: " + e.getMessage();
        }

        return executionResult;
    }

    private static class CodeExecutionThread implements Callable<String> {
        private final BlockingQueue<String> queue;
        private final String userInput;

        public CodeExecutionThread(BlockingQueue<String> queue, String userInput) {
            this.queue = queue;
            this.userInput = userInput;
        }

        @Override
        public String call() {
            String output = "";
            try {
                String javaCode = queue.take();
                if (javaCode == null || javaCode.isEmpty()) {
                    return "No Java code provided.";
                }

                // Extract the class name from the Java code
                String className = extractClassName(javaCode);

                // Create a temporary file for Java code
                File tempFile = new File(System.getProperty("java.io.tmpdir"), className + ".java");
                try (FileWriter writer = new FileWriter(tempFile)) {
                    writer.write(javaCode);
                }

                // Compile the Java code
                ProcessBuilder compileProcessBuilder = new ProcessBuilder("javac", tempFile.getAbsolutePath());
                Process compileProcess = compileProcessBuilder.start();
                int compileExitCode = compileProcess.waitFor();

                if (compileExitCode == 0) {
                    // Execute the compiled Java code
                    ProcessBuilder runProcessBuilder = new ProcessBuilder("java", "-cp", tempFile.getParent(), className);
                    Process runProcess = runProcessBuilder.start();

                    if (userInput != null) {
                        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(runProcess.getOutputStream()))) {
                            writer.write(userInput);
                            writer.newLine();
                            writer.flush();
                        }
                    }

                    StringBuilder runOutput = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            runOutput.append(line).append("\n");
                        }
                    }

                    int runExitCode = runProcess.waitFor();
                    if (runExitCode == 0) {
                        output = "Output:\n" + runOutput.toString();
                    } else {
                        String errorOutput = readStream(runProcess.getErrorStream());
                        output = "Execution failed:\n" + errorOutput;
                    }
                } else {
                    String compileErrorOutput = readStream(compileProcess.getErrorStream());
                    output = "Compilation failed:\n" + compileErrorOutput;
                }

            } catch (IOException | InterruptedException e) {
                Thread.currentThread().interrupt();
                output = "Error executing code: " + e.getMessage();
            }

            return output;
        }

        private String readStream(InputStream stream) throws IOException {
            StringBuilder result = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }
            }
            return result.toString();
        }

        private String extractClassName(String javaCode) {
            // Basic extraction of the class name assuming "public class <classname>"
            int classIndex = javaCode.indexOf("public class");
            if (classIndex == -1) {
                return "Main";
            }
            int startIndex = classIndex + "public class".length();
            int endIndex = javaCode.indexOf("{", startIndex);
            return javaCode.substring(startIndex, endIndex).trim().split("\\s+")[0];
        }
    }
}




//package com.example.multithreadingexample;//package com.example.multithreadingexample;
//
//import org.springframework.stereotype.Service;
//import java.io.*;
//import java.util.concurrent.*;
//import java.util.Scanner; // Added import for Scanner
//
//@Service
//public class CodeExecutionService {
//
//    public String executeJavaCode(String javaCode) {
//        ExecutorService executorService = Executors.newFixedThreadPool(3);
//        BlockingQueue<String> queue = new LinkedBlockingQueue<>();
//        String executionResult = null;
//
//        try {
//            queue.put(javaCode);
//            Future<String> future = executorService.submit(new CodeExecutionThread(queue));
//            executorService.shutdown();
//            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
//            executionResult = future.get(); // Get the execution result from the thread
//        } catch (InterruptedException | ExecutionException e) {
//            Thread.currentThread().interrupt();
//            executionResult = "Error executing code: " + e.getMessage();
//        }
//
//        return executionResult;
//    }
//
//    private static class CodeExecutionThread implements Callable<String> {
//        private final BlockingQueue<String> queue;
//
//        public CodeExecutionThread(BlockingQueue<String> queue) {
//            this.queue = queue;
//        }
//
//        @Override
//        public String call() {
//            String output = "";
//            try {
//                String javaCode = queue.take();
//                if (javaCode == null || javaCode.isEmpty()) {
//                    return "No Java code provided.";
//                }
//
//                // Insert imports for Scanner into the Java code
//                javaCode = "import java.util.Scanner;\n" + javaCode;
//
//                // Create a temporary file for Java code
//                File tempFile = File.createTempFile("Main", ".java");
//                try (FileWriter writer = new FileWriter(tempFile)) {
//                    writer.write(javaCode);
//                }
//
//                // Compile the Java code
//                ProcessBuilder compileProcessBuilder = new ProcessBuilder("javac", tempFile.getAbsolutePath());
//                Process compileProcess = compileProcessBuilder.start();
//                int compileExitCode = compileProcess.waitFor();
//
//                if (compileExitCode == 0) {
//                    // Execute the compiled Java code
//                    ProcessBuilder runProcessBuilder = new ProcessBuilder("java", "-cp", tempFile.getParent(), "Main");
//                    Process runProcess = runProcessBuilder.start();
//
//                    StringBuilder runOutput = new StringBuilder();
//                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()))) {
//                        String line;
//                        while ((line = reader.readLine()) != null) {
//                            runOutput.append(line).append("\n");
//                        }
//                    }
//
//                    int runExitCode = runProcess.waitFor();
//                    if (runExitCode == 0) {
//                        output = "Output:\n" + runOutput.toString();
//                    } else {
//                        String errorOutput = readStream(runProcess.getErrorStream());
//                        output = "Execution failed:\n" + errorOutput;
//                    }
//                } else {
//                    String compileErrorOutput = readStream(compileProcess.getErrorStream());
//                    output = "Compilation failed:\n" + compileErrorOutput;
//                }
//
//            } catch (IOException | InterruptedException e) {
//                Thread.currentThread().interrupt();
//                output = "Error executing code: " + e.getMessage();
//            }
//
//            return output;
//        }
//
//        private String readStream(InputStream stream) throws IOException {
//            StringBuilder result = new StringBuilder();
//            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    result.append(line).append("\n");
//                }
//            }
//            return result.toString();
//        }
//    }
//}
//
