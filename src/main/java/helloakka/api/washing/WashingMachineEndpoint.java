package helloakka.api.washing;

import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.http.HttpResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionStage;

/**
 * REST endpoint for washing machine operations.
 * Available at http://localhost:9000/washing-machines
 *
 * Example usage:
 * 
 * 1. Start a washing cycle:
 * ```bash
 * curl -X POST http://localhost:9000/washing-machines/machine1/start \
 *   -H "Content-Type: application/json" \
 *   -d '{
 *     "program": "normal",
 *     "temperature": 60
 *   }'
 * ```
 * Response: 200 OK with message on success, 400 Bad Request with error message on failure
 * 
 * 2. Get washing machine status:
 * ```bash
 * curl http://localhost:9000/washing-machines/machine1
 * ```
 * Response example:
 * ```json
 * {
 *   "cycleId": "machine1",
 *   "program": "normal",
 *   "temperature": 60,
 *   "status": "WASHING",
 *   "startTime": "2024-01-21T10:30:00Z",
 *   "lastUpdated": "2024-01-21T10:31:00Z"
 * }
 * ```
 */
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint("/washing-machines")
public class WashingMachineEndpoint {

    private final ComponentClient componentClient;
    private static final Logger logger = LoggerFactory.getLogger(WashingMachineEndpoint.class);

    public WashingMachineEndpoint(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

    @Get("/{machineId}")
    public CompletionStage<WashingMachineState> getStatus(String machineId) {
        logger.info("Get washing machine status id={}", machineId);
        return componentClient.forWorkflow(machineId)
            .method(WashingMachineWorkflow::getStatus)
            .invokeAsync();
    }

    @Post("/{machineId}/start")
    public CompletionStage<HttpResponse> startWashing(String machineId, StartWashing command) {
        logger.info("Starting washing machine id={} program={} temperature={}°C", 
            machineId, command.program(), command.temperature());
            
        return componentClient.forWorkflow(machineId)
            .method(WashingMachineWorkflow::startWashing)
            .invokeAsync(command)
            .thenApply(response -> {
                if (response instanceof Response.Success) {
                    return HttpResponses.ok();
                } else {
                    Response.Failure failure = (Response.Failure) response;
                    return HttpResponse.create()
                        .withStatus(StatusCodes.BAD_REQUEST)
                        .withEntity(failure.message());
                }
            });
    }
} 