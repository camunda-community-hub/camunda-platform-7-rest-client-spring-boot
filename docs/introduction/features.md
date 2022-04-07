The library supports the following features:

## General

* Usage of Open Feign library to allow for high-customizable REST client (generated from camunda openapi definition) 
* Provides a SpringBoot starter for usage in standalone client mode
* Provides a SpringBoot starter for usage inside a process application
* Decode HTTP error response and simulate exceptions, as if they are thrown locally by the service.
* Implemented Services: `RuntimeService`, `RepositoryService`, `TaskService`, `ExternalTaskService`, `HistoryService`

## RuntimeService

* Process start by key: `#startProcessInstanceByKey()`
* Process start by id: `#startProcessInstanceById()`
* Process instance query: `#createProcessInstanceQuery()`
* Message correlation: `#correlateMessage()`, `#createMessageCorrelation()`
* Signal event: `#signalEventReceived()`, `#createSignalEvent()`
* Execution trigger: `#signal()`
* Read variables: `#getVariable()`,`#getVariables()`, `#getVariableTyped()`, `#getVariablesTyped()`
* Read local variables: `#getVariableLocal()`,`#getVariablesLocal()`, `#getVariableLocalTyped()`, `#getVariablesLocalTyped()`
* Write variables: `#setVariable()`,`#setVariables()`, `#setVariableTyped()`, `#setVariablesTyped()`
* Delete variables: `#removeVariable()`,`#removeVariables()`
* Write local variables: `#setVariableLocal()`,`#setVariablesLocal()`, `#setVariableLocalTyped()`, `#setVariablesLocalTyped()`
* Delete local variables: `#removeVariableLocal()`,`#removeVariablesLocal()`
* Incident query: `#createIncidentQuery()`
* Create/Resolve incidents: `#createIncident()`, `#resolveIncident()`
* Annotations for incidents: `#setAnnotationForIncidentById()`, `#clearAnnotationForIncidentById()`
* Activate/Suspend process instances by definition key: `#suspendProcessInstanceByProcessDefinitionKey()`, `#activateProcessInstanceByProcessDefinitionKey()`
* Activate/Suspend process instances by definition id: `#suspendProcessInstanceByProcessDefinitionId()`, `#activateProcessInstanceByProcessDefinitionId()`
* Activate/Suspend process instances: `#suspendProcessInstanceById()`, `#activateProcessInstanceById()`
* Update process instance suspension state: `#updateProcessInstanceSuspensionState()`
* Delete process instances: `#deleteProcessInstance()`, `#deleteProcessInstanceIfExists()`, `#deleteProcessInstances()`, `#deleteProcessInstancesIfExists()`, `#deleteProcessInstancesAsync()`

## RepositoryService

* Query for process definitions: `#createProcessDefinitionQuery()`
* History time to live: `#updateDecisionDefinitionHistoryTimeToLive()`, `#updateProcessDefinitionHistoryTimeToLive()`
* Create deployment: `#createDeployment()`
* Delete deployment: `#deleteDeployment()`, `#deleteDeploymentCascade()`
* Deployment query: `#createDeploymentQuery()`
* Delete process definition: `#deleteProcessDefinition()`
* Update process definition suspension state: `#updateProcessDefinitionSuspensionState()`

## TaskService 

* Query for tasks: `#createTaskQuery()`
* Task assignment: `#claim()`, `#defer()`, `#resolve()`, `#addCandidateGroup()`, `#deleteCandidateGroup()`, `#addCadidateUser()`, `#deleteCandidateUser()`
* Identity links: `#addUserIdentityLink()`, `#addGroupIdentityLink()`, `#deleteUserIdentityLink()`, `#deleteGroupIdentityLink()`,
* Task completion: `#complete()`
* Task deletion: `#deleteTasks()`
* Task attributes: `#setPriority()`, `#setOwner()`, `#setAssignee()`, `#saveTask()`
* Handling Errors and Escalation: `#handleBpmnError()`, `#handleEscalation()`
* Read variables: `#getVariable()`,`#getVariables()`, `#getVariableTyped()`, `#getVariablesTyped()`
* Read local variables: `#getVariableLocal()`,`#getVariablesLocal()`, `#getVariableLocalTyped()`, `#getVariablesLocalTyped()`
* Write variables: `#setVariable()`,`#setVariables()`, `#setVariableTyped()`, `#setVariablesTyped()`
* Delete variables: `#removeVariable()`,`#removeVariables()`
* Write local variables: `#setVariableLocal()`,`#setVariablesLocal()`, `#setVariableLocalTyped()`, `#setVariablesLocalTyped()`
* Delete local variables: `#removeVariableLocal()`,`#removeVariablesLocal()`

## ExternalTaskService

We are not aiming to replace the existing [External Task Client](https://docs.camunda.org/manual/latest/user-guide/ext-client/),
but still provide an alternative implementation for some methods.

* Complete a task by id: `#complete()`
* Handle BPMN Errors: `#handleBpmnError()`
* Handle failures: `#handleFailure()`

## HistoryService

* Historic process instance query: `#createHistoricProcessInstanceQuery()`
