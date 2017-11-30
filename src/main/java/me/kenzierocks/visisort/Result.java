package me.kenzierocks.visisort;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Result {
    
    public static Result empty() {
        return of(false, -1);
    }
    
    public static Result of(int result) {
        return of(true, result);
    }
    
    private static Result of(boolean present, int value) {
        return new AutoValue_Result(present, value);
    }

    Result() {
    }
    
    public abstract boolean isPresent();
    
    public abstract int getValue();

}
