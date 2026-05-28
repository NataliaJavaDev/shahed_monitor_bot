// package com.tgbot.shahedmonitorbot;

// import com.tgbot.shahedmonitorbot.processing.AlertProcessingService;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.stereotype.Component;

// @Component
// public class TestRunner implements CommandLineRunner {

//     private final AlertProcessingService alertProcessingService;

//     public TestRunner(AlertProcessingService alertProcessingService) {
//         this.alertProcessingService = alertProcessingService;
//     }

//     @Override
//     public void run(String... args) {
//         System.out.println("TEST RUNNER STARTED");

//         alertProcessingService.process(
//                 "TEST",
//                 "Шахед курсом на Білу Церкву"
//         );

//         System.out.println("TEST RUNNER FINISHED");
//     }
// }
// package com.tgbot.shahedmonitorbot;

// import com.tgbot.shahedmonitorbot.admin.AdminCommandHandler;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.stereotype.Component;

// @Component
// public class TestRunner implements CommandLineRunner {

//     private final AdminCommandHandler adminCommandHandler;

//     public TestRunner(AdminCommandHandler adminCommandHandler) {
//         this.adminCommandHandler = adminCommandHandler;
//     }

//     @Override
//     public void run(String... args) {

//         adminCommandHandler.handle(1L, "/admin");
// adminCommandHandler.handle(1L, "/keywords");
// adminCommandHandler.handle(1L, "/add_keyword");
// adminCommandHandler.handle(1L, "сквира");
// adminCommandHandler.handle(1L, "/keywords");
// adminCommandHandler.handle(1L, "/remove_keyword");
// adminCommandHandler.handle(1L, "сквира");
// adminCommandHandler.handle(1L, "/keywords");
//     }
// }