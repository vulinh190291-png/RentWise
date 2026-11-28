package com.rentwise.content;
import org.junit.jupiter.api.Test;import java.util.*;import static org.junit.jupiter.api.Assertions.*;
class ComparisonTest {
 Map<String,Object> value(){return new HashMap<>(Map.of("numericValue",100,"period","2025","unit","元/月","scope","全市","method","中位数"));}
 @Test void states(){var a=value();var b=value();assertEquals("COMPARABLE",ContentService.status(a,b));b.put("period","2024");assertEquals("DIFFERENT_PERIOD",ContentService.status(a,b));b.put("unit","元/平方米");assertEquals("INCOMPARABLE",ContentService.status(a,b));assertEquals("MISSING",ContentService.status(a,null));a.put("period","UNSPECIFIED");assertEquals("INCOMPARABLE",ContentService.status(a,value()));}
}
