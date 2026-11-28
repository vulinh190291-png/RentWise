package com.rentwise.content;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
@Service
@org.springframework.transaction.annotation.Transactional(readOnly=true)
public class ContentService {
 private final JdbcTemplate db;
 public ContentService(JdbcTemplate db){this.db=db;}
 public List<Map<String,Object>> cities(){return db.queryForList("SELECT slug,name FROM city ORDER BY id");}
 public Map<String,Object> city(String slug){return db.queryForList("SELECT slug,name FROM city WHERE slug=?",slug).stream().findFirst().orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"City not found"));}
 public static String status(Map<String,Object> a,Map<String,Object> b){
  if(a==null||b==null)return "MISSING";
  if((a.get("numericValue")==null && (a.get("textValue")==null||a.get("textValue").toString().isBlank()))||(b.get("numericValue")==null && (b.get("textValue")==null||b.get("textValue").toString().isBlank())))return "MISSING";
  for(String k:List.of("unit","scope","method")) if(a.get(k)==null||a.get(k).toString().isBlank()||!Objects.equals(a.get(k),b.get(k)))return "INCOMPARABLE";
  if(a.get("period")==null||b.get("period")==null||a.get("period").equals("UNSPECIFIED")||b.get("period").equals("UNSPECIFIED"))return "INCOMPARABLE";
  if(!Objects.equals(a.get("period"),b.get("period")))return "DIFFERENT_PERIOD";
  return "COMPARABLE";
 }
 public Map<String,Object> detail(String slug){var result=new LinkedHashMap<String,Object>(city(slug));var metrics=db.queryForList("SELECT id,code,name,dimension,kind FROM metric ORDER BY id");for(var m:metrics)m.put("value",value(slug,((Number)m.get("id")).longValue()));result.put("metrics",metrics);return result;}
 private Map<String,Object> value(String slug,long metric){return db.queryForList("SELECT v.text_value AS textValue,v.numeric_value AS numericValue,v.period,v.unit,v.scope,v.method,s.title AS sourceTitle,s.url AS sourceUrl,s.checked_at AS checkedAt FROM city_metric_value v JOIN city c ON c.id=v.city_id JOIN source s ON s.id=v.source_id WHERE c.slug=? AND v.metric_id=?",slug,metric).stream().findFirst().orElse(null);}
 public Map<String,Object> compare(String left,String right){
  if(left.equals(right))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose two different cities");
  var l=city(left);var r=city(right);var metrics=db.queryForList("SELECT id,code,name,dimension,kind FROM metric ORDER BY id");
  List<Map<String,Object>> rows=new ArrayList<>();
  for(var m:metrics){var a=value(left,((Number)m.get("id")).longValue());var b=value(right,((Number)m.get("id")).longValue());var row=new LinkedHashMap<String,Object>(m);row.put("left",a);row.put("right",b);row.put("status",status(a,b));rows.add(row);}
  return Map.of("leftCity",l,"rightCity",r,"metrics",rows);
 }
}
