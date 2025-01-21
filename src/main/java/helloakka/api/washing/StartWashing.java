package helloakka.api.washing;

public class StartWashing {
    private final String program;
    private final int temperature;

    public StartWashing(String program, int temperature) {
        this.program = program;
        this.temperature = temperature;
    }

    public String program() { return program; }
    public int temperature() { return temperature; }
} 