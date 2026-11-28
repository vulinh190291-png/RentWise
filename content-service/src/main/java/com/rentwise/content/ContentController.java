package com.rentwise.content;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/content")
public class ContentController {
 private final ContentService service;
 public ContentController(ContentService service){this.service=service;}
 @GetMapping("/cities") public Object cities(){return service.cities();}
 @GetMapping("/cities/{slug}") public Object city(@PathVariable String slug){return service.detail(slug);}
 @GetMapping("/compare") public Object compare(@RequestParam String left,@RequestParam String right){return service.compare(left,right);}
}
