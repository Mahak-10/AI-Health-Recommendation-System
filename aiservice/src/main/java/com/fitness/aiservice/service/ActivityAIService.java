package com.fitness.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityAIService {

    private final GeminiService geminiService;

    public Recommendation generateRecommendation(Activity activity){
        String prompt= createPromptForActivity(activity);
        String aiResponse= geminiService.getAnswer(prompt);
        log.info("RESPONSE FROM AI: {}", aiResponse);
        return processAiResponse(activity,aiResponse);
    }

    public Recommendation processAiResponse(Activity activity,String aiResponse)
    {
        try{
            ObjectMapper mapper=new ObjectMapper();
            JsonNode rootNode=mapper.readTree(aiResponse);
            JsonNode textNode=rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text");
            String jsonContent= textNode.asText()
                    .replaceAll("```json\\n","")
                    .replaceAll("\\n```", "")
                    .trim();

           // log.info("PARSED RESPONSE FROM AI: {}", jsonContent);

            JsonNode analysisJson = mapper.readTree(jsonContent);
            JsonNode analysisNode =analysisJson.path("analysis");
            log.info("Parsed full analysis JSON: \n{}", analysisJson.toPrettyString());
            JsonNode safetyNode = analysisJson.path("safety");
            log.info("Safety Node Type: {}", safetyNode.getNodeType());
            log.info("Safety Node isArray(): {}", safetyNode.isArray());
            log.info("Safety Node toPrettyString(): \n{}", safetyNode.toPrettyString());

            StringBuilder fullAnalysis =new StringBuilder();
            addAnalysisSection(fullAnalysis,analysisNode,"overall","Overall:");
            addAnalysisSection(fullAnalysis,analysisNode,"pace","Pace:");
            addAnalysisSection(fullAnalysis,analysisNode,"heartRate","Heart Rate:");
            addAnalysisSection(fullAnalysis,analysisNode,"caloriesBurned","Calories:");

            List<String> improvements= extractImprovements(analysisJson.path("improvements"));
            List<String> suggestions= extractSuggestions(analysisJson.path("suggestions"));
            List<String> safety= extractSafetyGuidelines(analysisJson.path("safety"));



            return Recommendation.builder()
                    .activityId(activity.getId())
                    .userId(activity.getUserId())
                    .activityType(activity.getType())
                    .recommendation(fullAnalysis.toString().trim())
                    .improvements(improvements)
                    .suggestions(suggestions)
                    .safety(safety)
                    .createdAt(LocalDateTime.now())
                    .build();
        } catch(Exception e){
            e.printStackTrace();
            return  createDefaultRecommendation(activity);
        }
    }

    private Recommendation createDefaultRecommendation(Activity activity) {
        return Recommendation.builder()
                .activityId(activity.getId())
                .userId(activity.getUserId())
                .activityType(activity.getType())
                .recommendation("Unable to generate detailed analysis")
                .improvements(Collections.singletonList("Continue with your current routine"))
                .suggestions(Collections.singletonList("Consider consulting a fitness professional"))
                .safety(Arrays.asList(
                        "Always warm up before exercise",
                        "Stay hydrated",
                        "Listen to your body"
                ))
                .createdAt(LocalDateTime.now())
                .build();

    }

    private List<String> extractSafetyGuidelines(JsonNode safetyNode) {
        List<String> safety = new ArrayList<>();

        if (safetyNode != null && safetyNode.isArray()) {
            for (JsonNode item : safetyNode) {
                if (item.isTextual()) {
                    safety.add(item.asText().trim());
                } else {
                    log.warn("Non-text item in safety array: {}", item);
                }
            }
        } else {
            log.warn("Safety node is null or not an array: {}", safetyNode);
        }

        log.info("Extracted safety guidelines list: {}", safety);
        return safety.isEmpty()
                ? Collections.singletonList("Follow general safety guidelines")
                : safety;

        }


    private List<String> extractSuggestions(JsonNode suggestionsNode) {
        List<String> suggestions=new ArrayList<>();
        if(suggestionsNode.isArray()) {
            suggestionsNode.forEach(suggestion -> {
                String workout = suggestion.path("workout").asText();

                String description = suggestion.path("description").asText();
                suggestions.add(String.format("%s: %s",workout, description));

            });
        }
            return suggestions.isEmpty() ?
                    Collections.singletonList("No specific suggestions provided"):
                    suggestions;



    }

    private List<String> extractImprovements(JsonNode improvementsNode) {
        List<String> improvements=new ArrayList<>();
        if(improvementsNode.isArray()) {
            improvementsNode.forEach(improvement -> {
                String area = improvement.path("area").asText();

                String detail = improvement.path("recommendation").asText();
                improvements.add(String.format("%s: %s", area, detail));

            });
        }
            return improvements.isEmpty() ?
                    Collections.singletonList("No specific improvements provided"):
                    improvements;




    }

    private void addAnalysisSection(StringBuilder fullAnalysis, JsonNode analysisNode, String key, String prefix) {
        if(!analysisNode.path(key).isMissingNode()){
            fullAnalysis.append(prefix)
                    .append(analysisNode.path(key).asText())
                    .append("\n\n");
        }
    }

    private String createPromptForActivity(Activity activity) {
        return String.format("""
                Analyze this fitness activity and provide detailed recommendations in this json format
                {
                  "analysis": {
                    "heartRate": "Average heart rate is slightly elevated. Indicates moderate to high intensity workout.",
                    "pace": "Average pace is 6.5 min/km, which is within a healthy aerobic zone for most runners.",
                    "heatRate": "High external temperature (34°C) increases risk of dehydration and overheating.",
                    "overall": "Good session with effective calorie burn and cardiovascular benefit.",
                    "caloriesBurned": "520 kcal burned, which is appropriate for a 45-minute session."
                  },
                  "improvements": [{
                    "area": "Hydration and recovery strategy",
                    "recommendation": "Increase water intake before and after high-heat workouts. Consider electrolyte drinks if sweating heavily."
                  }],
                  "suggestions": [{
                    "workout": "Interval Running",
                    "description": "Alternate 2 minutes fast pace (5:30/km) with 3 minutes slow jog (7:30/km) for 5 cycles to improve VO2 max and endurance."
                  }],
                  "safety": [
                     "Avoid intense outdoor workouts during peak heat hours (11 AM – 4 PM).",
                     "Monitor heart rate; if it exceeds 180 bpm consistently, reduce intensity and consult a physician."
                  ],
                  "additional_notes": "Consider using a fitness smartwatch to better monitor heart rate zones and adjust training accordingly."
                }
                
                Analyze this activity:
                Activity Type: %s
                Duration: %d minutes
                Calories Burned: %d
                Additinal Metrics: %s
                 
                 Provide detailed analysis focusing on performance, improvements, etc.
                 Ensure the response follows the EXACT JSON format shown above.
                """,
                activity.getType(),
                activity.getDuration(),
                activity.getCaloriesBurned(),
                activity.getAdditionalMetrics()


                );
    }

}
