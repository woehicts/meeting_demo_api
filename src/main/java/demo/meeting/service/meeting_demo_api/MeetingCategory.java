package demo.meeting.service.meeting_demo_api;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum MeetingCategory {
    CodeMonkey("CodeMonkey"),
    Hub("Hub"),
    Short("Short"),
    TeamBuilding("TeamBuilding");

    private static volatile Map<String, MeetingCategory> enums = Stream.of(MeetingCategory.values()).collect(Collectors.toMap(Object::toString, Function.identity()));

    private String name;

    MeetingCategory (String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static MeetingCategory get (String name) {
        return enums.get(name.toLowerCase());
    }
}
