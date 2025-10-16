// ===== Controller to Inspect Raw Sensor Data =====
package com.fitness.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.*;

@RestController
@RequestMapping("/api/sensors")
public class SensorDataInspectorController {
    
    private final ObjectMapper objectMapper = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT);
    
    /**
     * ENDPOINT 1: Print everything as raw string
     * Use this URL in Sensor Logger: http://your-ip:8081/api/sensors/debug/raw
     */
    @PostMapping("/debug/raw")
    public ResponseEntity<String> debugRawData(@RequestBody String rawBody) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📱 RAW DATA RECEIVED FROM SENSOR LOGGER");
        System.out.println("=".repeat(80));
        System.out.println(rawBody);
        System.out.println("=".repeat(80) + "\n");
        
        return ResponseEntity.ok("✅ Data received! Check your console/logs");
    }
    
    /**
     * ENDPOINT 2: Print as JSON object (auto-parsed)
     * Use this URL: http://your-ip:8081/api/sensors/debug/json
     */
    @PostMapping("/debug/json")
    public ResponseEntity<String> debugJsonData(@RequestBody Object data) {
        try {
            String prettyJson = objectMapper.writeValueAsString(data);
            
            System.out.println("\n" + "=".repeat(80));
            System.out.println("📱 JSON DATA RECEIVED FROM SENSOR LOGGER");
            System.out.println("=".repeat(80));
            System.out.println(prettyJson);
            System.out.println("=".repeat(80));
            System.out.println("🔍 Data Type: " + data.getClass().getName());
            
            if (data instanceof List) {
                System.out.println("📊 List size: " + ((List<?>) data).size());
            } else if (data instanceof Map) {
                System.out.println("📊 Map keys: " + ((Map<?, ?>) data).keySet());
            }
            System.out.println("=".repeat(80) + "\n");
            
            return ResponseEntity.ok("✅ Data received! Check your console/logs");
            
        } catch (Exception e) {
            System.err.println("❌ Error parsing JSON: " + e.getMessage());
            return ResponseEntity.ok("⚠️ Data received but couldn't parse");
        }
    }
    
    /**
     * ENDPOINT 3: Print with all headers
     * Use this URL: http://your-ip:8081/api/sensors/debug/full
     */
    @PostMapping("/debug/full")
    public ResponseEntity<String> debugFullData(
            @RequestBody String rawBody,
            HttpServletRequest request) {
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📱 FULL REQUEST FROM SENSOR LOGGER");
        System.out.println("=".repeat(80));
        
        // Print Headers
        System.out.println("\n📋 HEADERS:");
        System.out.println("-".repeat(40));
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            System.out.println(headerName + ": " + headerValue);
        }
        
        // Print Request Info
        System.out.println("\n🌐 REQUEST INFO:");
        System.out.println("-".repeat(40));
        System.out.println("Method: " + request.getMethod());
        System.out.println("Content-Type: " + request.getContentType());
        System.out.println("Content-Length: " + request.getContentLength());
        System.out.println("Remote Address: " + request.getRemoteAddr());
        
        // Print Body
        System.out.println("\n📦 BODY:");
        System.out.println("-".repeat(40));
        System.out.println(rawBody);
        
        System.out.println("\n" + "=".repeat(80) + "\n");
        
        return ResponseEntity.ok("✅ Full data logged! Check console");
    }
    
    /**
     * ENDPOINT 4: Detailed analysis with structure detection
     * Use this URL: http://your-ip:8081/api/sensors/debug/analyze
     */
    @PostMapping("/debug/analyze")
    public ResponseEntity<Map<String, Object>> debugAnalyze(@RequestBody Object data) {
        Map<String, Object> analysis = new HashMap<>();
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🔬 ANALYZING SENSOR DATA STRUCTURE");
        System.out.println("=".repeat(80));
        
        try {
            String prettyJson = objectMapper.writeValueAsString(data);
            System.out.println(prettyJson);
            
            // Analyze structure
            System.out.println("\n📊 STRUCTURE ANALYSIS:");
            System.out.println("-".repeat(40));
            
            analysis.put("dataType", data.getClass().getSimpleName());
            
            if (data instanceof List) {
                List<?> list = (List<?>) data;
                analysis.put("isList", true);
                analysis.put("size", list.size());
                
                System.out.println("✓ Data is a List");
                System.out.println("✓ Size: " + list.size());
                
                if (!list.isEmpty()) {
                    Object firstItem = list.get(0);
                    System.out.println("✓ First item type: " + firstItem.getClass().getSimpleName());
                    analysis.put("firstItemType", firstItem.getClass().getSimpleName());
                    
                    if (firstItem instanceof Map) {
                        Map<?, ?> firstMap = (Map<?, ?>) firstItem;
                        System.out.println("✓ First item keys: " + firstMap.keySet());
                        analysis.put("sampleKeys", new ArrayList<>(firstMap.keySet()));
                        
                        // Print first item details
                        System.out.println("\n📝 FIRST ITEM SAMPLE:");
                        System.out.println("-".repeat(40));
                        firstMap.forEach((key, value) -> {
                            System.out.println(key + " = " + value + 
                                " (type: " + (value != null ? value.getClass().getSimpleName() : "null") + ")");
                        });
                    }
                }
            } else if (data instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) data;
                analysis.put("isMap", true);
                analysis.put("keys", new ArrayList<>(map.keySet()));
                
                System.out.println("✓ Data is a Map/Object");
                System.out.println("✓ Keys: " + map.keySet());
                
                // Print all fields
                System.out.println("\n📝 FIELDS:");
                System.out.println("-".repeat(40));
                map.forEach((key, value) -> {
                    System.out.println(key + " = " + value + 
                        " (type: " + (value != null ? value.getClass().getSimpleName() : "null") + ")");
                });
            }
            
            System.out.println("\n" + "=".repeat(80) + "\n");
            
            analysis.put("status", "success");
            analysis.put("message", "Check your console for detailed analysis");
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            analysis.put("status", "error");
            analysis.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(analysis);
    }
    
    /**
     * ENDPOINT 5: Simple test endpoint (GET)
     * Visit in browser: http://your-ip:8081/api/sensors/debug/test
     */
    @GetMapping("/debug/test")
    public ResponseEntity<Map<String, String>> testEndpoint() {
        System.out.println("✅ Test endpoint accessed successfully!");
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "Server is running");
        response.put("message", "Configure Sensor Logger to POST to one of these endpoints:");
        response.put("endpoint1", "/api/sensors/debug/raw");
        response.put("endpoint2", "/api/sensors/debug/json");
        response.put("endpoint3", "/api/sensors/debug/full");
        response.put("endpoint4", "/api/sensors/debug/analyze");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * ENDPOINT 6: Catch-all for any path under /debug
     */
    @RequestMapping(value = "/debug/**", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<String> catchAll(
            @RequestBody(required = false) String body,
            HttpServletRequest request) {
        
        System.out.println("\n🎯 Caught request to: " + request.getRequestURI());
        if (body != null) {
            System.out.println("Body: " + body);
        }
        
        return ResponseEntity.ok("✅ Request received at: " + request.getRequestURI());
    }
}

// ===== Update SecurityConfig to allow debug endpoints =====
/*
Add to your SecurityConfig.java:

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/sensors/debug/**").permitAll()  // ✅ Allow all debug endpoints
            .requestMatchers("/api/users/register", "/api/users/me/test").permitAll()
            .requestMatchers("/api/users/**").authenticated()
            .anyRequest().permitAll()
        )
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt->{}));
    return http.build();
}
*/