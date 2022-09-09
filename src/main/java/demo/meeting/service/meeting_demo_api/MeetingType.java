package demo.meeting.service.meeting_demo_api;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum MeetingType {
    Live("Live"),
    InPerson("InPerson");
    
    private static volatile Map<String, MeetingType> enums = Stream.of(MeetingType.values()).collect(Collectors.toMap(Object::toString, Function.identity()));

    private String name;

    MeetingType (String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static MeetingType get (String name) {
        return enums.get(name.toLowerCase());
    }
}
