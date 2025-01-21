package helloakka.api.washing;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Response.Success.class, name = "success"),
    @JsonSubTypes.Type(value = Response.Failure.class, name = "failure")
})
public sealed interface Response {
    record Success(String message) implements Response {
        public static Success of(String message) {
            return new Success(message);
        }
    }

    record Failure(String message) implements Response {
        public static Failure of(String message) {
            return new Failure(message);
        }
    }
} 