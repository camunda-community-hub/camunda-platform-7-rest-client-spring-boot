<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="https://www.omg.org/spec/DMN/20191111/MODEL/" xmlns:dmndi="https://www.omg.org/spec/DMN/20191111/DMNDI/" xmlns:dc="http://www.omg.org/spec/DMN/20180521/DC/" xmlns:modeler="http://camunda.org/schema/modeler/1.0" xmlns:camunda="http://camunda.org/schema/1.0/dmn" id="test" name="Test" namespace="http://camunda.org/schema/1.0/dmn" exporter="Camunda Modeler" exporterVersion="5.14.0" modeler:executionPlatform="Camunda Platform" modeler:executionPlatformVersion="7.19.0">
  <decision id="decision_table" name="Decision Table" camunda:versionTag="1">
    <decisionTable id="DecisionTable_0b8smbf">
      <input id="Input_1" label="Input 1" camunda:inputVariable="input1">
        <inputExpression id="InputExpression_1" typeRef="string">
          <text></text>
        </inputExpression>
      </input>
      <output id="Output_1" label="Output 1" name="output1" typeRef="string" />
      <rule id="DecisionRule_0rz0pev">
        <description>test</description>
        <inputEntry id="UnaryTests_1vvd8vz">
          <text>"Rule1"</text>
        </inputEntry>
        <outputEntry id="LiteralExpression_1y70lrx">
          <text>"Rule1OutputV2"</text>
        </outputEntry>
      </rule>
    </decisionTable>
  </decision>
  <dmndi:DMNDI>
    <dmndi:DMNDiagram>
      <dmndi:DMNShape dmnElementRef="decision_table">
        <dc:Bounds height="80" width="180" x="160" y="100" />
      </dmndi:DMNShape>
    </dmndi:DMNDiagram>
  </dmndi:DMNDI>
</definitions>
