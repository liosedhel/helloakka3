package helloakka.api.washing;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.workflow.Workflow;
import akka.javasdk.workflow.Workflow.Effect.TransitionalEffect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.CompletableFuture;
import static java.time.Duration.ofMinutes;
import java.util.Random;

@ComponentId("washing-machine")
public class WashingMachineWorkflow extends Workflow<WashingMachineState> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ComponentClient componentClient;
    private final Random random = new Random();

    public WashingMachineWorkflow(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

    @Override
    public WorkflowDef<WashingMachineState> definition() {
        Step fillWater = step("fill-water")
            .asyncCall(this::fillWaterStep)
            .andThen(Response.class, this::moveToWashing);

        Step washing = step("washing")
            .asyncCall(this::washingStep)
            .andThen(Response.class, this::moveToRinsing);

        Step rinsing = step("rinsing")
            .asyncCall(this::rinsingStep)
            .andThen(Response.class, this::moveToSpinning);

        Step spinning = step("spinning")
            .asyncCall(this::spinningStep)
            .andThen(Response.class, this::finishCycle);
        Step error = step("error")
            .asyncCall(this::handleError)
            .andThen(Response.class, this::finalizeError);

        return workflow()
            .timeout(ofMinutes(2))
            .defaultStepTimeout(ofMinutes(1))
            .addStep(fillWater)
            .addStep(washing)
            .addStep(rinsing)
            .addStep(spinning)
            .addStep(error);
    }

    private CompletionStage<Response> fillWaterStep() {
        logger.info("Filling water for cycle {}", currentState().getCycleId());
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000); // Simulate work
                if (random.nextInt(10) == 0) { // 10% chance of failure
                    throw new RuntimeException("Water valve malfunction");
                }
                if (random.nextInt(10) == 1) { // Another 10% chance of different failure
                    throw new RuntimeException("Water pressure too low");
                }
                return Response.Success.of("Water filled");
            } catch (Exception e) {
                logger.error("Error filling water", e);
                return Response.Failure.of("Failed to fill water: " + e.getMessage());
            }
        });
    }

    private CompletionStage<Response> washingStep() {
        logger.info("Washing clothes for cycle {} with program {} at {}°C", 
            currentState().getCycleId(),
            currentState().getProgram(),
            currentState().getTemperature());
            
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000); // Simulate washing
                if (random.nextInt(10) == 0) { // 10% chance of failure
                    throw new RuntimeException("Drum motor overheated");
                }
                if (random.nextInt(10) == 1) { // Another 10% chance
                    throw new RuntimeException("Door lock malfunction");
                }
                if (currentState().getTemperature() > 90 && random.nextInt(5) == 0) { // 20% chance at high temps
                    throw new RuntimeException("Temperature sensor failure");
                }
                return Response.Success.of("Washing completed");
            } catch (Exception e) {
                logger.error("Error during washing", e);
                return Response.Failure.of("Washing failed: " + e.getMessage());
            }
        });
    }

    private CompletionStage<Response> rinsingStep() {
        logger.info("Rinsing clothes for cycle {}", currentState().getCycleId());
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1500); // Simulate rinsing
                if (random.nextInt(10) == 0) { // 10% chance of failure
                    throw new RuntimeException("Water drain blocked");
                }
                if (random.nextInt(15) == 1) { // ~7% chance
                    throw new RuntimeException("Water inlet valve stuck");
                }
                return Response.Success.of("Rinsing completed");
            } catch (Exception e) {
                logger.error("Error during rinsing", e);
                return Response.Failure.of("Rinsing failed: " + e.getMessage());
            }
        });
    }

    private CompletionStage<Response> spinningStep() {
        logger.info("Spinning clothes for cycle {}", currentState().getCycleId());
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000); // Simulate spinning
                if (random.nextInt(10) == 0) { // 10% chance of failure
                    throw new RuntimeException("Drum imbalance detected");
                }
                if (random.nextInt(12) == 1) { // ~8% chance
                    throw new RuntimeException("Spin speed sensor failure");
                }
                if (random.nextInt(20) == 1) { // 5% chance
                    throw new RuntimeException("Excessive vibration detected");
                }
                return Response.Success.of("Spinning completed");
            } catch (Exception e) {
                logger.error("Error during spinning", e);
                return Response.Failure.of("Spinning failed: " + e.getMessage());
            }
        });
    }

    private CompletionStage<Response> handleError() {
      logger.error("Washing machine failed in state: {}", currentState().getStatus());
        return CompletableFuture.completedFuture(
            Response.Failure.of("Machine failed during " + currentState().getStatus())
        );
    }

    private TransitionalEffect<Void> finalizeError(Response response) {
        logger.info("Finalizing error state");
        return effects()
            .updateState(currentState().setStatus(WashingMachineStatus.ERROR))
            .end();
    }

    private TransitionalEffect<Void> moveToWashing(Response response) {
        return switch (response) {
            case Response.Success s -> {
                logger.info("Moving to washing step");
                yield effects()
                    .updateState(currentState().setStatus(WashingMachineStatus.WASHING))
                    .transitionTo("washing");
            }
            case Response.Failure f -> {
                logger.error("Failed to fill water");
                yield effects()
                    .updateState(currentState())
                    .transitionTo("error");
            }
        };
    }

    private TransitionalEffect<Void> moveToRinsing(Response response) {
        return switch (response) {
            case Response.Success s -> {
                logger.info("Moving to rinsing step");
                yield effects()
                    .updateState(currentState().setStatus(WashingMachineStatus.RINSING))
                    .transitionTo("rinsing");
            }
            case Response.Failure f -> {
                logger.error("Washing failed");
                yield effects()
                    .updateState(currentState())
                    .transitionTo("error");
            }
        };
    }

    private TransitionalEffect<Void> moveToSpinning(Response response) {
        return switch (response) {
            case Response.Success s -> {
                logger.info("Moving to spinning step");
                yield effects()
                    .updateState(currentState().setStatus(WashingMachineStatus.SPINNING))
                    .transitionTo("spinning");
            }
            case Response.Failure f -> {
                logger.error("Rinsing failed");
                yield effects()
                    .updateState(currentState())
                    .transitionTo("error");
            }
        };
    }

    private TransitionalEffect<Void> finishCycle(Response response) {
        return switch (response) {
            case Response.Success s -> {
                logger.info("Washing cycle completed");
                yield effects()
                    .updateState(currentState().setStatus(WashingMachineStatus.COMPLETED))
                    .end();
            }
            case Response.Failure f -> {
                logger.error("Spinning failed");
                yield effects()
                    .updateState(currentState())
                    .transitionTo("error");
            }
        };
    }

    public Effect<Response> startWashing(StartWashing command) {
        if (currentState() != null) {
            logger.warn("Attempt to start washing when machine is already running");
            return effects().error("Washing machine is already running. Current status: " + currentState().getStatus());
        }

        // Validate input
        if (command.temperature() < 0 || command.temperature() > 95) {
            return effects().error("Invalid temperature. Must be between 0 and 95°C");
        }
        if (command.program() == null || command.program().trim().isEmpty()) {
            return effects().error("Program must be specified");
        }

        String cycleId = commandContext().workflowId();
        logger.info("Starting new washing cycle {} with program {} at {}°C", 
            cycleId, command.program(), command.temperature());

        WashingMachineState state = new WashingMachineState(
            cycleId,
            command.program(),
            command.temperature(),
            WashingMachineStatus.FILLING
        );

        return effects()
            .updateState(state)
            .transitionTo("fill-water")
            .thenReply(Response.Success.of("Washing cycle " + cycleId + " started"));
    }

    public Effect<WashingMachineState> getStatus() {
        if (currentState() == null) {
            return effects().error("No washing cycle in progress");
        }
        return effects().reply(currentState());
    }
} 