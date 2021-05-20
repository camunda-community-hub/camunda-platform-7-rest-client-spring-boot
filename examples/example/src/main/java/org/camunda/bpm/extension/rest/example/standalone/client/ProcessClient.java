package org.camunda.bpm.extension.rest.example.standalone.client;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.MessageCorrelationResultWithVariables;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.extension.rest.variables.PrettyPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.camunda.bpm.engine.variable.Variables.*;

/**
 * Client speaking with a remote process engine.
 */
@Component
public class ProcessClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessClient.class);

  private final RuntimeService runtimeService;
  private final RepositoryService repositoryService;

  /**
   * Constructs the client.
   *
   * @param runtimeService
   * @param repositoryService
   */
  public ProcessClient(
    @Qualifier("remote") RuntimeService runtimeService,
    @Qualifier("remote") RepositoryService repositoryService
  ) {
    this.runtimeService = runtimeService;
    this.repositoryService = repositoryService;
  }

  /**
   * Retrieves process definitions periodically.
   */
  @Scheduled(initialDelay = 8_000, fixedRate = Integer.MAX_VALUE)
  public void retrieveProcessDefinition() {
    LOGGER.info("CLIENT-90: Retrieving process definition");
    long count = repositoryService.createProcessDefinitionQuery().count();
    LOGGER.info("CLIENT-91: Found {} deployed processes", count);
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    LOGGER.info("CLIENT-92: Deployed process definition is {}", PrettyPrinter.toPrettyString(processDefinition));
  }

  /**
   * Starts processes periodically.
   */
  @Scheduled(initialDelay = 10_000, fixedDelay = 5_000)
  public void startProcess() {
    LOGGER.trace("CLIENT-100: Starting a process instance remote");
    VariableMap variables = createVariables()
      .putValueTyped("ID", stringValue("MESSAGING-" + UUID.randomUUID()));
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("process_messaging", "WAIT_FOR_MESSAGE", variables);
    LOGGER.trace("CLIENT-101: Started instance {} - {}", instance.getId(), instance.getBusinessKey());
  }

  /**
   * Fires signals periodically.
   */
  @Scheduled(initialDelay = 12_500, fixedDelay = 5_000)
  public void fireSignal() {

    LOGGER.info("CLIENT-200: Sending signal");
    VariableMap variables = createVariables()
      .putValueTyped("BYTES", byteArrayValue("World".getBytes()));
    runtimeService
      .createSignalEvent("signal_received")
      .setVariables(variables)
      .send();
  }

  /**
   * Correlates messages periodically.
   */
  @Scheduled(initialDelay = 13_500, fixedDelay = 5_000)
  public void correlateMessage() {

    LOGGER.info("CLIENT-300: Correlating message");

    VariableMap variables = createVariables();
    variables.putValueTyped("STRING", stringValue("my string"));
    variables.putValueTyped("CORRELATION_DATE", dateValue(Date.from(Instant.now())));
    variables.putValueTyped("SHORT", shortValue((short) 120));
    variables.putValueTyped("DOUBLE", doubleValue(1.0));
    variables.putValueTyped("INTEGER", integerValue(65800));
    variables.putValueTyped("LONG", longValue(1L + Integer.MAX_VALUE));
    variables.putValueTyped("BYTES", byteArrayValue("Hello!".getBytes()));
    variables.putValueTyped("OBJECT", objectValue(new MyDataStructure("string", 100)).create());

    List<MessageCorrelationResultWithVariables> result = runtimeService
      .createMessageCorrelation("message_received")
      .processInstanceBusinessKey("WAIT_FOR_MESSAGE")
      .setVariables(variables)
      .correlateAllWithResultAndVariables(true);

    result.forEach(element -> LOGGER.info("CLIENT-301: {}", PrettyPrinter.toPrettyString(element)));
  }

}
