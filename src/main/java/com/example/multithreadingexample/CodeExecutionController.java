//package com.example.multithreadingexample;//package com.example.multithreadingexample;
//
//import org.springframework.beans.factory.annotation.Autowired;
//
//import com.example.multithreadingexample.CodeExecutionService;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api")
//public class CodeExecutionController {
//
//    private final CodeExecutionService codeExecutionService;
//
//    @Autowired
//    public CodeExecutionController(CodeExecutionService codeExecutionService) {
//        this.codeExecutionService = codeExecutionService;
//    }
//
//    @PostMapping("/execute")
//    public String executeCode(@RequestBody String javaCode) {
//        return codeExecutionService.executeJavaCode(javaCode);
//    }
//}

package com.example.multithreadingexample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/code-execution")
public class CodeExecutionController {

    @Autowired
    private CodeExecutionService codeExecutionService;

    @PostMapping("/execute")
    public String executeJavaCode(@RequestBody String javaCode) {
        return codeExecutionService.executeJavaCode(javaCode);
    }

    @PostMapping("/user-input")
    public String provideUserInput(@RequestBody String userInput) {
        return codeExecutionService.provideUserInput(userInput);
    }
}
