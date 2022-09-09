package demo.meeting.service.meeting_demo_api.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import demo.meeting.service.meeting_demo_api.services.MeetingManager;

@Configuration
public class MeetingManagerConfiguration {

    @Bean(name= {"getMeetingManager","MeetingManager"}, initMethod="init", destroyMethod="close")
    public MeetingManager fromJsonFile(){
        return new MeetingManager();
    }
}
