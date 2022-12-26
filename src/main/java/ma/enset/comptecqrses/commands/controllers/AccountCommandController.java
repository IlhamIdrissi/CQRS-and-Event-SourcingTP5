package ma.enset.comptecqrses.commands.controllers;

import lombok.AllArgsConstructor;
import ma.enset.comptecqrses.commonapi.commands.CreateAccountCommand;
import ma.enset.comptecqrses.commonapi.commands.CreditAccountCommand;
import ma.enset.comptecqrses.commonapi.commands.DebitAccountCommand;
import ma.enset.comptecqrses.commonapi.dtos.CreateAccountRequestDTO;
import ma.enset.comptecqrses.commonapi.dtos.CreditAccountRequestDTO;
import ma.enset.comptecqrses.commonapi.dtos.DebitAccountRequestDTO;
import ma.enset.comptecqrses.commonapi.enums.AccountStatus;
import ma.enset.comptecqrses.commonapi.events.AccountActivatedEvent;
import ma.enset.comptecqrses.commonapi.events.AccountCreatedEvent;
import ma.enset.comptecqrses.commonapi.events.AccountCreditedEvent;
import ma.enset.comptecqrses.commonapi.events.AccountDebitedEvent;
import ma.enset.comptecqrses.commonapi.exceptions.InsufficientBalanceToDebitException;
import ma.enset.comptecqrses.commonapi.exceptions.InsufficientCreditAmount;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;


@RestController
@RequestMapping(path = "/commands/account")
@AllArgsConstructor
public class AccountCommandController {
        private CommandGateway commandGateway;
        private EventStore eventStore;
        @PostMapping("/create")
        public CompletableFuture<String> createAccount
                (@RequestBody CreateAccountRequestDTO request){
                CompletableFuture<String> commandResponse =
                        commandGateway.send(new CreateAccountCommand(
                        UUID.randomUUID().toString(),
                        request.getInitialBalance(),
                        request.getCurrency()
                ));

                return commandResponse;
        }
        @ExceptionHandler(Exception.class)
        public ResponseEntity<String> exceptionHandler
                (Exception exception){
                ResponseEntity<String> responseEntity = new ResponseEntity<>(
                        exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR
                );
                return responseEntity;
        }
        @GetMapping("/eventStore/{accountId}")
        public Stream eventStore(@PathVariable String accountId){
                return eventStore.readEvents(accountId).asStream();
        }

        @PutMapping("/credit")
        public CompletableFuture<String> creditAccount
                (@RequestBody CreditAccountRequestDTO creditAccountRequestDTO){
                CompletableFuture<String> creditAccountCommandResponse =
                        commandGateway.send(new CreditAccountCommand(
                        creditAccountRequestDTO.getAccountId(),
                        creditAccountRequestDTO.getAmount(),
                        creditAccountRequestDTO.getCurrency()
                ));
                return creditAccountCommandResponse;
        }
        @PutMapping("/debit")
        public CompletableFuture<String> debitAccount
                (@RequestBody DebitAccountRequestDTO debitAccountRequestDTO){
                CompletableFuture<String> debitAccountCommandResponse =
                        commandGateway.send(new DebitAccountCommand(
                        debitAccountRequestDTO.getAccountId(),
                        debitAccountRequestDTO.getAmount(),
                        debitAccountRequestDTO.getCurrency()
                ));

                return debitAccountCommandResponse;
        }
}