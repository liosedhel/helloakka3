package helloakka.api.washing;  // Updated package 

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public class WashingMachineState {
    private final String cycleId;
    private final String program;
    private final int temperature;
    private final WashingMachineStatus status;
    private final Instant startTime;
    private final Instant lastUpdated;

    @JsonCreator
    public WashingMachineState(
        @JsonProperty("cycleId") String cycleId,
        @JsonProperty("program") String program,
        @JsonProperty("temperature") int temperature,
        @JsonProperty("status") WashingMachineStatus status,
        @JsonProperty("startTime") Instant startTime,
        @JsonProperty("lastUpdated") Instant lastUpdated) {
        this.cycleId = cycleId;
        this.program = program;
        this.temperature = temperature;
        this.status = status;
        this.startTime = startTime;
        this.lastUpdated = lastUpdated;
    }

    public WashingMachineState(String cycleId, String program, int temperature, WashingMachineStatus status) {
        this(cycleId, program, temperature, status, Instant.now(), Instant.now());
    }

    public WashingMachineState setStatus(WashingMachineStatus newStatus) {
        return new WashingMachineState(
            this.cycleId, 
            this.program, 
            this.temperature, 
            newStatus,
            this.startTime,
            Instant.now()
        );
    }

    @JsonProperty("cycleId")
    public String getCycleId() { return cycleId; }
    
    @JsonProperty("program")
    public String getProgram() { return program; }
    
    @JsonProperty("temperature")
    public int getTemperature() { return temperature; }
    
    @JsonProperty("status")
    public WashingMachineStatus getStatus() { return status; }
    
    @JsonProperty("startTime")
    public Instant getStartTime() { return startTime; }
    
    @JsonProperty("lastUpdated")
    public Instant getLastUpdated() { return lastUpdated; }

    @Override
    public String toString() {
        return String.format("WashingMachineState[cycleId=%s, program=%s, temperature=%d°C, status=%s]",
            cycleId, program, temperature, status);
    }
} 