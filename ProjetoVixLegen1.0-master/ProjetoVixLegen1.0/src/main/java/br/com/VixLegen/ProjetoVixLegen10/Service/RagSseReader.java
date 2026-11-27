package br.com.VixLegen.ProjetoVixLegen10.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.BiFunction;

/** Parser de SSE incremental: não acumula a resposta HTTP inteira. */
public final class RagSseReader {
    private RagSseReader(){}
    public static Map<String,Object> read(InputStream in,BiFunction<String,String,Map<String,Object>> handler) throws IOException {
        var reader=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8));
        String event="";var data=new StringBuilder();String line;
        while((line=reader.readLine())!=null){
            if(line.startsWith(":")){handler.apply("ping","");continue;}
            if(line.isEmpty()){
                if(!event.isEmpty()&&!data.isEmpty()){
                    var result=handler.apply(event,data.toString());
                    if(result!=null)return result;
                }
                event="";data.setLength(0);continue;
            }
            if(line.startsWith("event:"))event=line.substring(6).strip();
            else if(line.startsWith("data:")){
                String value=line.substring(5);if(value.startsWith(" "))value=value.substring(1);
                if(!data.isEmpty())data.append('\n');data.append(value);
                if(data.length()>2_000_000)throw new IOException("Evento SSE acima do limite");
            }
        }
        throw new EOFException("Stream sem resultado final");
    }
}
