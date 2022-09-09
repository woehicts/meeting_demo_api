package demo.meeting.service.meeting_demo_api;
import java.time.LocalDateTime;

/**
 * TimeRange class for various time range functions
 */
public class TimeRange {
    private LocalDateTime from, to;
    public TimeRange(LocalDateTime from, LocalDateTime to){
        this.from = from;
        this.to = to;
    }

    public LocalDateTime getFrom(){
        return from;
    }

    public LocalDateTime getTo(){
        return to;
    }

    /**
    * Checks whether a given time range intersects another
    */
    public boolean intersects(TimeRange other){
        LocalDateTime from2 = other.getFrom();
        LocalDateTime to2 = other.getTo();
        if ((from2.isAfter(from) && from2.isBefore(to)) ||
             (to2.isAfter(from) && to2.isBefore(to)) ||
             (from.isAfter(from2) && from.isBefore(to2)) ||
             (to.isAfter(from2) && to.isBefore(to2))){
            return true;
        }else{
            return false;
        }
    }
}
