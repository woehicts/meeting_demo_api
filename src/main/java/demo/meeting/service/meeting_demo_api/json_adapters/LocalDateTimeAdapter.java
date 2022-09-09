package demo.meeting.service.meeting_demo_api.json_adapters;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");

    @Override
    public void write(JsonWriter out, LocalDateTime value) throws IOException { 
        out.beginObject();
        out.name("LocalDateTime"); 
        out.value(value.format(formatter)); 
        out.endObject();
    }

    @Override
    public LocalDateTime read(JsonReader in) throws IOException {
      LocalDateTime time = null;
      in.beginObject(); 
      String fieldname = null; 
      
      while (in.hasNext()) { 
         JsonToken token = in.peek();            
         
         if (token.equals(JsonToken.NAME)) {     
            fieldname = in.nextName(); 
         } 
         
         if ("LocalDateTime".equals(fieldname)) {       
            token = in.peek(); 
            time = LocalDateTime.parse(in.nextString(), formatter); 
         }               
      } 
     in.endObject();
    return time;
    }
    
}
