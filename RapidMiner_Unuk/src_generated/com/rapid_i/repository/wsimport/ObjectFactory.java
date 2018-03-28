
package com.rapid_i.repository.wsimport;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.rapid_i.repository.wsimport package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _GetProcessIdsForJobId_QNAME = new QName("http://service.web.rapidanalytics.de/", "getProcessIdsForJobId");
    private final static QName _GetRunningProcessesInfo_QNAME = new QName("http://service.web.rapidanalytics.de/", "getRunningProcessesInfo");
    private final static QName _StopProcess_QNAME = new QName("http://service.web.rapidanalytics.de/", "stopProcess");
    private final static QName _GetRunningProcessesInfoResponse_QNAME = new QName("http://service.web.rapidanalytics.de/", "getRunningProcessesInfoResponse");
    private final static QName _ExecuteProcessCron_QNAME = new QName("http://service.web.rapidanalytics.de/", "executeProcessCron");
    private final static QName _ExecuteProcessSimpleResponse_QNAME = new QName("http://service.web.rapidanalytics.de/", "executeProcessSimpleResponse");
    private final static QName _GetProcessIdsForJobIdResponse_QNAME = new QName("http://service.web.rapidanalytics.de/", "getProcessIdsForJobIdResponse");
    private final static QName _CancelTriggerResponse_QNAME = new QName("http://service.web.rapidanalytics.de/", "cancelTriggerResponse");
    private final static QName _GetRunningProcessesResponse_QNAME = new QName("http://service.web.rapidanalytics.de/", "getRunningProcessesResponse");
    private final static QName _StopProcessResponse_QNAME = new QName("http://service.web.rapidanalytics.de/", "stopProcessResponse");
    private final static QName _CancelTrigger_QNAME = new QName("http://service.web.rapidanalytics.de/", "cancelTrigger");
    private final static QName _GetRunningProcesses_QNAME = new QName("http://service.web.rapidanalytics.de/", "getRunningProcesses");
    private final static QName _ExecuteProcessCronResponse_QNAME = new QName("http://service.web.rapidanalytics.de/", "executeProcessCronResponse");
    private final static QName _ExecuteProcessSimple_QNAME = new QName("http://service.web.rapidanalytics.de/", "executeProcessSimple");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.rapid_i.repository.wsimport
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CancelTriggerResponse }
     * 
     */
    public CancelTriggerResponse createCancelTriggerResponse() {
        return new CancelTriggerResponse();
    }

    /**
     * Create an instance of {@link GetRunningProcessesResponse }
     * 
     */
    public GetRunningProcessesResponse createGetRunningProcessesResponse() {
        return new GetRunningProcessesResponse();
    }

    /**
     * Create an instance of {@link ProcessContextWrapper }
     * 
     */
    public ProcessContextWrapper createProcessContextWrapper() {
        return new ProcessContextWrapper();
    }

    /**
     * Create an instance of {@link ExecuteProcessCron }
     * 
     */
    public ExecuteProcessCron createExecuteProcessCron() {
        return new ExecuteProcessCron();
    }

    /**
     * Create an instance of {@link GetProcessIdsForJobId }
     * 
     */
    public GetProcessIdsForJobId createGetProcessIdsForJobId() {
        return new GetProcessIdsForJobId();
    }

    /**
     * Create an instance of {@link ProcessStackTraceElement }
     * 
     */
    public ProcessStackTraceElement createProcessStackTraceElement() {
        return new ProcessStackTraceElement();
    }

    /**
     * Create an instance of {@link GetProcessIdsForJobIdResponse }
     * 
     */
    public GetProcessIdsForJobIdResponse createGetProcessIdsForJobIdResponse() {
        return new GetProcessIdsForJobIdResponse();
    }

    /**
     * Create an instance of {@link CancelTrigger }
     * 
     */
    public CancelTrigger createCancelTrigger() {
        return new CancelTrigger();
    }

    /**
     * Create an instance of {@link GetRunningProcesses }
     * 
     */
    public GetRunningProcesses createGetRunningProcesses() {
        return new GetRunningProcesses();
    }

    /**
     * Create an instance of {@link ProcessResponse }
     * 
     */
    public ProcessResponse createProcessResponse() {
        return new ProcessResponse();
    }

    /**
     * Create an instance of {@link StopProcess }
     * 
     */
    public StopProcess createStopProcess() {
        return new StopProcess();
    }

    /**
     * Create an instance of {@link Response }
     * 
     */
    public Response createResponse() {
        return new Response();
    }

    /**
     * Create an instance of {@link MacroDefinition }
     * 
     */
    public MacroDefinition createMacroDefinition() {
        return new MacroDefinition();
    }

    /**
     * Create an instance of {@link ExecuteProcessSimple }
     * 
     */
    public ExecuteProcessSimple createExecuteProcessSimple() {
        return new ExecuteProcessSimple();
    }

    /**
     * Create an instance of {@link StopProcessResponse }
     * 
     */
    public StopProcessResponse createStopProcessResponse() {
        return new StopProcessResponse();
    }

    /**
     * Create an instance of {@link GetRunningProcessesInfo }
     * 
     */
    public GetRunningProcessesInfo createGetRunningProcessesInfo() {
        return new GetRunningProcessesInfo();
    }

    /**
     * Create an instance of {@link ExecuteProcessSimpleResponse }
     * 
     */
    public ExecuteProcessSimpleResponse createExecuteProcessSimpleResponse() {
        return new ExecuteProcessSimpleResponse();
    }

    /**
     * Create an instance of {@link ExecuteProcessCronResponse }
     * 
     */
    public ExecuteProcessCronResponse createExecuteProcessCronResponse() {
        return new ExecuteProcessCronResponse();
    }

    /**
     * Create an instance of {@link ExecutionResponse }
     * 
     */
    public ExecutionResponse createExecutionResponse() {
        return new ExecutionResponse();
    }

    /**
     * Create an instance of {@link GetRunningProcessesInfoResponse }
     * 
     */
    public GetRunningProcessesInfoResponse createGetRunningProcessesInfoResponse() {
        return new GetRunningProcessesInfoResponse();
    }

    /**
     * Create an instance of {@link ProcessStackTrace }
     * 
     */
    public ProcessStackTrace createProcessStackTrace() {
        return new ProcessStackTrace();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetProcessIdsForJobId }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.web.rapidanalytics.de/", name = "getProcessIdsForJobId")
    public JAXBElement<GetProcessIdsForJobId> createGetProcessIdsForJobId(GetProcessIdsForJobId value) {
        return new JAXBElement<GetProcessIdsForJobId>(_GetProcessIdsForJobId_QNAME, GetProcessIdsForJobId.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetRunningProcessesInfo }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.web.rapidanalytics.de/", name = "getRunningProcessesInfo")
    public JAXBElement<GetRunningProcessesInfo> createGetRunningProcessesInfo(GetRunningProcessesInfo value) {
        return new JAXBElement<GetRunningProcessesInfo>(_GetRunningProcessesInfo_QNAME, GetRunningProcessesInfo.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StopProcess }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.web.rapidanalytics.de/", name = "stopProcess")
    public JAXBElement<StopProcess> createStopProcess(StopProcess value) {
        return new JAXBElement<StopProcess>(_StopProcess_QNAME, StopProcess.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetRunningProcessesInfoResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.web.rapidanalytics.de/", name = "getRunningProcessesInfoResponse")
    public JAXBElement<GetRunningProcessesInfoResponse> createGetRunningProcessesInfoResponse(GetRunningProcessesInfoResponse value) {
        return new JAXBElement<GetRunningProcessesInfoResponse>(_GetRunningProcessesInfoResponse_QNAME, GetRunningProcessesInfoResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExecuteProcessCron }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.web.rapidanalytics.de/", name = "executeProcessCron")
    public JAXBElement<ExecuteProcessCron> createExecuteProcessCron(ExecuteProcessCron value) {
        return new JAXBElement<ExecuteProcessCron>(_ExecuteProcessCron_QNAME, ExecuteProcessCron.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExecuteProcessSimpleResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.web.rapidanalytics.de/", name = "executeProcessSimpleResponse")
    public JAXBElement<ExecuteProcessSimpleResponse> createExecuteProcessSimpleResponse(ExecuteProcessSimpleResponse value) {
        return new JAXBElement<ExecuteProcessSimpleResponse>(_ExecuteProcessSimpleResponse_QNAME, ExecuteProcessSimpleResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetProcessIdsForJobIdResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.web.rapidanalytics.de/", name = "getProcessIdsForJobIdResponse")
    public JAXBElement<GetProcessIdsForJobIdResponse> createGetProcessIdsForJobIdResponse(GetProcessIdsForJobIdResponse value) {
        return new JAXBElement<GetProcessIdsForJobIdResponse>(_GetProcessIdsForJobIdResponse_QNAME, GetProcessIdsForJobIdResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CancelTriggerResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.web.rapidanalytics.de/", name = "cancelTriggerResponse")
    public JAXBElement<CancelTriggerResponse> createCancelTriggerResponse(CancelTriggerResponse value) {
        return new JAXBElement<CancelTriggerResponse>(_CancelTriggerResponse_QNAME, CancelTriggerResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetRunningProcessesResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.web.rapidanalytics.de/", name = "getRunningProcessesResponse")
    public JAXBElement<GetRunningProcessesResponse> createGetRunningProcessesResponse(GetRunningProcessesResponse value) {
        return new JAXBElement<GetRunningProcessesResponse>(_GetRunningProcessesResponse_QNAME, GetRunningProcessesResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StopProcessResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.web.rapidanalytics.de/", name = "stopProcessResponse")
    public JAXBElement<StopProcessResponse> createStopProcessResponse(StopProcessResponse value) {
        return new JAXBElement<StopProcessResponse>(_StopProcessResponse_QNAME, StopProcessResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CancelTrigger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.web.rapidanalytics.de/", name = "cancelTrigger")
    public JAXBElement<CancelTrigger> createCancelTrigger(CancelTrigger value) {
        return new JAXBElement<CancelTrigger>(_CancelTrigger_QNAME, CancelTrigger.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetRunningProcesses }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.web.rapidanalytics.de/", name = "getRunningProcesses")
    public JAXBElement<GetRunningProcesses> createGetRunningProcesses(GetRunningProcesses value) {
        return new JAXBElement<GetRunningProcesses>(_GetRunningProcesses_QNAME, GetRunningProcesses.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExecuteProcessCronResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.web.rapidanalytics.de/", name = "executeProcessCronResponse")
    public JAXBElement<ExecuteProcessCronResponse> createExecuteProcessCronResponse(ExecuteProcessCronResponse value) {
        return new JAXBElement<ExecuteProcessCronResponse>(_ExecuteProcessCronResponse_QNAME, ExecuteProcessCronResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExecuteProcessSimple }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.web.rapidanalytics.de/", name = "executeProcessSimple")
    public JAXBElement<ExecuteProcessSimple> createExecuteProcessSimple(ExecuteProcessSimple value) {
        return new JAXBElement<ExecuteProcessSimple>(_ExecuteProcessSimple_QNAME, ExecuteProcessSimple.class, null, value);
    }

}
